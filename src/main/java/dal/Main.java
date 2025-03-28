package dal;

import dal.data.Languages;
import dal.data.speech.PodcastDownloader;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        PodcastDownloader.downloadPodcast("O7SYBPxN0u8", Languages.GERMAN, 1);
        System.out.println("Starting...");
        SceneManager.start(SceneType.MAIN_MENU, stage);
    }

    public static void main(String[] args) {
        launch();
    }
}