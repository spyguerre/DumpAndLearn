package dal.graphic;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class SceneManager {
    private static Controller currentController;

    public static void start(SceneType sceneType, Stage stage) throws IOException {
        System.out.println("Starting app on scene " + sceneType.name());

        FXMLLoader fxmlLoader = SceneType.getSceneFxml(sceneType);
        Scene scene = new Scene(fxmlLoader.load(), 600, 442);
        currentController = fxmlLoader.getController();

        stage.setTitle("Dump And Learn!");
        stage.setScene(scene);
        stage.show();
    }

    public static void switchScene(SceneType sceneType, Stage stage, int[] currentSize) {
        System.out.println("Switching to scene " + sceneType.name());

        Scene scene;
        try {
            FXMLLoader fxmlLoader = SceneType.getSceneFxml(sceneType);
            scene = new Scene(fxmlLoader.load(), currentSize[0], currentSize[1]);
            currentController = fxmlLoader.getController();
        } catch (IOException e) {
            System.err.println("Could not load scene " + sceneType.name() + ".");
            e.printStackTrace();
            return;
        }

        stage.setScene(scene);
    }

    public static Controller getCurrentController() {
        return currentController;
    }
}
