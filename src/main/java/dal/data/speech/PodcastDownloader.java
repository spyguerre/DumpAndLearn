package dal.data.speech;

import dal.data.Languages;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public abstract class PodcastDownloader {
    public static void downloadPodcast(String ytCode, Languages language, long dlId) {
        try {
            ProcessBuilder pb = new ProcessBuilder("python", "pythonHelpers/podcastHelper.py", ytCode, Languages.getStdCode(language), String.valueOf(dlId));
            pb.redirectErrorStream(true);
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
