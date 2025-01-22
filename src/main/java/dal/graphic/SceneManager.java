package dal.graphic;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class SceneManager {
    public static void start(SceneType sceneType, Stage stage) throws IOException {
        FXMLLoader fxmlLoader = SceneType.getSceneFxml(sceneType);
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Dump And Learn!");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(SceneType sceneType, Stage stage) {
        Scene scene;
        try {
            FXMLLoader fxmlLoader = SceneType.getSceneFxml(sceneType);
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            System.err.println("Could not load scene " + sceneType + ".");
            e.printStackTrace();
            return;
        }

        stage.setScene(scene);
    }
}
