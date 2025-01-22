package dal;

import dal.graphic.SceneManager;
import dal.graphic.SceneType;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.start(SceneType.ADD_WORD, stage);
    }

    public static void main(String[] args) {
        launch();
    }
}