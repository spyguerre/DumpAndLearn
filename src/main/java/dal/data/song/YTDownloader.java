package dal.data.song;

import dal.data.db.Db;
import javafx.scene.control.ProgressBar;

import java.io.*;

public abstract class YTDownloader {
    /**
     * Search for the song with yt-dlp and extract the best Youtube URL.
     */
    public static String getYoutubeURL(String songName, String artist) {
        try {
            // Try to get the Youtube URL from the database first.
            Long id = Db.getsongIDFromtitle(songName, artist);
            if (id != null) { // Found the song ID in the database.
                String youtubeURL = Db.getYoutubeLink(id);
                if (youtubeURL != null) { // Found the song's Youtube link in the database.
                    System.out.println("Youtube URL found in the database: " + youtubeURL);
                    return youtubeURL;
                }
            }

            // Add the ytsearch: prefix to the query
            String searchQuery = "ytsearch:\"" + songName + artist + "\"";

            // Build the yt-dlp command to search for a song on YouTube
            String command = "yt-dlp --get-id --no-warnings " + searchQuery;

            // Run the command via ProcessBuilder
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Capture the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            // Wait for the process to finish
            process.waitFor();

            // Extract the video URL from the output (it will return video IDs or URLs)

            return "https://www.youtube.com/watch?v=" + output.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error getting youtube link: " + e.getMessage();
        }
    }

    public static void downloadVideo(String videoUrl, String songTitle, String artist) {
        downloadVideo(videoUrl, songTitle, artist, null);
    }

    public static void downloadVideo(String videoUrl, String songTitle, String artist, ProgressBar progressBar) {
        // Ensure the output directory exists
        File outputDir = new File("./downloads");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Get the song ID from the database using the song title and artist
        Long songID = Db.getsongIDFromtitle(songTitle, artist);

        // Update progress bar
        if (progressBar != null) {
            progressBar.setProgress(0.42);
        }

        if (songID == null) {
            System.err.println("Couldn't get song ID for title " + songTitle + " and artist " + artist + "; aborting download.");
            return;
        }

        // Construct the output path with the song ID as the filename
        String outputPath = "./downloads/" + songID + ".%(ext)s";  // %(ext)s will automatically get the correct file extension (e.g., mp4)

        // Check if the song is already downloaded (i.e., if the file exists)
        File downloadedFile = new File("./downloads/" + songID + ".mp4");
        if (downloadedFile.exists()) {
            System.out.println("Song with ID " + songID + " is already downloaded. Skipping download.");
            return;  // Skip the download process if the file already exists
        }

        // Construct the yt-dlp command with the desired video format (mp4)
        String command = "yt-dlp -f \"bestvideo[height<=300][ext=mp4][vcodec^=avc1]+bestaudio[ext=m4a][acodec^=mp4a]/best[height<=300][ext=mp4]\" --merge-output-format mp4 -o " + outputPath + " " + videoUrl;

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Read and print the output from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.contains("[download]") && line.contains("% of") && progressBar != null) {
                    progressBar.setProgress(.5 + .005 * Float.parseFloat(line.split("\\[download] ")[1].split("% of")[0]));
                }
            }

            // Also print the stderr for debugging
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            while ((line = errorReader.readLine()) != null) {
                System.err.println(line);
            }

            // Wait for the process to finish
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                Db.saveYoutubeLink(videoUrl, songID);
                System.out.println("Download complete!");
            } else {
                System.out.println("Download failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
