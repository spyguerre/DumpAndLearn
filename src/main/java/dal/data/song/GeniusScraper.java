package dal.data.song;

import dal.data.db.Db;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class GeniusScraper {
    private String bestGeniusURL = null;
    private Document geniusDoc = null;

    /**
     * Gets a song's lyrics online, unless there is already a close enough match saved in the database.
     * @return the corrected (if necessary) title, artist, and lyrics.
     */
    public String[] getSongInfo(String title, String artist, boolean forceRedownload, boolean ensureLyricsMatch) {
        String lyrics;
        if (!forceRedownload) {
            System.out.println("Looking for an exact match in the database...");

            // First, check for an exact match in the database
            lyrics = Db.getLyrics(title, artist);

            if (lyrics != null) {
                System.out.println("✅ Lyrics found in database!");
                System.out.println("Title found: " + title);
                System.out.println("Artist found: " + artist);
                return new String[]{title, artist, lyrics};
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

                lyrics = Db.getLyrics(title, artist);
                if (lyrics != null) {
                    System.out.println("✅ Lyrics found in database!");
                    System.out.println("Title found: " + title);
                    System.out.println("Artist found: " + artist);
                    return new String[]{title, artist, lyrics};
                } else {
                    System.out.println("Couldn't find the lyrics in the database using fuzzy matching.");
                }
            } else {
                System.out.println("Couldn't find the lyrics in the database using fuzzy matching.");
            }

        }
        // Else scrape lyrics from Genius using the corrected title and artist
        System.out.println("Searching for the lyrics online on Genius...");
        String geniusUrl = getGeniusUrl(title, artist, ensureLyricsMatch);
        System.out.println("Genius URL found: " + geniusUrl);
        lyrics = getLyrics();

        // Scrape the title and artist from the Genius page if possible.
        String correctTitle = title;
        String correctArtist = artist;
        if (geniusUrl != null) {
            String[] scrapedData = getTitleAndArtistFromGenius();
            correctTitle = scrapedData[0];
            System.out.println("Title found: " + correctTitle);
            correctArtist = scrapedData[1];
            System.out.println("Artist found: " + correctArtist);
        }

        // Save the lyrics to the database.
        if (geniusUrl != null) {
            System.out.println("Lyrics found online!");
        } else {
            System.out.println("Couldn't find the lyrics online.");
        }
        // Check if the song already exists in the database before saving
        String existingLyrics = Db.getLyrics(correctTitle, correctArtist);
        if (existingLyrics == null || forceRedownload) {
            // Only save if it doesn't already exist or if the user wants to force redownload.
            Db.deleteSongFromDatabase(correctTitle, correctArtist); // Delete the song if it already exists
            Db.saveLyricsToDatabase(correctTitle, correctArtist, geniusUrl, lyrics);
        } else {
            System.out.println("✅ Lyrics already exist in the database, skipping save.");
        }

        return new String[]{correctTitle, correctArtist, lyrics};
    }

    // Fuzzy match for artist name
    private String findClosestMatchingArtist(String userArtist) {
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
    private String findClosestMatchingTitle(String userTitle) {
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
     * Search for the song using googlesearch python module and extract the Genius URL.
     */
    public String getGeniusUrl(String songName, String artist, boolean ensureLyricsMatch) {
        // Check if we searched for the best genius link before.
        if (bestGeniusURL != null) {
            return bestGeniusURL;
        }

        // Construct the search query
        String query = songName + " " + artist + " lyrics site:genius.com";

        // Python command to perform the search using duckduckgo package
        String command = "python -c \"from ddgs import DDGS; [print(r[\\\"href\\\"]) for r in DDGS().text(\\\"" + query.replace("'", "\\'") + "\\\", max_results=10)]\"";

        try {
            System.out.println("Running command: " + command);

            // Run the Python command
            Process process = Runtime.getRuntime().exec(command);

            // Capture the output from the process (the list of URLs)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // If we find a link:
                if (line.contains("genius.com")) {
                    bestGeniusURL = line;

                    if (!ensureLyricsMatch) { // If the user doesn't care about the lyrics matching, return the first link found.
                        return line;
                    }

                    // Else check that it contains a title and artist that matches the user's input
                    Document doc = getGeniusDocument();
                    assert doc != null;

                    String[] scrappedData = getTitleAndArtistFromGenius();
                    String scrappedTitle = scrappedData[0];
                    String scrappedArtist = scrappedData[1];

                    LevenshteinDistance levenshtein = new LevenshteinDistance();
                    int titleDistance = levenshtein.apply(scrappedTitle.toLowerCase(), songName.toLowerCase());
                    int artistDistance = levenshtein.apply(scrappedArtist.toLowerCase(), artist.toLowerCase());

                    System.out.println("Trying Genius URL: " + bestGeniusURL);
                    System.out.println("Title found on Genius page: \"" + scrappedTitle + "\"; Artist found on Genius page: \"" + scrappedArtist + "\"");
                    System.out.println("Title distance: " + titleDistance + ", Artist distance: " + artistDistance);
                    if (titleDistance <= 5 && artistDistance <= 5) {
                        return line;  // Return the link if the distance is low enough, ie the search succeeded.
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // If no Genius link was found, return null
        System.out.println("The Genius links found do not match the title and artist given by the user.");
        bestGeniusURL = null;
        return null;
    }

    /**
     * Scrape lyrics from the Genius page.
     */
    private String getLyrics() {
        if (bestGeniusURL == null) {
            return "It seems lyrics aren't listed on Genius.";
        }

        Document doc = getGeniusDocument();
        assert doc != null;

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
    }

    /**
     * Scrape the song title and artist from Genius page.
     * @return String[]{title, artist}
     */
    private String[] getTitleAndArtistFromGenius() {
        assert bestGeniusURL != null;

        Document doc = getGeniusDocument();
        assert doc != null;

        // Extract song title
        Element titleElement = doc.select("span[class^=SongHeader-desktop__HiddenMask-sc-]").first();
        String title = (titleElement != null) ? titleElement.text() : "Unknown Title";

        // Extract only the main artist (ignoring featured artists)
        Element mainArtistElement = doc.selectFirst("div[class^=SongHeader-desktop__CreditList-sc-] a[class^=StyledLink-sc-]");
        String artist = (mainArtistElement != null) ? mainArtistElement.text() : "Unknown Artist";

        return new String[]{title, artist};
    }

    private Document getGeniusDocument() {
        assert bestGeniusURL != null;

        if (geniusDoc != null) {
            return geniusDoc;
        }

        try {
            geniusDoc = Jsoup.connect(bestGeniusURL)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                    .get();
            return geniusDoc;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Couldn't find the genius document.");
            return null;
        }
    }
}
