package dal.graphic.review;

import dal.graphic.Controller;
import dal.word.Word;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.util.ArrayList;
import java.util.Collection;

public class ReviewController extends Controller {
    private Collection<Word> words = new ArrayList<>();

    @FXML
    private TextField nativeTextField;

    @FXML
    private TextField foreignTextField;

    @FXML
    private void initialize() {

    }

    @FXML
    private void previous() {

    }

    @FXML
    private void next() {

    }

    public Collection<Word> getWords() {
        return words;
    }

    public void setWords(Collection<Word> words) {
        this.words = words;
    }
}
