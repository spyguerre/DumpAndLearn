package dal.graphic.podcast;

import dal.data.Languages;
import dal.data.db.Db;
import dal.data.podcast.PodcastDownloader;
import dal.graphic.*;
import dal.graphic.general.SettingsController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class PodcastMenuController extends Controller {
    private final ObservableList<String> loadedPodcastsList = FXCollections.observableArrayList();

    @FXML
    private TextField linkTextField;

    @FXML
    private ListView<String> podcastsListView;

    @FXML
    protected void initialize() {;
        // Add confirmation listener to the link text field
        linkTextField.setOnAction(event -> {
            String link = linkTextField.getText();
            if (link != null && !link.isEmpty()) {
                loadPodcast();
            }
        });

        // Initialize the podcasts list view
        podcastsListView.setItems(loadedPodcastsList);
        loadedPodcastsList.addAll(Db.getRecentPodcasts(20));

        // Set listener to react to clicks on the list
        podcastsListView.setOnMouseClicked(event -> {
            String selectedPodcast = podcastsListView.getSelectionModel().getSelectedItem();
            // If the user double-clicked, play the podcast
            if (event.getClickCount() == 2 && selectedPodcast != null) {
                String[] podcastInfo = selectedPodcast.split(" --- ");
                playPodcast(Integer.parseInt(podcastInfo[0]));
            }
        });
        // Set it to also react to Enter key
        podcastsListView.addEventHandler(KeyEvent.KEY_PRESSED, new KeyboardListener(() -> {
            String selectedPodcast = podcastsListView.getSelectionModel().getSelectedItem();
            if (selectedPodcast != null) {
                String[] podcastInfo = selectedPodcast.split(" --- ");
                playPodcast(Integer.parseInt(podcastInfo[0]));
            }
        }));
    }

    @FXML
    private void loadPodcast() {
        String link = linkTextField.getText();

        // Ensure the link is not empty
        if (link == null || link.isEmpty()) {
            NotificationDisplayer.displayError("Please enter a podcast link.");
            return;
        }

        // Check if the link is already in database
        if (Db.isPodcastLinkAlreadyInDatabase(link)) {
            playPodcast(Db.getPodcastId(link));
            return;
        }

        linkTextField.clear();
        NotificationDisplayer.displayInfo("Your podcast is being loaded. This might take a few minutes, but you can continue using the app in the mean time!");

        // Save the podcast to the database and get its id
        long dlId = Db.savePodcastToDatabase(link);

        // Get the podcast title and update the database
        new Thread(() -> {
            Db.setPodcastTitle(link, PodcastDownloader.getPodcastTitle(link));
            PodcastDownloader.downloadPodcast(link, Languages.valueOf(SettingsController.getForeignLanguage().toUpperCase()), dlId);
        }).start();
    }

    private void playPodcast(long podcastId) {
        // Load the podcast in the player and initialize its video
        SceneManager.switchScene(SceneType.PODCAST_PLAYING, (Stage) root.getScene().getWindow(), new int[]{(int)((Pane)root).getWidth(), (int)((Pane)root).getHeight()});
        PodcastPlayingController podcastPlayingController = (PodcastPlayingController) SceneManager.getCurrentController();
        podcastPlayingController.initVideo("downloads/podcasts/" + podcastId + ".mp4", podcastId);
    }

    public void refreshPodcastsList() {
        loadedPodcastsList.clear();
        loadedPodcastsList.addAll(Db.getRecentPodcasts(20));
        podcastsListView.refresh();
    }
}
