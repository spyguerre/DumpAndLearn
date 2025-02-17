package dal.graphic.review;

import dal.Db;
import dal.graphic.Controller;
import dal.word.WordReviewed;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;

public class CorrectionController extends Controller {
    private List<WordReviewed> words = null;
    private final List<WordReviewed> wordsToCorrect = new ArrayList<>();

    @FXML
    private VBox mainVBox;

    @FXML
    private Text rightWordsCount;

    private void initWords() {
        System.out.println("Correcting words...");
        assert words != null;

        for (WordReviewed currentWord : words) {
            boolean typedCorrectly = !(currentWord.isWrittenInForeign() && !currentWord.getForeign().equalsIgnoreCase(currentWord.getUserAnswer())
                    || !currentWord.isWrittenInForeign() && !currentWord.getNative_().equalsIgnoreCase(currentWord.getUserAnswer()));

            // Graphic update
            TextFlow correctedWordTextFlow = new TextFlow();
            VBox.setMargin(correctedWordTextFlow, new Insets(0, 0, 20, 0));
            correctedWordTextFlow.setPrefWidth(Region.USE_COMPUTED_SIZE);
            correctedWordTextFlow.setTextAlignment(TextAlignment.CENTER);

            Text emoji;
            if (typedCorrectly) {
                correctedWordTextFlow.getChildren().add(new Text("✅"));
                emoji = (Text) correctedWordTextFlow.getChildren().getFirst();
                emoji.setStroke(Color.GREEN);
            } else {
                correctedWordTextFlow.getChildren().add(new Text("❌"));
                emoji = (Text) correctedWordTextFlow.getChildren().getFirst();
                emoji.setStroke(Color.RED);
            }
            emoji.setStrokeType(StrokeType.CENTERED);
            emoji.setStrokeWidth(1);
            emoji.setFill(Color.WHITE);
            correctedWordTextFlow.getChildren().add(new Text("  -  "));
            correctedWordTextFlow.getChildren().add(new Text(currentWord.getNative_()));
            correctedWordTextFlow.getChildren().add(new Text(" = "));
            correctedWordTextFlow.getChildren().add(new Text(currentWord.getForeign()));

            if (!typedCorrectly) {
                // Add description to fail message if there is one.
                if (currentWord.getDescription() == null) {
                    correctedWordTextFlow.getChildren().add(new Text(" (and not " + currentWord.getUserAnswer() + ")"));
                } else {
                    correctedWordTextFlow.getChildren().add(new Text(" (and not " + currentWord.getUserAnswer() + " : \"" + currentWord.getDescription() + "\")"));
                }

                // Stroke the missed word.
                Text failedText;
                if (currentWord.isWrittenInForeign()) {
                    failedText = (Text) correctedWordTextFlow.getChildren().get(4);
                } else {
                    failedText = (Text) correctedWordTextFlow.getChildren().get(2);
                }
                failedText.setStroke(Color.RED);
                failedText.setStrokeWidth(1);
                failedText.setStrokeType(StrokeType.CENTERED);
            } else {
                // Stroke the correct word in green.
                Text failedText;
                if (currentWord.isWrittenInForeign()) {
                    failedText = (Text) correctedWordTextFlow.getChildren().get(4);
                } else {
                    failedText = (Text) correctedWordTextFlow.getChildren().get(2);
                }
                failedText.setStroke(Color.GREEN);
                failedText.setStrokeWidth(.5);
                failedText.setStrokeType(StrokeType.CENTERED);
            }
            // Add the TextFlow to the mainVBox
            mainVBox.getChildren().add(correctedWordTextFlow);

            // Backend update.
            Db.updateLastReviewTimestamp(currentWord.getId());
            Db.incrReviewsCount(currentWord.getId());
            if (!typedCorrectly) {
                wordsToCorrect.add(currentWord); // Add the failed word to the wordsToCorrect list to keep reviewing later
                Db.incrFailedReviews(currentWord.getId());
            }
        }

        rightWordsCount.setText(rightWordsCount.getText().replaceFirst("\\?", String.valueOf(words.size() - wordsToCorrect.size())));
        rightWordsCount.setText(rightWordsCount.getText().replaceFirst("\\?", String.valueOf(words.size())));
        System.out.println("Found " + rightWordsCount.getText());
    }

    public List<WordReviewed> getWords() {
        return words;
    }

    public void setWords(List<WordReviewed> words) {
        this.words = words;
        initWords();
    }
}
