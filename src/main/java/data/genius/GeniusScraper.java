package data.genius;

import data.db.Db;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;

public class GeniusScraper {
    // Fuzzy match for artist name
    private static String findClosestMatchingArtist(String userArtist) {
        List<String> artists = Db.getAllArtists();  // Assume this method exists
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

    public static String getLyricsWithDatabase(String title, String artist) {
        // First, check for an exact match in the database
        String lyrics = Db.getLyricsFromDatabase(title, artist);

        if (lyrics != null) {
            System.out.println("✅ Lyrics found in database!");
            return lyrics;
        }

        // If no exact match is found, attempt fuzzy matching for title and artist
        System.out.println("🔍 Lyrics not found, attempting fuzzy matching...");

        // Find closest matching title and artist
        String closestTitle = findClosestMatchingTitle(title);
        String closestArtist = findClosestMatchingArtist(artist);

        // If no close match for the title, try scraping from Genius
        if (closestTitle != null && !closestTitle.equals(title)) {
            System.out.println("🔍 Did you mean the song: " + closestTitle + "? Trying the closest match.");
        }
        if (closestArtist != null && !closestArtist.equals(artist)) {
            System.out.println("🔍 Did you mean the artist: " + closestArtist + "? Trying the closest match.");
        }

        // If either the title or artist was fuzzy matched, use the closest match
        if (closestTitle != null) {
            title = closestTitle;
        }
        if (closestArtist != null) {
            artist = closestArtist;
        }

        // Scrape lyrics from Genius using the corrected title and artist
        String geniusUrl = getGeniusUrl(title, artist);
        lyrics = getLyrics(geniusUrl);

        if (!lyrics.equals("Couldn't find the lyrics.")) {
            // Scrape the title and artist from the Genius page if needed
            String[] scrapedData = getTitleAndArtistFromGenius(geniusUrl);
            String correctTitle = scrapedData[0];
            String correctArtist = scrapedData[1];

            // Save the correct data to the database
            Db.saveLyricsToDatabase(correctTitle, correctArtist, lyrics);
        }

        return lyrics;
    }

    /**
     * Search for the song on Bing and extract the Genius URL.
     */
    public static String getGeniusUrl(String songName, String artist) {
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
    public static String getLyrics(String geniusUrl) {
        try {
            Document doc = Jsoup.connect(geniusUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // Select all elements inside the lyrics container
            Elements lyricElements = doc.select("div[data-lyrics-container='true']");

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
    public static String[] getTitleAndArtistFromGenius(String geniusUrl) {
        try {
            Document doc = Jsoup.connect(geniusUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();

            // Extract song title
            Element titleElement = doc.select("span.SongHeader-desktop-sc-d2837d6d-11.eOWfHT").first();
            String title = (titleElement != null) ? titleElement.text() : "Unknown Title";

            // Extract artist name
            Element artistElement = doc.select("a.StyledLink-sc-15c685a-0.lHPZL").first();
            String artist = (artistElement != null) ? artistElement.text() : "Unknown Artist";

            return new String[]{title, artist};
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{"Unknown Title", "Unknown Artist"};
    }
}
