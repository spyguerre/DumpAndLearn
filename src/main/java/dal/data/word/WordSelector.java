package dal.data.word;

import dal.data.db.Db;
import dal.graphic.NotificationDisplayer;
import dal.graphic.word.startReview.ReviewPreference;
import dal.graphic.word.startReview.WriteIn;

import java.sql.*;
import java.util.*;

import static dal.data.db.Db.fetchWords;
import static dal.graphic.word.startReview.ReviewPreference.*;

public abstract class WordSelector {
    public static List<WordReviewed> getSelection(int n, ReviewPreference preference, WriteIn writeIn) {
        List<WordReviewed> selectedWords = new ArrayList<>();
        Set<Long> selectedIds = new HashSet<>(); // Track selected word IDs

        // Ensure that there are enough words in the database
        String query = "SELECT COUNT(*) >= ? AS enoughWords FROM words LIMIT ? ;";
        ResultSet rs = Db.query(query, new Object[]{n, n+1});
        try {
            assert rs != null;
            rs.next();
            if (!rs.getBoolean("enoughWords")) {
                System.out.println("Not enough words found to start a review session.");
                NotificationDisplayer.displayError("There are less than " + n + " words in the database.");
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
                List<Word> recentWords = fetchWords(RECENT, categoryLimit);
                List<Word> oldWords = fetchWords(OLD, categoryLimit);
                List<Word> difficultWords = fetchWords(OFTEN_FAILED, categoryLimit);

                // Add priority words and track their IDs
                addUniqueWords(selectedWords, selectedIds, recentWords, writeIn, (int) (1./3. * categoryLimit));
                addUniqueWords(selectedWords, selectedIds, oldWords, writeIn, (int) (2./3. * categoryLimit));
                addUniqueWords(selectedWords, selectedIds, difficultWords, writeIn, categoryLimit);
            }
            case RECENT -> {
                List<Word> recentWords = fetchWords(RECENT, categoryLimit);

                // Add priority words and track their IDs
                addUniqueWords(selectedWords, selectedIds, recentWords, writeIn, categoryLimit);
            }
            case OLD -> {
                List<Word> oldWords = fetchWords(OLD, categoryLimit);

                addUniqueWords(selectedWords, selectedIds, oldWords, writeIn, categoryLimit);
            }
            case OFTEN_FAILED -> {
                List<Word> difficultWords = fetchWords(OFTEN_FAILED, categoryLimit);

                addUniqueWords(selectedWords, selectedIds, difficultWords, writeIn, categoryLimit);
            }
        }

        // Fetch random words
        List<Word> randomWords = fetchWords(ANY, n * 2); // Fetch extra to avoid conflicts

        // Add unique random words
        addUniqueWords(selectedWords, selectedIds, randomWords, writeIn, n);

        // And finally shuffle the selection
        Collections.shuffle(selectedWords);

        return selectedWords;
    }

    private static void addUniqueWords(List<WordReviewed> selectedWords, Set<Long> selectedIds, List<Word> words, WriteIn writeIn, int limit) {
        for (Word word : words) {
            if (selectedWords.size() >= limit) break; // Stop when we have enough words
            if (!selectedIds.contains(word.getId())) {
                selectedWords.add(new WordReviewed(word, writeIn));
                selectedIds.add(word.getId());
            }
        }
    }
}
