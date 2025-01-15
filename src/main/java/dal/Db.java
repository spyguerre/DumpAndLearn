package dal;

import dal.word.Word;
import dal.word.WordType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Db {
    private static Connection con = null;

    public static Connection getConnection() {
        if (con == null) {
            System.out.println("Connecting to database...");
            try {
                Class.forName("org.sqlite.JDBC");
                Db.con = DriverManager.getConnection("jdbc:sqlite:dalData.db");
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

        PreparedStatement pstmt = con.prepareStatement(sql);
        pstmt.setString(1, tableName);
        ResultSet rs = pstmt.executeQuery();

        if (rs.next()) {
            System.out.println("Found table " + tableName + ".");
        } else {
            System.out.println("Table " + tableName + " not found.");
            sql = """
                    CREATE TABLE words (
                        id                  INTEGER PRIMARY KEY
                                                    NOT NULL,
                        native              TEXT    NOT NULL,
                        [foreign]           TEXT    NOT NULL,
                        timestamp           NUMERIC NOT NULL,
                        reviewsCount        INTEGER NOT NULL
                                                    DEFAULT (0),
                        failedReviews       INTEGER NOT NULL
                                                    DEFAULT (0),
                        lastReviewTimestamp NUMERIC
                    );
                    """;

            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
            System.out.println("Created table " + tableName + ".");
        }
    }

    public static void closeConnection() {
        try {
            con.close();
            con = null;
        } catch (SQLException e) {
            System.out.println("Couldn't close database connection.");
        }
    }

    public static List<Word> searchForWords(String query, WordType wordType) {
        List<Word> words = new ArrayList<>();

        try {
            ResultSet rs = null;
            switch (wordType) {
                case ANY -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ? OR foreign LIKE ?";
                    rs = query(sql, new String[]{"'"+query+"%'", "'"+query+"%'"});
                }
                case NATIVE -> {
                    String sql = "SELECT * FROM words WHERE native LIKE ?";
                    rs = query(sql, new String[]{"'"+query+"%'"});
                }
                case FOREIGN -> {
                    String sql = "SELECT * FROM words WHERE foreign LIKE ?";
                    rs = query(sql, new String[]{"'"+query+"%'"});
                }
            }

            // Construct the list of words.
            assert rs != null;
            while (rs.next()) {

                words.add(new Word(
                        rs.getLong("id"),
                        rs.getString("native"),
                        rs.getString("foreign"),
                        rs.getLong("timestamp"),
                        rs.getInt("reviewsCount"),
                        rs.getInt("failedReviews"),
                        rs.getInt("lastReviewTimestamp")
                ));
            }

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error searching for words starting with '" + query + "': " + e);
        }

        return words;
    }

    private static void insert(String insertQuery, String[] args) {
        try {
            PreparedStatement stmt = con.prepareStatement(insertQuery);
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i + 1, args[i]);
            }
            stmt.executeUpdate(insertQuery);
        } catch (SQLException e) {
            System.out.println("Error executing insertion " + insertQuery);
            e.printStackTrace();
        }
    }

    private static ResultSet query(String query, String[] args) {
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            for (int i = 0; i < args.length; i++) {
                stmt.setString(i + 1, args[i]);
            }
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.out.println("Error executing query " + query);
            e.printStackTrace();
        }
        return null;
    }
}
