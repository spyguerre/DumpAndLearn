package dal;

import java.sql.*;

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

        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";

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
}
