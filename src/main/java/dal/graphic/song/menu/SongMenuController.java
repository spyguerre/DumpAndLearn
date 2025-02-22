package dal.graphic.song.menu;

import dal.data.db.Db;
import dal.data.song.GeniusScraper;
import dal.data.song.YTDownloader;
import dal.graphic.ConfirmationListener;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.song.playing.MusicPlayingController;
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
        // First search for the song's lyrics and correct info using Genius
        Object[] songInfo = GeniusScraper.getSongInfo(titleTextField.getText(), artistTextField.getText());
        String title = (String) songInfo[0];
        String artist = (String) songInfo[1];
        Long songId = Db.getsongIDFromtitle(title, artist);
        assert songId != null;

        // Then search the song's link using yt-dlp.
        String youtubeURL = YTDownloader.getYoutubeURL(title, artist);
        // And download the song using yt-dlp.
        YTDownloader.downloadVideo(youtubeURL, title, artist);

        // And finally once the download is complete, switch scene.
        SceneManager.switchScene(SceneType.SONG_PLAYING, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

        // And initialize the scene with the right music video.
        MusicPlayingController newController = (MusicPlayingController) SceneManager.getCurrentController();
        newController.initVideo(songId);
    }
}
