package dal.data.song;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;

import java.io.File;

public class MusicPlayerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Path to your video file (use the absolute path for better reliability)
        File videoFile = new File("downloads/0.mp4");  // Specify relative path
        String absolutePath = videoFile.getAbsolutePath();  // Convert to absolute path

        // Check if the file exists
        if (!videoFile.exists()) {
            System.out.println("File not found: " + absolutePath);
            return;
        }

        // Convert backslashes to forward slashes
        String formattedPath = absolutePath.replace("\\", "/");

        // Create the Media object with the formatted path
        String videoFilePath = "file:///" + formattedPath;  // Make sure to prepend "file:///"
        Media media = new Media(videoFilePath);

        // Create the MediaPlayer to control the media
        MediaPlayer mediaPlayer = new MediaPlayer(media);

        // Create the MediaView to display the video
        MediaView mediaView = new MediaView(mediaPlayer);

        // Make sure the media player is properly linked to the media view
        mediaPlayer.setOnReady(() -> {
            // Set the video aspect ratio
            mediaView.setFitWidth(800);
            mediaView.setFitHeight(600);
            mediaView.setPreserveRatio(true);
        });

        // Create a button for play/pause functionality
        Button playPauseButton = new Button("Play");
        playPauseButton.setOnAction(event -> togglePlayPause(mediaPlayer, playPauseButton));

        // Create the video progress slider
        Slider progressSlider = new Slider();
        progressSlider.setMaxWidth(800);
        progressSlider.setMin(0);
        progressSlider.setValue(0);

        // Update the progress slider as the video plays
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!progressSlider.isValueChanging()) {
                // Ensure the slider value updates in sync with the media's current time
                progressSlider.setValue(newValue.toSeconds());
            }
        });

        // Set the maximum value of the progress slider when the video is ready
        mediaPlayer.setOnReady(() -> {
            progressSlider.setMax(mediaPlayer.getMedia().getDuration().toSeconds());
        });

        // Allow clicking to skip to a new time
        progressSlider.setOnMouseClicked(event -> {
            // When clicked, set the media player's time based on the slider's value
            mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
        });

        // Update the slider when the user drags it
        progressSlider.setOnMouseDragged(event -> {
            mediaPlayer.seek(javafx.util.Duration.seconds(progressSlider.getValue()));
        });

        // Create the volume slider
        Slider volumeSlider = new Slider();
        volumeSlider.setMin(0);
        volumeSlider.setMax(1);
        volumeSlider.setValue(0.5);  // Default volume level
        volumeSlider.setShowTickLabels(true);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setBlockIncrement(0.1);

        // Adjust volume based on slider value
        volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            mediaPlayer.setVolume(newValue.doubleValue());
        });

        // Create the layout with vertical spacing
        VBox controlLayout = new VBox(10);  // 10px spacing between controls
        controlLayout.getChildren().addAll(playPauseButton, progressSlider, volumeSlider);

        // Create the main layout and add the MediaView
        StackPane root = new StackPane();
        root.getChildren().add(mediaView);
        root.setStyle("-fx-background-color: #000000;");
        StackPane.setAlignment(controlLayout, javafx.geometry.Pos.BOTTOM_CENTER); // Align controls at the bottom

        // Add the controls to the bottom
        root.getChildren().add(controlLayout);

        // Set up the scene
        Scene scene = new Scene(root, 800, 700);  // Increased height for controls
        primaryStage.setTitle("JavaFX Video Player");
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start playing the video
        mediaPlayer.play();

        // Add key event listener for space bar
        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("SPACE")) {
                togglePlayPause(mediaPlayer, playPauseButton);
            }
        });
    }

    // Toggle the play/pause state and update the button text
    private void togglePlayPause(MediaPlayer mediaPlayer, Button playPauseButton) {
        if (mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
            mediaPlayer.pause();
            playPauseButton.setText("Play");
        } else {
            mediaPlayer.play();
            playPauseButton.setText("Pause");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
