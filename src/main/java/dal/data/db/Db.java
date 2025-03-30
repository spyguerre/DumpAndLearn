package dal.data.db;

import dal.data.word.Word;
import dal.data.word.WordType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Db {
    private static Connection con = null;

    ///////// GENERAL /////////

    public static Connection getConnection() {
        if (con == null) {
            System.out.println("Connecting to database...");
            try {
                Class.forName("org.sqlite.JDBC");
                con = DriverManager.getConnection("jdbc:sqlite:dalData.db");
                System.out.println("Connected to database, initializing...");
                initDatabase();
            } catch (ClassNotFoundException e) {
                System.out.println("Couldn't find SQLite driver.");
                throw new RuntimeException(e);
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Couldn't open or initialize the database.");
                throw new RuntimeException(e);
            }
        }
        return con;
    }

    private static void initDatabase() throws SQLException {
        // Check for and create each table if necessary
        String[] tableNames = new String[]{"words", "lyrics", "podcasts"};
        for (String tableName : tableNames) {

            String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";

            PreparedStatement pstmt = getConnection().prepareStatement(sql);
            pstmt.setString(1, tableName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                System.out.println("Found table " + tableName + ".");
            } else {
                System.out.println("Table " + tableName + " not found.");

                if (tableName.equals("words")) {
                    sql = """
                            CREATE TABLE words (
                                id                  INTEGER UNIQUE
                                                            NOT NULL
                                                            DEFAULT (0),
                                native              TEXT    NOT NULL,
                                [foreign]           TEXT    NOT NULL,
                                description         TEXT,
                                timestamp           NUMERIC NOT NULL,
                                reviewsCount        INTEGER NOT NULL
                                                            DEFAULT (0),
                                failedReviews       INTEGER NOT NULL
                                                            DEFAULT (0),
                                lastReviewTimestamp NUMERIC,
                                PRIMARY KEY (
                                    id AUTOINCREMENT
                                )
                            );
                            """;
                } else if (tableName.equals("lyrics")) {
                    sql = """
                            CREATE TABLE lyrics (
                                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                                title       TEXT    NOT NULL,
                                artist      TEXT    NOT NULL,
                                youtubeLink TEXT,
                                geniusLink  TEXT,
                                lyrics      TEXT    NOT NULL,
                                lastPlayed  NUMERIC NOT NULL,
                                UNIQUE (
                                    title,
                                    artist
                                )
                            );
                            """;
                } else if (tableName.equals("podcasts")) {
                    sql = """
                            CREATE TABLE podcasts (
                                id                  INTEGER PRIMARY KEY AUTOINCREMENT,
                                title               TEXT,
                                ytLink              TEXT    NOT NULL
                                                            UNIQUE,
                                lastPlayedTimestamp NUMERIC NOT NULL,
                                fullyLoaded         BOOL    NOT NULL
                                                            DEFAULT (0)
                            );
                            """;
                } else {
                    throw new RuntimeException("Unknown table name: " + tableName);
                }

                pstmt = getConnection().prepareStatement(sql);
                pstmt.executeUpdate();
                System.out.println("Created table " + tableName + ".");
            }
        }
    }

    public static void closeConnection() {
        try {
            getConnection().close();
            con = null;
        } catch (SQLException e) {
            System.out.println("Couldn't close database connection.");
        }
    }

    ///////// WORDS /////////

    public static List<Word> searchForWords(String query, WordType wordType) {
        return searchForWords(query, wordType, null);
    }

    public static List<Word> searchForWords(String query, WordType wordType, Integer limit) {
        List<Word> words = new ArrayList<>();

        try {
            ResultSet rs = null;
            switch (wordType) {
                case ANY -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ? OR [foreign] LIKE ? LIMIT ?";
                    rs = query(sql, new Object[]{query+"%", query+"%", limit == null ? -1 : limit});
                }
                case NATIVE -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ? LIMIT ?";
                    rs = query(sql, new Object[]{query+"%", limit == null ? -1 : limit});
                }
                case FOREIGN -> {
                    String sql = "SELECT * FROM words WHERE [foreign] LIKE ? LIMIT ?";
                    rs = query(sql, new Object[]{query+"%", limit == null ? -1 : limit});
                }
            }

            // Construct the list of words.
            int nb = 0;
            assert rs != null;
            while (rs.next() && (limit == null || nb < limit)) {

                words.add(new Word(
                        rs.getLong("id"),
                        rs.getString("native"),
                        rs.getString("foreign"),
                        rs.getString("description"),
                        rs.getLong("timestamp"),
                        rs.getInt("reviewsCount"),
                        rs.getInt("failedReviews"),
                        rs.getInt("lastReviewTimestamp")
                ));

                nb++;
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching for words starting with '" + query + "': " + e);
        }

        return words;
    }

    public static void insertNewWord(String native_, String foreign) {
        insertNewWord(native_, foreign, null);
    }

    public static void insertNewWord(String native_, String foreign, String description) {
        String sql = "INSERT INTO words (native, [foreign], description, timestamp) VALUES (?, ?, ?, ?);";
        update(sql, new Object[]{native_, foreign, description, System.currentTimeMillis()});
    }

    public static void removeWord(String native_, String foreign) {
        String sql = "DELETE FROM words WHERE native = ? AND [foreign] = ?";
        update(sql, new Object[]{native_, foreign});
    }

    public static void incrReviewsCount(Long id) {
        String sql = "UPDATE words SET reviewsCount = reviewsCount + 1 WHERE id = ?";
        update(sql, new Object[]{id});
    }

    public static void incrFailedReviews(Long id) {
        String sql = "UPDATE words SET failedReviews = failedReviews + 1 WHERE id = ?";
        update(sql, new Object[]{id});
    }

    public static void updateLastReviewTimestamp(Long id) {
        String sql = "UPDATE words SET lastReviewTimestamp = ? WHERE id = ?";
        update(sql, new Object[]{System.currentTimeMillis(), id});
    }

    ///////// LYRICS /////////

    public static void saveLyricsToDatabase(String title, String artist, String geniusLink, String lyrics) {
        String sql = "INSERT INTO lyrics (title, artist, geniusLink, lyrics, lastPlayed) VALUES (?, ?, ?, ?, ?)";
        update(sql, new Object[]{title, artist, geniusLink, lyrics, System.currentTimeMillis()});
        System.out.println("✅ Lyrics saved to database!");
    }

    public static void saveYoutubeLink(String youtubeLink, long id) {
        String sql = "UPDATE lyrics SET youtubeLink = ? WHERE id = ?";
        update(sql, new Object[]{youtubeLink, id});
    }

    public static void deleteSongFromDatabase(String title, String artist) {
        String sql = "DELETE FROM lyrics WHERE title = ? AND artist = ?";
        update(sql, new Object[]{title, artist});
    }

    public static List<String> getAllTitlesForArtist(String artist) {
        List<String> titles = new ArrayList<>();
        String sql = "SELECT title FROM lyrics WHERE artist = ?";

        try {
            ResultSet rs = query(sql, new Object[]{artist});

            assert rs != null;
            while (rs.next()) {
                titles.add(rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    public static String getLyrics(long id) {
        String sql = "SELECT lyrics FROM lyrics WHERE id = ?";

        try {
            ResultSet rs = query(sql, new Object[]{id});

            assert rs != null;
            if (rs.next()) {
                return rs.getString("lyrics");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLyrics(String title, String artist) {
        String sql = "SELECT lyrics FROM lyrics WHERE title = ? AND artist = ?";

        try {
            ResultSet rs = query(sql, new Object[]{title, artist});

            assert rs != null;
            if (rs.next()) {
                return rs.getString("lyrics");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<String> getAllArtists() {
        List<String> artists = new ArrayList<>();
        String query = "SELECT DISTINCT artist FROM lyrics";  // Replace 'lyrics' with the correct table name

        try {
            ResultSet rs = query(query);

            // Iterate through the result set and add artists to the list
            while (rs.next()) {
                String artist = rs.getString("artist");
                artists.add(artist);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving artists from database: " + e.getMessage());
        }

        return artists;
    }

    public static List<String> getAllTitles() {
        List<String> titles = new ArrayList<>();
        String query = "SELECT DISTINCT title FROM lyrics";  // Replace 'lyrics' with the correct table name

        try {
            ResultSet rs = query(query);

            // Iterate through the result set and add titles to the list
            while (rs.next()) {
                String title = rs.getString("title");
                titles.add(title);
            }

        } catch (SQLException e) {
            System.err.println("Error retrieving titles from database: " + e.getMessage());
        }

        return titles;
    }

    public static Long getsongIDFromtitle(String title, String artist) {
        String sql = "SELECT id FROM lyrics WHERE title = ? AND artist = ?";
        try {
            ResultSet rs = query(sql, new Object[]{title, artist});

            assert rs != null;
            if (rs.next()) {
                return rs.getLong("id");
            } else {
                System.out.println("Couldn't retrieve song ID from title='" + title + "' and artist='" + artist + "'.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving song ID from database using artist='" + artist + "' and title='" + title + "': " + e.getMessage());
            return null;
        }
    }

    public static String getYoutubeLink(long id) {
        String sql = "SELECT youtubeLink FROM lyrics WHERE id = ?";
        ResultSet rs = query(sql, new Object[]{id});
        assert rs != null;
        try {
            if (rs.next()) {
                return rs.getString("youtubeLink");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving youtube link from database: " + e.getMessage());
            return null;
        }

        return null;
    }

    public static List<String> getLastPlayedSongs(int limit) {
        List<String> songs = new ArrayList<>();
        String sql = "SELECT title, artist FROM lyrics ORDER BY lastPlayed DESC LIMIT ?";
        try {
            ResultSet rs = query(sql, new Object[]{limit});

            assert rs != null;
            while (rs.next()) {
                songs.add(rs.getString("title") + " --- " + rs.getString("artist"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving last played songs from database: " + e.getMessage());
        }
        return songs;
    }

    public static void updateLastPlayed(long id) {
        String sql = "UPDATE lyrics SET lastPlayed = ? WHERE id = ?";
        update(sql, new Object[]{System.currentTimeMillis(), id});
    }

    ///////// PODCASTS /////////

    public static int savePodcastToDatabase(String ytLink) {
        String sql = "INSERT INTO podcasts (ytLink, lastPlayedTimestamp) VALUES (?, ?)";
        update(sql, new Object[]{ytLink, System.currentTimeMillis()});
        System.out.println("✅ Podcast saved to database!");
        // Return the ID of the newly inserted podcast
        return getPodcastId(ytLink);
    }

    public static boolean isPodcastFullyLoaded(String ytLink) {
        String sql = "SELECT fullyLoaded FROM podcasts WHERE ytLink = ?";
        try {
            ResultSet rs = query(sql, new Object[]{ytLink});
            assert rs != null;
            if (rs.next()) {
                return rs.getBoolean("fullyLoaded");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking if podcast is fully downloaded: " + e.getMessage());
        }
        return false;
    }

    public static void setPodcastFullyLoaded(String ytLink) {
        String sql = "UPDATE podcasts SET fullyLoaded = 1 WHERE ytLink = ?";
        try {
            update(sql, new Object[]{ytLink});
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error setting podcast as fully downloaded: " + e.getMessage());
        }
    }

    public static List<String> getRecentPodcasts(int limit) {
        List<String> podcasts = new ArrayList<>();
        String sql = "SELECT id, title FROM podcasts ORDER BY lastPlayedTimestamp DESC LIMIT ?";
        try {
            ResultSet rs = query(sql, new Object[]{limit});

            assert rs != null;
            while (rs.next()) {
                podcasts.add(rs.getInt("id") + " --- " + rs.getString("title"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving recent podcasts from database: " + e.getMessage());
        }
        return podcasts;
    }

    public static int getPodcastId(String ytLink) {
        String sql = "SELECT id FROM podcasts WHERE ytLink = ?";
        try {
            ResultSet rs = query(sql, new Object[]{ytLink});
            assert rs != null;
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving podcast ID from database: " + e.getMessage());
        }
        return -1;
    }

    public static String getPodcastYoutubeLink(int id) {
        String sql = "SELECT ytLink FROM podcasts WHERE id = ?";
        try {
            ResultSet rs = query(sql, new Object[]{id});
            assert rs != null;
            if (rs.next()) {
                return rs.getString("ytLink");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving youtube link from database: " + e.getMessage());
        }
        return null;
    }

    public static void setPodcastTitle(String ytLink, String title) {
        String sql = "UPDATE podcasts SET title = ? WHERE ytLink = ?";
        try {
            update(sql, new Object[]{title, ytLink});
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error setting podcast title: " + e.getMessage());
        }
    }

    public static String getPodcastTitle(String ytLink) {
        String sql = "SELECT title FROM podcasts WHERE ytLink = ?";
        try {
            ResultSet rs = query(sql, new Object[]{ytLink});
            assert rs != null;
            if (rs.next()) {
                return rs.getString("title");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error retrieving podcast title from database: " + e.getMessage());
        }
        return null;
    }

    public static boolean isPodcastLinkAlreadyInDatabase(String ytLink) {
        String sql = "SELECT COUNT(*) FROM podcasts WHERE ytLink = ?";
        try {
            ResultSet rs = query(sql, new Object[]{ytLink});
            assert rs != null;
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error checking if link is already in database: " + e.getMessage());
        }
        return false;
    }

    ///////// ATOMIC REQUESTS /////////

    private static String getSqlWithParams(String sql, Object[] params) {
        StringBuilder sqlWithParams = new StringBuilder(sql);
        for (Object param : params) {
            int placeholderIndex = sqlWithParams.indexOf("?");
            if (placeholderIndex != -1) {
                // Replace the first placeholder with the parameter value, formatted accordingly
                String formattedParam = formatParam(param);
                sqlWithParams.replace(placeholderIndex, placeholderIndex + 1, formattedParam);
            }
        }
        return sqlWithParams.toString();
    }

    private static String formatParam(Object param) {
        if (param == null) {
            return "NULL";
        } else if (param instanceof String) {
            return "'" + param + "'";  // Add quotes around strings
        } else if (param instanceof Integer || param instanceof Long) {
            return param.toString();  // Leave numeric values as is
        }
        // Add more type checks as needed for other types like Date, Float, etc.
        return param.toString();
    }

    public static void update(String insertion) {
        update(insertion, new Object[]{});
    }

    public static void update(String insertion, Object[] args) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(insertion);
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case null -> stmt.setNull(i + 1, Types.NULL);
                    case String s -> stmt.setString(i + 1, s);
                    case Integer integer -> stmt.setInt(i + 1, integer);
                    case Long l -> stmt.setLong(i + 1, l);
                    default -> throw new RuntimeException("Unsupported argument type: " + args[i].getClass());
                }
            }
            String sqlWithParams = getSqlWithParams(insertion, args);
            System.out.println("Executing update: " + sqlWithParams.substring(0, Math.min(sqlWithParams.length(), 3*42)) + (sqlWithParams.length() > 3*42 ? "..." : ""));
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error executing insertion " + getSqlWithParams(insertion, args));
            e.printStackTrace();
        }
    }

    public static ResultSet query(String insertion) {
        return query(insertion, new Object[]{});
    }

    public static ResultSet query(String query, Object[] args) {
        try {
            PreparedStatement stmt = getConnection().prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case String s -> stmt.setString(i + 1, s);
                    case Integer integer -> stmt.setInt(i + 1, integer);
                    case Long l -> stmt.setLong(i + 1, l);
                    case null, default -> {
                        assert args[i] != null;
                        throw new RuntimeException("Unsupported argument type: " + args[i].getClass());
                    }
                }
            }
            String sqlWithParams = getSqlWithParams(query, args);
            System.out.println("Executing update: " + sqlWithParams.substring(0, Math.min(sqlWithParams.length(), 3*42)) + (sqlWithParams.length() > 3*42 ? "..." : ""));
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Error executing query " + getSqlWithParams(query, args));
            e.printStackTrace();
        }
        return null;
    }
}
