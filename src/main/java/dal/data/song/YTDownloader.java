package dal.data.song;

import java.io.*;

public class YTDownloader {
    public static void main(String[] args) {
        String videoUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ"; // Your video URL
        String outputPath = "./downloads/%(title)s.%(ext)s";  // Relative path for the download folder

        downloadVideo(videoUrl, outputPath);
    }

    public static void downloadVideo(String videoUrl, String outputPath) {
        // Ensure the output directory exists
        File outputDir = new File("./downloads");
        if (!outputDir.exists()) {
            outputDir.mkdir();
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
                System.out.println("Download complete!");
            } else {
                System.out.println("Download failed with exit code: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
