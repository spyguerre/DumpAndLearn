package dal;

import dal.data.ocr.OCR;
import dal.graphic.Languages;
import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.song.playing.MusicPlayingController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // OCR.read(Languages.GERMAN, 14);
        SceneManager.start(SceneType.MAIN_MENU, stage);
        // ((MusicPlayingController) SceneManager.getCurrentController()).initVideo(1);
    }

    public static void main(String[] args) {
        launch();
    }
}