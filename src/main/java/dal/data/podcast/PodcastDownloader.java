package dal.data.podcast;

import dal.data.Languages;
import dal.data.db.Db;
import dal.graphic.NotificationDisplayer;
import dal.graphic.SceneManager;
import dal.graphic.podcast.PodcastMenuController;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.FileReader;
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

    // Returns the transcription of the podcast at the given time in seconds and the end time of the transcription found.
    public static Object[] getTranscription(long id, float seconds) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("downloads/podcasts/" + id + ".srt"));
            String line;
            StringBuilder transcription = new StringBuilder();
            float endTime = 0f;
            while ((line = reader.readLine()) != null) {
                // Read the transcription file in SRT format
                if (line.matches("\\d{2}:\\d{2}:\\d{2},\\d{3} --> \\d{2}:\\d{2}:\\d{2},\\d{3}")) {
                    String[] timeRange = line.split(" --> ");
                    float startTime = parseTimeToSeconds(timeRange[0]);
                    endTime = parseTimeToSeconds(timeRange[1]);
                    if (seconds >= startTime && seconds <= endTime) {
                        while ((line = reader.readLine()) != null && !line.isEmpty()) {
                            transcription.append(line).append("\n");
                        }
                        break;
                    }
                }
            }
            reader.close();
            return new Object[]{transcription.toString().trim(), endTime};
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[]{"", 0f};
        }
    }

    private static float parseTimeToSeconds(String time) {
        String[] parts = time.split("[:,]");
        return Integer.parseInt(parts[0]) * 3600 + Integer.parseInt(parts[1]) * 60 + Integer.parseInt(parts[2]) + Integer.parseInt(parts[3]) / 1000.0f;
    }
}
