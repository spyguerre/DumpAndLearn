package dal.graphic.word.addWord;

import dal.data.word.Word;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

public class WordCell extends GridPane {
    private final Word word;

    public WordCell(Word word) {
        super();

        // Set the word
        this.word = word;

        // Divide the GridPane into 7 equal columns
        for (int i = 0; i < 7; i++) {
            getColumnConstraints().add(new ColumnConstraints());
            // Set the percentage width of each column
            getColumnConstraints().get(i).setPercentWidth(100.0 / 7);
        }

        // Add the word's data to the GridPane in Labels
        add(new Label(word.getNative_()), 0, 0);
        add(new Label(word.getForeign()), 1, 0);
        add(new Label(word.getDescription() == null ? "/" : word.getDescription()), 2, 0);
        add(new Label(java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy").format(java.time.Instant.ofEpochMilli(word.getTimeStamp()).atZone(java.time.ZoneId.systemDefault()).toLocalDate())), 3, 0);
        add(new Label(word.getReviewsCount() == 0 ? "/" : String.valueOf(word.getReviewsCount())), 4, 0);
        add(new Label(word.getFailedReviews() == 0 ? "/" : String.valueOf(word.getFailedReviews())), 5, 0);
        add(new Label(word.getLastReviewsTimestamp() == 0 ? "/" : java.time.format.DateTimeFormatter.ofPattern("MM/dd/yy").format(java.time.Instant.ofEpochMilli(word.getLastReviewsTimestamp()).atZone(java.time.ZoneId.systemDefault()).toLocalDate())), 6, 0);

        // Center each Label in its cell
        for (int i = 0; i < 7; i++) {
            Label label = (Label) getChildren().get(i);
            label.setAlignment(Pos.CENTER);
            label.setTextAlignment(TextAlignment.CENTER);
            GridPane.setHalignment(label, HPos.CENTER);
            GridPane.setValignment(label, VPos.CENTER);
        }
    }

    public Word getWord() {
        return word;
    }
}
