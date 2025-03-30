package dal.graphic.podcast;

import dal.data.db.Db;
import dal.data.podcast.PodcastDownloader;
import dal.data.translation.Translator;
import dal.graphic.VideoController;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class PodcastPlayingController extends VideoController {
    private float transcriptionUpdateTime = 0f;
    private PauseTransition sliderUpdatePause = new PauseTransition(Duration.seconds(0.742));

    @FXML
    private TextArea foreignTextArea;

    @FXML
    private TextArea nativeTextArea;

    @FXML
    public void initialize() {
        super.initialize();

        // Attach listeners to trigger translation on lyrics highlighting.
        PauseTransition pause = new PauseTransition(Duration.millis(742)); // Timer to wait for the user to finish highlighting.
        foreignTextArea.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                pause.setOnFinished(event -> {
                    System.out.println("User highlighted transcription: " + newText.substring(0, Math.min(newText.length(), 42)) + (newText.length() > 42 ? "..." : ""));
                    showTranslationPopup(newText, true);
                });
                pause.playFromStart(); // Restart the timer on every change
            }
        });
        nativeTextArea.selectedTextProperty().addListener((obs, oldText, newText) -> {
            if (!newText.isEmpty()) {
                pause.setOnFinished(event -> {
                    System.out.println("User highlighted transcription: " + newText.substring(0, Math.min(newText.length(), 42)) + (newText.length() > 42 ? "..." : ""));
                    showTranslationPopup(newText, false);
                });
                pause.playFromStart(); // Restart the timer on every change
            }
        });
    }

    private void updateTranscription(long id, float seconds) {
        // Get the transcription and update the text area
        Object[] transcriptionData = PodcastDownloader.getTranscription(id, seconds);
        String transcription = (String) transcriptionData[0];
        float endTime = (float) transcriptionData[1];
        foreignTextArea.setText(transcription.replaceAll("\\r\\n|\\r|\\n", "\n"));
        transcriptionUpdateTime = endTime;

        // Update the native text area with the translation
        nativeTextArea.setText("Updating translation...");
        new Thread(() -> {
            String translatedText = Translator.translate(transcription);
            Platform.runLater(() -> nativeTextArea.setText(translatedText));
        }).start();
    }

    @Override
    @FXML
    protected void updateVideoProgress() {
        super.updateVideoProgress();

        // Update the transcription
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        float currentTime = (float) mediaPlayer.getCurrentTime().toSeconds();

        // Only update the transcription if the user has finished interacting with the slider
        sliderUpdatePause.setOnFinished(event -> updateTranscription(id, currentTime));
        sliderUpdatePause.playFromStart();
    }

    @Override
    protected void initTranscription() {
        System.out.println("Setting the transcription...");
        // Init the transcription with the first line
        updateTranscription(id, 0f);

        // Set the media player to update the transcription at the current time
        MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
        Runnable existingOnReady = mediaPlayer.getOnReady();
        mediaPlayer.setOnReady(() -> {
            if (existingOnReady != null) existingOnReady.run(); // Set to run any previously defined onReady actions
            System.out.println("Transcription is ready.");
            mediaPlayer.setOnPlaying(() -> {
                Thread transcriptionThread = new Thread(() -> {
                    try {
                        while (!Thread.currentThread().isInterrupted() && mediaPlayer.getCurrentTime().toSeconds() < mediaPlayer.getTotalDuration().toSeconds()) {
                            float currentTime = (float) mediaPlayer.getCurrentTime().toSeconds();
                            if (currentTime >= transcriptionUpdateTime) {
                                updateTranscription(id, currentTime);
                            }
                            try {
                                Thread.sleep(200); // Sleep for a short time to avoid excessive CPU usage
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    } catch (NullPointerException e) {
                        System.out.println("Transcription thread interrupted.");
                    }
                });
                transcriptionThread.start();

                // Add a listener to stop the thread when the scene changes
                mediaPlayer.setOnStopped(transcriptionThread::interrupt);
            });
        });
    }

    @Override
    protected void updateLastPlayed() {
        Db.updateLastPlayedPodcast(id);
    }
}
