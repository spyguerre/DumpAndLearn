package dal.graphic.song.playing;

import dal.data.db.Db;
import dal.graphic.VideoController;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class MusicPlayingController extends VideoController {
    @FXML
    private TextArea lyricsTextArea;

    @FXML
    public void initialize() {
        super.initialize();

        // Attach listener to trigger translation on lyrics highlighting.
        PauseTransition pause = new PauseTransition(Duration.millis(742)); // Timer to wait for the user to finish highlighting.
        lyricsTextArea.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                pause.setOnFinished(event -> {
                    System.out.println("User highlighted lyrics: " + newText.substring(0, Math.min(newText.length(), 42)) + (newText.length() > 42 ? "..." : ""));
                    showTranslationPopup(newText);
                });
                pause.playFromStart(); // Restart the timer on every change
            }
        });
    }

    @Override
    protected void initTranscription() {
        System.out.println("Setting the lyrics...");
        String lyrics = Db.getLyrics(id);
        assert lyrics != null;
        lyricsTextArea.setText(lyrics.replaceAll("\\r\\n|\\r|\\n", "\n"));
    }

    @Override
    protected void updateLastPlayed() {
        Db.updateLastPlayedSong(id);
    }
}
