package dal.graphic;

import dal.data.db.Db;
import dal.graphic.song.playing.TranslationPopupController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public abstract class VideoController extends Controller {
    private int retryTime = 10; // Time to wait before retrying to play the video (in milliseconds)
    protected long id; // Id of the video in the database and/or in the filename.

    @FXML
    protected MediaView mediaView;

    @FXML
    private Slider progressSlider;

    @FXML
    private Slider volumeSlider;

    @FXML
    private Button playPauseButton;

    @FXML
    private StackPane popupContainerStackPane;

    @FXML
    private Rectangle dimBackground;

    @FXML
    private Pane popupContainerPane;

    @FXML
    public void initialize() {
        super.initialize();

        // Ensure MediaView resizes dynamically to fit the window
        StackPane root = (StackPane) this.root;
        mediaView.fitWidthProperty().bind(root.widthProperty().subtract(200)); // Leaves space for lyrics
        mediaView.fitHeightProperty().bind(root.heightProperty().subtract(150)); // Leaves space for controls

        // Automatically resize the rectangle to dim the background
        dimBackground.widthProperty().bind(popupContainerStackPane.widthProperty());
        dimBackground.heightProperty().bind(popupContainerStackPane.heightProperty());

        // Hide the popup when rectangle is clicked.
        dimBackground.setOnMouseClicked(event -> popupContainerStackPane.setVisible(false));
    }

    public void initVideo(String path, long id) {
        System.out.println("Initializing video...");

        this.id = id;

        // Path to video file (use the absolute path for better reliability)
        File videoFile = new File(path);  // Specify relative path
        String absolutePath = videoFile.getAbsolutePath();  // Convert to absolute path
        System.out.println(absolutePath);

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

        if (isFileLocked(videoFile)) {
            System.err.println("File is locked. Try again.");
        }

        // Create the MediaPlayer to control the media
        initMediaPlayer(media);

        // Bind the popup Pane size
        popupContainerPane.prefWidthProperty().bind(this.root.getScene().getWindow().widthProperty().multiply(0.8));
        popupContainerPane.prefHeightProperty().bind(this.root.getScene().getWindow().heightProperty().multiply(0.8));

        // Set the lyrics.
        initTranscription();

        // Update the time played
        updateLastPlayed();

        // Add key event listener for space bar pause/play.
        Platform.runLater(() -> {
            Scene scene = root.getScene();
            scene.setOnKeyPressed(event -> {
                if (event.getCode().toString().equals("SPACE")) {
                    togglePlayPause();
                }
            });
        });
    }

    private void initMediaPlayer(Media media) {
        MediaPlayer oldMediaPlayer = mediaView.getMediaPlayer();
        if (oldMediaPlayer != null) {
            oldMediaPlayer.stop();
            oldMediaPlayer.dispose();
        }

        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setOnError(() -> Platform.runLater(() -> {
            System.err.println("MediaPlayer error: " + mediaPlayer.getError());
            retryTime *= 2; // Double the retry time each time video fails to load properly.
            System.out.println("Trying to set and play the video again in " + retryTime + " milliseconds...");
            new Thread(() -> {
                try {
                    Thread.sleep(retryTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initMediaPlayer(media);
                mediaPlayer.play();
            }).start();
        }));
        mediaView.setMediaPlayer(mediaPlayer);

        Runnable existingOnReady = mediaPlayer.getOnReady();
        mediaPlayer.setOnReady(() -> {
            if (existingOnReady != null) existingOnReady.run(); // Set to run any previously defined onReady actions
            // Set the video aspect ratio.
            mediaView.setPreserveRatio(true);
            // Set the maximum value of the progress slider when the video is ready.
            progressSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
            // Adjust initial volume.
            updateVolume();
        });

        // Update the progress slider as the video plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                // Ensure the slider value updates in sync with the media's current time
                progressSlider.setValue(newValue.toSeconds());
            }
        });

        // Start playing the video
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(mediaPlayer::play);
            System.out.println("Video ready!");
        }).start();
    }

    // Toggle the play/pause state and update the button text
    @FXML
    private void togglePlayPause() {
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("▶");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("⏸");
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
    protected void updateVideoProgress() {
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
    }

    protected void showTranslationPopup(String str) {
        showTranslationPopup(str, true);
    }

    protected void showTranslationPopup(String str, boolean isForeignText) {
        // Load the new scene in the translationPopupContainer Pane.
        popupContainerPane.getChildren().clear(); // Remove previous content
        TranslationPopupController popupController = (TranslationPopupController) SceneManager.loadSceneInContainer(SceneType.TRANSLATION_POPUP, popupContainerPane);
        popupContainerStackPane.setVisible(true);
        assert popupController != null;
        popupController.initTranslation(str, isForeignText);
    }

    private boolean isFileLocked(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
             FileChannel channel = raf.getChannel();
             FileLock lock = channel.tryLock()) {
            if (lock != null) {
                lock.release(); // Release immediately
                return false;   // File is NOT locked
            }
        } catch (Exception e) {
            return true; // File is locked
        }
        return true; // Default assumption: locked
    }

    @FXML
    private void learnFromSong() {
        if (mediaView.getMediaPlayer() != null) {
            // Stop the video before switching scene.
            mediaView.getMediaPlayer().stop();
            // Release the video file that the MediaPlayer is using.
            mediaView.getMediaPlayer().dispose();
        }
        SceneManager.switchScene(SceneType.SONG_MENU, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
    }

    @FXML
    protected void mainMenu() {
        // Stop the video before switching scene.
        mediaView.getMediaPlayer().stop();
        // Release the video file that the MediaPlayer is using.
        mediaView.getMediaPlayer().dispose();
        super.mainMenu();
    }

    @FXML
    protected void quit() {
        // Stop the video before quitting.
        mediaView.getMediaPlayer().stop();
        // Release the video file that the MediaPlayer is using.
        mediaView.getMediaPlayer().dispose();
        super.quit();
    }

    protected abstract void initTranscription();

    protected abstract void updateLastPlayed();
}
