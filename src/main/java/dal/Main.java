package dal;

import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // OCR.read(Languages.GERMAN, 14);
        System.out.println("Starting...");
        SceneManager.start(SceneType.MAIN_MENU, stage);
        // ((MusicPlayingController) SceneManager.getCurrentController()).initVideo(1);
    }

    public static void main(String[] args) {
        launch();
    }
}