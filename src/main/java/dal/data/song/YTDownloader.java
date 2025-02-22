package dal.data.song;

import dal.data.db.Db;

import java.io.*;

public class YTDownloader {
    /**
     * Search for the song with yt-dlp and extract the best Youtube URL.
     */
    public static String getYoutubeURL(String songName, String artist) {
        try {
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
        // Ensure the output directory exists
        File outputDir = new File("./downloads");
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }

        // Get the song ID from the database using the song title and artist
        Long songID = Db.getsongIDFromtitle(songTitle, artist);

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
        String command = "yt-dlp -f mp4 -o " + outputPath + " " + videoUrl;

        try {
            // Execute the command
            Process process = Runtime.getRuntime().exec(command);

            // Read and print the output from the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
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
