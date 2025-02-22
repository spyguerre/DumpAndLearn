package dal;

import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import dal.graphic.song.playing.MusicPlayingController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.start(SceneType.SONG_PLAYING, stage);
        MusicPlayingController musicPlayingController = (MusicPlayingController) SceneManager.getCurrentController();
        musicPlayingController.setVideo(0);
    }

    public static void main(String[] args) {
        launch();
    }
}