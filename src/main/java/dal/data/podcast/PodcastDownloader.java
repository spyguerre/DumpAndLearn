package dal.data.podcast;

import dal.data.Languages;
import dal.data.db.Db;
import dal.graphic.NotificationDisplayer;
import dal.graphic.SceneManager;
import dal.graphic.podcast.PodcastMenuController;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class PodcastDownloader {
    public static void downloadPodcast(String ytLink, Languages language, long dlId) {
        try {
            // Run the Python script to download and transcript the podcast
            ProcessBuilder pb = new ProcessBuilder("python", "pythonHelpers/podcastHelper.py", ytLink, Languages.getStdCode(language), String.valueOf(dlId));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();

            // Update the database with the podcast information
            Db.setPodcastFullyLoaded(ytLink);

            // Notify the user that the podcast is ready
            System.out.println("Podcast download completed.");
            Platform.runLater(() -> NotificationDisplayer.displayInfo("Your podcast \"" + Db.getPodcastTitle(ytLink) + "\" is ready!"));

            // If the current Controller is a PodcastMenuController, refresh the list
            if (SceneManager.getCurrentController() instanceof PodcastMenuController) {
                Platform.runLater(() -> ((PodcastMenuController) SceneManager.getCurrentController()).refreshPodcastsList());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPodcastTitle(String ytCode) {
        String title = "";
        try {
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--get-title", ytCode);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            title = reader.readLine();
            process.waitFor();
            System.out.println("Podcast's title found: " + title);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return title;
    }
}
