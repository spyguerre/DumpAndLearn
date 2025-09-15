package dal.graphic.word.review;

import dal.data.db.Db;
import dal.data.word.Review;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.data.word.WordReviewed;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CorrectionController extends Controller {
    private List<WordReviewed> words = null;
    private final List<WordReviewed> wordsToCorrect = new ArrayList<>();
    private int allowedError;

    @FXML
    private VBox mainVBox;

    @FXML
    private Text rightWordsCount;

    @FXML
    private Button reviewAgainButton;

    @FXML
    private HBox bottomHBox;

    private void initWords() {
        System.out.println("Correcting words...");
        assert words != null;

        GridPane gridPane = new GridPane(); // Create a GridPane to hold the words' details
        gridPane.setHgap(10); // Set horizontal gap between columns
        gridPane.setVgap(5);  // Set vertical gap between rows
        gridPane.setPadding(new Insets(10));

        // Column Titles (first row)
        Text nothingColumn = new Text(""); // Empty column for the emoji
        nothingColumn.setTextAlignment(TextAlignment.CENTER);
        GridPane.setConstraints(nothingColumn, 0, 0);
        gridPane.getChildren().add(nothingColumn);

        Text nativeWordTitle = new Text("Native Word");
        nativeWordTitle.setTextAlignment(TextAlignment.CENTER);
        GridPane.setConstraints(nativeWordTitle, 1, 0);
        gridPane.getChildren().add(nativeWordTitle);

        Text foreignWordTitle = new Text("Foreign Word");
        foreignWordTitle.setTextAlignment(TextAlignment.CENTER);
        GridPane.setConstraints(foreignWordTitle, 2, 0);
        gridPane.getChildren().add(foreignWordTitle);

        Text attemptTitle = new Text("Attempt");
        attemptTitle.setTextAlignment(TextAlignment.CENTER);
        GridPane.setConstraints(attemptTitle, 3, 0);
        gridPane.getChildren().add(attemptTitle);

        Text descriptionTitle = new Text("Description");
        descriptionTitle.setTextAlignment(TextAlignment.CENTER);
        GridPane.setConstraints(descriptionTitle, 4, 0);
        gridPane.getChildren().add(descriptionTitle);

        // Set column proportions
        ColumnConstraints emojiColumn = new ColumnConstraints();
        emojiColumn.setPercentWidth(3); // 3% for the emoji column

        ColumnConstraints nativeColumn = new ColumnConstraints();
        nativeColumn.setPercentWidth(20); // 20% for the native word column

        ColumnConstraints foreignColumn = new ColumnConstraints();
        foreignColumn.setPercentWidth(20); // 20% for the foreign word column

        ColumnConstraints attemptColumn = new ColumnConstraints();
        attemptColumn.setPercentWidth(20); // 20% for the attempt column

        ColumnConstraints descriptionColumn = new ColumnConstraints();
        descriptionColumn.setPercentWidth(37); // 37% for the description column (larger)

        gridPane.getColumnConstraints().addAll(emojiColumn, nativeColumn, foreignColumn, attemptColumn, descriptionColumn);

        for (WordReviewed currentWord : words) {
            boolean typedCorrectly = isWordCorrect(currentWord);

            // Create a row in the GridPane
            int rowIndex = gridPane.getChildren().size() / 5; // Get the row index based on current number of rows in the grid

            // Emoji Column
            Text emoji = new Text();
            Color emojiColor;
            if (typedCorrectly) {
                emoji.setText("✅");
                emojiColor = Color.GREEN;
            } else {
                emoji.setText("❌");
                emojiColor = Color.RED;
            }
            emoji.setStroke(emojiColor);
            emoji.setFill(emojiColor); // Set fill to match stroke (green or red)
            emoji.setStrokeType(StrokeType.CENTERED);
            GridPane.setConstraints(emoji, 0, rowIndex + 1); // Start adding from row 1 (below titles)
            gridPane.getChildren().add(emoji);

            // Set color for all Texts in the row (based on correctness)
            Color rowColor = typedCorrectly ? Color.GREEN : Color.RED;

            // Native Word Column
            Text nativeText = new Text(currentWord.getNative_());
            nativeText.setTextAlignment(TextAlignment.CENTER);
            nativeText.setStroke(rowColor);
            nativeText.setFill(rowColor); // Ensure fill color is correctly applied (green or red)
            GridPane.setConstraints(nativeText, 1, rowIndex + 1);
            gridPane.getChildren().add(nativeText);

            // Foreign Word Column
            Text foreignText = new Text(currentWord.getForeign());
            foreignText.setTextAlignment(TextAlignment.CENTER);
            foreignText.setStroke(rowColor);
            foreignText.setFill(rowColor); // Ensure fill color is correctly applied (green or red)
            GridPane.setConstraints(foreignText, 2, rowIndex + 1);
            gridPane.getChildren().add(foreignText);

            // User Answer Column
            Text userAnswerText = new Text(currentWord.getUserAnswer());
            userAnswerText.setTextAlignment(TextAlignment.CENTER);
            userAnswerText.setStroke(rowColor);
            userAnswerText.setFill(rowColor); // Set fill color to match stroke (green or red)
            if (!typedCorrectly) {
                userAnswerText.setFill(Color.RED); // Fill red if the answer is incorrect
            }
            GridPane.setConstraints(userAnswerText, 3, rowIndex + 1);
            gridPane.getChildren().add(userAnswerText);

            // Description Column
            Text descriptionText = new Text();
            if (currentWord.getDescription() != null) {
                descriptionText.setText(currentWord.getDescription());
            } else {
                descriptionText.setText(""); // Empty description if none is available
            }
            descriptionText.setTextAlignment(TextAlignment.CENTER);
            descriptionText.setStroke(rowColor);
            descriptionText.setFill(rowColor); // Ensure fill color is correctly applied (green or red)
            GridPane.setConstraints(descriptionText, 4, rowIndex + 1);
            gridPane.getChildren().add(descriptionText);

            // Backend update.
            Review review = new Review(currentWord.getId(), System.currentTimeMillis(), typedCorrectly);
            Db.insertReview(review);
        }

        // Add the GridPane to the main container (you need to have a parent container for this)
        mainVBox.getChildren().add(gridPane);


        // After processing every word correction, update the total count on the top label
        rightWordsCount.setText(rightWordsCount.getText().replaceFirst("\\?", String.valueOf(words.size() - wordsToCorrect.size())));
        rightWordsCount.setText(rightWordsCount.getText().replaceFirst("\\?", String.valueOf(words.size())));

        // And change the bottom buttons to a "Finish Review Session" in case there is no more word to review.
        if (wordsToCorrect.isEmpty()) {
            bottomHBox.getChildren().clear();

            Button finishReviewSessionButton = new Button("Finish Review Session");
            finishReviewSessionButton.setOnAction(event -> finishReviewSession());
            HBox.setMargin(finishReviewSessionButton, new Insets(10));
            bottomHBox.getChildren().add(finishReviewSessionButton);

            finishReviewSessionButton.requestFocus();
        } else {
            reviewAgainButton.requestFocus();
        }

        System.out.println("Found " + rightWordsCount.getText());
    }

    private boolean isWordCorrect(WordReviewed currentWord) {
        // Normalize & remove accents, then convert to lowercase
        String normalizedUserInput = normalizeWord(currentWord.getUserAnswer());
        String normalizedCorrection = normalizeWord(currentWord.isWrittenInForeign() ? currentWord.getForeign() : currentWord.getNative_());

        // Compute Levenshtein distance using Apache Commons Text
        int distance = new LevenshteinDistance().apply(normalizedUserInput, normalizedCorrection);

        // Check if within allowed error range
        return distance <= allowedError;
    }

    private String normalizeWord(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD) // Decomposes letters with accents
                .replaceAll("\\p{M}", "") // Removes diacritics (accents)
                .toLowerCase(Locale.ROOT); // Ensures case insensitivity
    }

    private void clearUserAnswers() {
        for (WordReviewed currentWord : wordsToCorrect) {
            currentWord.setUserAnswer("");
            currentWord.resetHintRevealed();
        }
    }

    @FXML
    private void quitReviewing() {
        SceneManager.switchScene(SceneType.START_REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    @FXML
    private void reviewFailedWordsAgain() {
        // Switch scene
        SceneManager.switchScene(SceneType.REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

        // Get the new controller and pass it the list of failed words to review
        ReviewController newController = (ReviewController) SceneManager.getCurrentController();
        clearUserAnswers();
        Collections.shuffle(wordsToCorrect);
        newController.setWords(wordsToCorrect, allowedError);
    }

    private void finishReviewSession() {
        SceneManager.switchScene(SceneType.START_REVIEW, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});
    }

    public List<WordReviewed> getWords() {
        return words;
    }

    public void setWords(List<WordReviewed> words, int allowedError) {
        this.allowedError = allowedError;
        this.words = words;
        initWords();
    }
}
