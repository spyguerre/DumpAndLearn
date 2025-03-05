package dal.graphic.song.playing;

import dal.data.db.Db;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

public class MusicPlayingController extends Controller {
    @FXML
    private MediaView mediaView;

    @FXML
    private Slider progressSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button playPauseButton;

    @FXML
    private TextArea lyricsTextArea;

    @FXML
    private StackPane popupContainerStackPane;

    @FXML
    private Rectangle dimBackground;

    @FXML
    private Pane PopupContainerPane;

    @FXML
    public void initialize() {
        super.initialize();

        // Ensure MediaView resizes dynamically to fit the window
        StackPane root = (StackPane) this.root;
        mediaView.fitWidthProperty().bind(root.widthProperty().subtract(200)); // Leaves space for lyrics
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(100)); // Leaves space for controls

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

        // Automatically resize the rectangle to dim the background
        dimBackground.widthProperty().bind(popupContainerStackPane.widthProperty());
        dimBackground.heightProperty().bind(popupContainerStackPane.heightProperty());

        // Hide the popup when rectangle is clicked.
        dimBackground.setOnMouseClicked(event -> popupContainerStackPane.setVisible(false));
    }

    public void initVideo(long songId) {
        System.out.println("Initializing video...");

        // Path to video file (use the absolute path for better reliability)
        File videoFile = new File("downloads/" + songId + ".mp4");  // Specify relative path
        String absolutePath = videoFile.getAbsolutePath();  // Convert to absolute path

        // Check if the file exists
        if (!videoFile.exists()) {
            System.out.println("File not found: " + absolutePath);
            return;
        }

        System.out.println("File found, loading...");

        // Convert backslashes to forward slashes
        String formattedPath = absolutePath.replace("\\", "/");

        // Create the Media object with the formatted path
        String videoFilePath = "file:///" + formattedPath;  // Make sure to prepend "file:///"
        Media media = new Media(videoFilePath);

        // Create the MediaPlayer to control the media
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> {
            System.err.println("MediaPlayer error: " + mediaPlayer.getError());
        });
        mediaView.setMediaPlayer(mediaPlayer);

        mediaPlayer.setOnReady(() -> {
            // Set the video aspect ratio.
            mediaView.setPreserveRatio(true);
            // Set the maximum value of the progress slider when the video is ready.
            progressSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
        });

        // Update the progress slider as the video plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                // Ensure the slider value updates in sync with the media's current time
                progressSlider.setValue(newValue.toSeconds());
            }
        });

        // Adjust initial volume.
        updateVolume();


        // Set the lyrics.
        System.out.println("Setting the lyrics...");
        String lyrics = Db.getLyrics(songId);
        assert lyrics != null;
        lyricsTextArea.setText(lyrics.replaceAll("\\r\\n|\\r|\\n", "\n"));

        // Start playing the video
        System.out.println("Video ready!");
        mediaPlayer.play();

        // Add key event listener for space bar pause/play.
        Scene scene = root.getScene();
        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("SPACE")) {
                togglePlayPause();
            }
        });
    }

    // Toggle the play/pause state and update the button text
    @FXML
    private void togglePlayPause() {
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
        }
    }

    @FXML
    private void updateVolume() {
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        double sliderValue = volumeSlider.getValue();

        // Apply the power curve
        double volume = Math.pow(sliderValue, 4.2);

        // Set the volume
        mediaPlayer.setVolume(volume);
    }

    @FXML
    private void updateVideoProgress() {
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
    }

    public void showTranslationPopup(String str) {
        // Load the new scene in the translationPopupContainer Pane.
        TranslationPopupController popupController = (TranslationPopupController) SceneManager.loadSceneInContainer(SceneType.TRANSLATION_POPUP, PopupContainerPane);
        popupContainerStackPane.setVisible(true);
        assert popupController != null;
        popupController.initTranslation(str);
    }

    @FXML
    private void learnFromSong() {
        // Stop the video before switching scene.
        mediaView.getMediaPlayer().stop();
        SceneManager.switchScene(SceneType.SONG_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
    }

    @FXML
    protected void mainMenu() {
        // Stop the video before switching scene.
        mediaView.getMediaPlayer().stop();
        super.mainMenu();
    }

    @FXML
    protected void quit() {
        // Stop the video before quitting.
        mediaView.getMediaPlayer().stop();
        super.quit();
    }
}
