package dal.word;

import dal.Db;
import dal.graphic.ErrorDisplayer;
import dal.graphic.startReview.ReviewPreference;

import java.sql.*;
import java.util.*;

public abstract class WordSelector {
    public static List<Word> getSelection(int n, ReviewPreference preference) {
        List<Word> selectedWords = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>(); // Track selected word IDs

        // Ensure that there are enough words in the database
        String query = "SELECT COUNT(*) >= ? AS enoughWords FROM words LIMIT ? ;";
        ResultSet rs = Db.query(query, new Object[]{n, n+1});
        try {
            assert rs != null;
            rs.next();
            if (!rs.getBoolean("enoughWords")) {
                System.out.println("Not enough words found to start a review session.");
                ErrorDisplayer.displayError("There are less than " + n + " words in the database.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't ensure there are enough words in the database.");
        }

        int categoryLimit = Math.max(1, (n+1) / 2); // Half the words follow a rule

        // Fetch words from the requested query
        switch(preference) {
            case ANY -> {
                List<Word> recentWords = fetchWords("""
                SELECT * FROM words
                ORDER BY timestamp DESC, reviewsCount ASC
                LIMIT ?
                """, categoryLimit);
                List<Word> oldWords = fetchWords("""
                SELECT * FROM words
                ORDER BY timestamp ASC
                LIMIT ?
                """, categoryLimit);
                List<Word> difficultWords = fetchWords("""
                SELECT *, (CAST(failedReviews AS FLOAT) / (reviewsCount + 1)) AS difficulty
                FROM words
                ORDER BY difficulty DESC
                LIMIT ?
                """, categoryLimit);

                // Add priority words and track their IDs
                addUniqueWords(selectedWords, selectedIds, recentWords, (int) (1./3. * categoryLimit));
                addUniqueWords(selectedWords, selectedIds, oldWords, (int) (2./3. * categoryLimit));
                addUniqueWords(selectedWords, selectedIds, difficultWords, categoryLimit);
            }
            case RECENT -> {
                List<Word> recentWords = fetchWords("""
                SELECT * FROM words
                ORDER BY timestamp DESC, reviewsCount ASC
                LIMIT ?
                """, categoryLimit);

                // Add priority words and track their IDs
                addUniqueWords(selectedWords, selectedIds, recentWords, categoryLimit);
            }
            case OLD -> {
                List<Word> oldWords = fetchWords("""
                SELECT * FROM words
                ORDER BY timestamp ASC
                LIMIT ?
                """, categoryLimit);

                addUniqueWords(selectedWords, selectedIds, oldWords, categoryLimit);
            }
            case OFTEN_FAILED -> {
                List<Word> difficultWords = fetchWords("""
                SELECT *, (CAST(failedReviews AS FLOAT) / (reviewsCount + 1)) AS difficulty
                FROM words
                ORDER BY difficulty DESC
                LIMIT ?
                """, categoryLimit);

                addUniqueWords(selectedWords, selectedIds, difficultWords, categoryLimit);
            }
        }

        // Fetch random words
        List<Word> randomWords = fetchWords("SELECT * FROM words ORDER BY RANDOM() LIMIT ?", n * 2); // Fetch extra to avoid conflicts

        // Add unique random words
        addUniqueWords(selectedWords, selectedIds, randomWords, n);

        // And finally shuffle the selection
        Collections.shuffle(selectedWords);

        return selectedWords;
    }

    private static List<Word> fetchWords(String sql, int limit) {
        List<Word> words = new ArrayList<>();
        try {
            ResultSet rs = Db.query(sql, new Object[]{limit});
            assert rs != null;
            while (rs.next()) {
                words.add(new Word(
                        rs.getInt("id"),
                        rs.getString("native"),
                        rs.getString("foreign"),
                        rs.getLong("timestamp"),
                        rs.getInt("reviewsCount"),
                        rs.getInt("failedReviews"),
                        rs.getLong("lastReviewTimestamp")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching words: " + e.getMessage());
        }
        return words;
    }

    private static void addUniqueWords(List<Word> selectedWords, Set<Long> selectedIds, List<Word> words, int limit) {
        for (Word word : words) {
            if (selectedWords.size() >= limit) break; // Stop when we have enough words
            if (!selectedIds.contains(word.getId())) {
                selectedWords.add(word);
                selectedIds.add(word.getId());
            }
        }
    }
}
