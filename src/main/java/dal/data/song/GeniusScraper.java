package dal.data.song;

import dal.data.db.Db;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class GeniusScraper {
    /**
     * Gets a song's lyrics online, unless there is already a close enough match saved in the database.
     */
    public static String getLyricsWithDatabase(String title, String artist) {
        System.out.println("Looking for an exact match in the database...");

        // First, check for an exact match in the database
        String lyrics = Db.getLyricsFromDatabase(title, artist);

        if (lyrics != null) {
            System.out.println("✅ Lyrics found in database!");
            System.out.println("Title found: " + title);
            System.out.println("Artist found: " + artist);
            return lyrics;
        }

        // If no exact match is found, attempt fuzzy matching for title and artist
        System.out.println("🔍 Lyrics not found, attempting fuzzy matching...");

        // Find closest matching title and artist
        String closestTitle = findClosestMatchingTitle(title);
        String closestArtist = findClosestMatchingArtist(artist);

        // If either the title or artist was fuzzy matched, use the closest match
        if (closestTitle != null || closestArtist != null) {
            if (closestTitle != null &&!closestTitle.equals(title)) {
                title = closestTitle;
            }
            if (closestArtist != null && !closestArtist.equals(artist)) {
                artist = closestArtist;
            }

            System.out.println("Trying to retrieve lyrics from database using title: " + title + " and artist: " + artist + "...");

            lyrics = Db.getLyricsFromDatabase(title, artist);
            if (lyrics != null) {
                System.out.println("✅ Lyrics found in database!");
                System.out.println("Title found: " + title);
                System.out.println("Artist found: " + artist);
                return lyrics;
            } else {
                System.out.println("Couldn't find the lyrics in the database using fuzzy matching.");
            }
        }

        // Else scrape lyrics from Genius using the corrected title and artist
        System.out.println("Searching for the lyrics online on Genius...");
        String geniusUrl = getGeniusUrl(title, artist);
        System.out.println("Genius URL found: " + geniusUrl);
        lyrics = getLyrics(geniusUrl);

        // Save the lyrics to the database if they are found.
        if (!lyrics.equals("Couldn't find the lyrics.")) {
            System.out.println("Lyrics found online!");
            // Scrape the title and artist from the Genius page if needed
            String[] scrapedData = getTitleAndArtistFromGenius(geniusUrl);
            String correctTitle = scrapedData[0];
            System.out.println("Title found: " + correctTitle);
            String correctArtist = scrapedData[1];
            System.out.println("Artist found: " + correctArtist);

            // Check if the song already exists in the database before saving
            String existingLyrics = Db.getLyricsFromDatabase(correctTitle, correctArtist);
            if (existingLyrics == null) {
                // Only save if it doesn't already exist
                Db.saveLyricsToDatabase(correctTitle, correctArtist, lyrics);
            } else {
                System.out.println("✅ Lyrics already exist in the database, skipping save.");
            }
        }

        return lyrics;
    }

    // Fuzzy match for artist name
    private static String findClosestMatchingArtist(String userArtist) {
        List<String> artists = Db.getAllArtists();
        if (artists.isEmpty()) return null;

        String closestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (String artist : artists) {
            int distance = levenshtein.apply(userArtist.toLowerCase(), artist.toLowerCase());
            if (distance < bestDistance && distance <= 3) {  // Allow up to 3 mistakes
                bestDistance = distance;
                closestMatch = artist;
            }
        }

        if (closestMatch != null && !closestMatch.equals(userArtist)) {
            System.out.println("🔍 Received " + userArtist + ". Did you mean: " + closestMatch + "? Using closest match.");
        }
        return closestMatch;
    }

    // Fuzzy match for title name
    private static String findClosestMatchingTitle(String userTitle) {
        List<String> titles = Db.getAllTitles();  // Assume this method exists
        if (titles.isEmpty()) return null;

        String closestMatch = null;
        int bestDistance = Integer.MAX_VALUE;
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        for (String title : titles) {
            int distance = levenshtein.apply(userTitle.toLowerCase(), title.toLowerCase());
            if (distance < bestDistance && distance <= 3) {  // Allow up to 3 mistakes
                bestDistance = distance;
                closestMatch = title;
            }
        }

        if (closestMatch != null && !closestMatch.equals(userTitle)) {
            System.out.println("🔍 Received " + userTitle + ". Did you mean: " + closestMatch + "? Using closest match.");
        }
        return closestMatch;
    }

    /**
     * Search for the song on Bing and extract the Genius URL.
     */
    private static String getGeniusUrl(String songName, String artist) {
        String searchUrl = "https://www.bing.com/search?q=site:genius.com+" + songName.replace(" ", "+") + "+" + artist.replace(" ", "+");

        try {
            Document doc = Jsoup.connect(searchUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // Select the first Genius link from search results
            Element link = doc.select("li.b_algo a[href*='genius.com']").first();
            if (link != null) {
                return link.attr("href");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Scrape lyrics from the Genius page.
     */
    private static String getLyrics(String geniusUrl) {
        try {
            Document doc = Jsoup.connect(geniusUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // Select all elements inside the lyrics container
            Elements lyricElements = doc.select("div[dal.data-lyrics-container='true']");

            // Check if song is instrumental = has no lyrics.
            if (lyricElements.isEmpty()) {
                return "This song is an instrumental.";
            }

            StringBuilder lyrics = new StringBuilder();
            boolean firstLine = true;

            for (Element element : lyricElements) {
                // Extract text including section headers while preserving line breaks
                String text = element.html().replaceAll("<br>", "\n"); // Replace <br> with newlines
                text = text.replaceAll("<[^>]+>", ""); // Remove all HTML tags
                text = text.replaceAll("\n{2,}", "\n"); // Remove excessive newlines

                // Trim each line individually and add line breaks before section headers
                String[] lines = text.split("\n");
                for (String line : lines) {
                    line = line.trim();
                    if (line.matches("\\[.*\\]")) { // Check if it's a section header
                        if (!firstLine) {
                            lyrics.append("\n"); // Add a blank line before the header
                        }
                    }
                    lyrics.append(line).append("\n");
                    firstLine = false;
                }
            }

            return lyrics.toString().trim();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Couldn't find the lyrics.";
    }

    /**
     * Scrape the song title and artist from Genius page.
     */
    private static String[] getTitleAndArtistFromGenius(String geniusUrl) {
        try {
            Document doc = Jsoup.connect(geniusUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // Extract song title
            Element titleElement = doc.select("span.SongHeader-desktop-sc-d2837d6d-11.eOWfHT").first();
            String title = (titleElement != null) ? titleElement.text() : "Unknown Title";

            // Extract only the main artist (ignoring featured artists)
            Element mainArtistElement = doc.selectFirst("div.HeaderArtistAndTracklist-desktop-sc-afd25865-1 a.StyledLink-sc-15c685a-0");
            String artist = (mainArtistElement != null) ? mainArtistElement.text() : "Unknown Artist";

            return new String[]{title, artist};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"Unknown Title", "Unknown Artist"};
    }
}
