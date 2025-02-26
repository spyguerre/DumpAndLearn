package dal.graphic.song.menu;

import dal.data.db.Db;
import dal.data.song.GeniusScraper;
import dal.data.song.YTDownloader;
import dal.graphic.ConfirmationListener;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.song.playing.MusicPlayingController;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SongMenuController extends Controller {
    @FXML
    private TextField titleTextField;

    @FXML
    private TextField artistTextField;

    @FXML
    private ProgressBar dlProgressBar;

    @FXML
    private void initialize() {
        titleTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::searchForSong));
        artistTextField.addEventHandler(KeyEvent.KEY_PRESSED, new ConfirmationListener(this::searchForSong));
    }

    @FXML
    void searchForSong() {
        // And download the song using yt-dlp.
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                // Show progressBar to indicate things are going on
                dlProgressBar.setVisible(true);

                // First search for the song's lyrics and correct info using Genius
                Object[] songInfo = (new GeniusScraper()).getSongInfo(titleTextField.getText(), artistTextField.getText());
                dlProgressBar.setProgress(0.1);
                String title = (String) songInfo[0];
                String artist = (String) songInfo[1];
                dlProgressBar.setProgress(0.2);

                // Then search the song's link using yt-dlp.
                String youtubeURL = YTDownloader.getYoutubeURL(title, artist);
                System.out.println("Youtube URL found: " + youtubeURL);
                dlProgressBar.setProgress(0.3);

                // And download the video
                YTDownloader.downloadVideo(youtubeURL, title, artist, dlProgressBar);

                // And finally once the download is complete, switch scene (in the main thread).
                Platform.runLater(() -> {
                    SceneManager.switchScene(SceneType.SONG_PLAYING, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

                    // And initialize the scene with the right music video.
                    MusicPlayingController newController = (MusicPlayingController) SceneManager.getCurrentController();
                    Long songId = Db.getsongIDFromtitle(title, artist);
                    assert songId != null;
                    newController.initVideo(songId);
                });

                return null;
            }
        };
        // Run the task on a new thread to avoid blocking JavaFX UI
        new Thread(downloadTask).start();
    }
}
