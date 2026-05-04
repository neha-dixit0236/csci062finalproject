

//This will be part of feature 1. It takes in a data set and outputs summary statistics

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class SongStatistics {
    private String topArtist;
    private String topSong;
    private String topGenre;
    private double totalMinutes; //should we use this?

    public SongStatistics (List<KeyValuePair> list){
        if (list == null || list.isEmpty()) {
            this.topArtist = "None";
            this.topSong = "None";
            this.topGenre = "None";
            return;
        }

        Map<String, Integer> artistCounts = new HashMap<>();
        Map<String, Integer> songCounts = new HashMap<>();
        Map<String, Integer> genreCounts = new HashMap<>();

        //Fill in the value in hashMaps
        for (KeyValuePair entry : list) {
            String artist = entry.getSongObject().getArtist();
            String songName = entry.getSongObject().getName();
            String genre = entry.getSongObject().getGenre();

            //Update frequency counts
            artistCounts.put(artist, artistCounts.getOrDefault(artist, 0) + 1);
            songCounts.put(songName, songCounts.getOrDefault(songName, 0) + 1);
            genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
        }

        this.topArtist = findMax(artistCounts);
        this.topSong = findMax(songCounts);
        this.topGenre = findMax(genreCounts);


        }

    private String findMax(Map<String, Integer> counts) {
        if (counts.isEmpty()) {
            return "None";
        }

        Map.Entry<String, Integer> winner = Collections.max(counts.entrySet(), Map.Entry.comparingByValue());
        return winner.getKey();
    }


    public String toString(){
        // To be edited
        return String.format("Top Artist: %s\nTop Song: %s\nTop Genre: %s\n", 
                             topArtist, topSong, topGenre);
    }

    public static void main(String[] args) {
        System.out.println("--- Testing SongStatistics Frequency Analysis ---");

        // 1. Create dummy SongInfo objects
        SongInfo dua1 = new SongInfo("Dua Lipa", "Levitating", "Pop");
        SongInfo dua2 = new SongInfo("Dua Lipa", "Physical", "Pop");
        SongInfo weeknd = new SongInfo("The Weeknd", "Blinding Lights", "R&B");

        // 2. Create a dummy list of KeyValuePairs
        List<KeyValuePair> testList = new ArrayList<>();
    
        // We'll add Dua Lipa 3 times, The Weeknd 1 time
        // We'll add "Levitating" 2 times, "Physical" 1 time
        // Pop will have 3 counts, R&B will have 1
        Timestamp now = new Timestamp(System.currentTimeMillis());
    
        testList.add(new KeyValuePair(now, dua1));
        testList.add(new KeyValuePair(now, dua1)); // Repeat song
        testList.add(new KeyValuePair(now, dua2)); // Same artist, different song
        testList.add(new KeyValuePair(now, weeknd));

        // 3. Initialize SongStatistics
        // This will trigger your HashMap logic
        SongStatistics stats = new SongStatistics(testList);

        // 4. Verify Results
        System.out.println(stats.toString());

        

    }



    
}
