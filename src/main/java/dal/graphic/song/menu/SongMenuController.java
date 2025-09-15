package dal.graphic.song.menu;

import dal.data.db.Db;
import dal.data.song.GeniusScraper;
import dal.data.song.YTDownloader;
import dal.graphic.KeyboardListener;
import dal.graphic.Controller;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.song.playing.MusicPlayingController;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SongMenuController extends Controller {
    private final ObservableList<String> songsList = FXCollections.observableArrayList();

    @FXML
    private TextField titleTextField;

    @FXML
    private TextField artistTextField;

    @FXML
    private ProgressBar dlProgressBar;

    @FXML
    private ListView<String> lastPlayedSongsListView;

    @FXML
    private CheckBox forceRedownloadCheckbox;

    @FXML
    private CheckBox ensureGeniusMatchCheckbox;

    @FXML
    protected void initialize() {
        super.initialize();

        titleTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::searchForSong));
        artistTextField.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::searchForSong));

        // Set the last played songs list
        lastPlayedSongsListView.setItems(songsList);
        songsList.addAll(Db.getLastPlayedSongs(20));

        // Set listener to react to clicks on the list
        lastPlayedSongsListView.setOnMouseClicked(event -> {
            // Fill the text fields with the selected song's info
            String selectedSong = lastPlayedSongsListView.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                String[] songInfo = selectedSong.split(" --- ");
                titleTextField.setText(songInfo[0]);
                artistTextField.setText(songInfo.length > 1 ? songInfo[1] : "");
                // If the user double-clicked, search for the song
                if (event.getClickCount() == 2) {
                    searchForSong();
                }
            }
        });
        // Set it to also react to Enter key
        lastPlayedSongsListView.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(this::searchForSong));
    }

    @FXML
    private void searchForSong() {
        // Disable everything to avoid multiple searches
        root.setDisable(true);

        // And download the song using yt-dlp.
        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                // Show progressBar to indicate things are going on
                Platform.runLater(() -> dlProgressBar.setVisible(true));

                // First search for the song's lyrics and correct info using Genius
                Object[] songInfo = (new GeniusScraper()).getSongInfo(titleTextField.getText(), artistTextField.getText(), forceRedownloadCheckbox.isSelected(), ensureGeniusMatchCheckbox.isSelected());
                Platform.runLater(() -> dlProgressBar.setProgress(0.1));
                String title = (String) songInfo[0];
                String artist = (String) songInfo[1];
                Platform.runLater(() -> dlProgressBar.setProgress(0.2));

                // Then search the song's link using yt-dlp.
                String youtubeURL = YTDownloader.getYoutubeURL(title, artist, forceRedownloadCheckbox.isSelected());
                System.out.println("Youtube URL found: " + youtubeURL);
                Platform.runLater(() -> dlProgressBar.setProgress(0.3));

                // And download the video
                YTDownloader.downloadVideo(youtubeURL, title, artist, forceRedownloadCheckbox.isSelected(), dlProgressBar);

                try {
                    Thread.sleep(250); // Give the system to recognize the video file properly or something.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // And finally once the download is complete, switch scene (in the main thread).
                Platform.runLater(() -> {
                    SceneManager.switchScene(SceneType.SONG_PLAYING, (Stage) root.getScene().getWindow(), new int[]{(int)((BorderPane)root).getWidth(), (int)((BorderPane)root).getHeight()});

                    // And initialize the scene with the right music video.
                    MusicPlayingController newController = (MusicPlayingController) SceneManager.getCurrentController();
                    Long songId = Db.getsongIDFromtitle(title, artist);
                    assert songId != null;
                    newController.initVideo("downloads/music/" + songId + ".mp4", songId);
                });

                return null;
            }
        };
        // Run the task on a new thread to avoid blocking JavaFX UI
        new Thread(downloadTask).start();
    }
}
