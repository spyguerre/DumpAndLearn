package dal;

import dal.word.Word;
import dal.word.WordType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class Db {
    private static Connection con = null;

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
                System.out.println("Couldn't open or initialize the database.");
                throw new RuntimeException(e);
            }
        }
        return con;
    }

    private static void initDatabase() throws SQLException {
        String tableName = "words";

        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name = ?";

        PreparedStatement pstmt = getConnection().prepareStatement(sql);
        pstmt.setString(1, tableName);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("Found table " + tableName + ".");
        } else {
            System.out.println("Table " + tableName + " not found.");
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

            pstmt = getConnection().prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Created table " + tableName + ".");
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

    public static List<Word> searchForWords(String query, WordType wordType) {
        return searchForWords(query, wordType, null);
    }

    public static List<Word> searchForWords(String query, WordType wordType, Integer limit) {
        List<Word> words = new ArrayList<>();

        try {
            ResultSet rs = null;
            switch (wordType) {
                case ANY -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ? OR [foreign] LIKE ?";
                    rs = query(sql, new String[]{query+"%", query+"%"});
                }
                case NATIVE -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ?";
                    rs = query(sql, new String[]{query+"%"});
                }
                case FOREIGN -> {
                    String sql = "SELECT * FROM words WHERE [foreign] LIKE ?";
                    rs = query(sql, new String[]{query+"%"});
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
            System.out.println("Executing update: " + getSqlWithParams(insertion, args));
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
            System.out.println("Executing query: " + getSqlWithParams(query, args));
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Error executing query " + getSqlWithParams(query, args));
            e.printStackTrace();
        }
        return null;
    }
}
