import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;

public class SongStatistics {
    private String topArtist;
    private String topSong;
    private String topGenre;

    public SongStatistics (List<KeyValuePair> list){
        if (list == null || list.isEmpty()) {
            this.topArtist = "None";
            this.topSong = "None";
            this.topGenre = "None";
            return;
        }

        //creating hash maps for to count how many times each artist/song/genre shows up in the listening history
        HashMap<String, Integer> artistCounts = new HashMap<>();
        HashMap<String, Integer> songCounts = new HashMap<>();
        HashMap<String, Integer> genreCounts = new HashMap<>();

        for (KeyValuePair entry : list){
            SongInfo song = entry.getSongObject();

            String artist = song.getArtist();
            String songName = song.getName();
            String genre = song.getGenre();

            //Update frequency counts
            artistCounts.put(artist, artistCounts.getOrDefault(artist, 0) + 1);
            songCounts.put(songName, songCounts.getOrDefault(songName, 0) + 1);
            genreCounts.put(genre, genreCounts.getOrDefault(genre, 0) + 1);
        }

        this.topArtist = findMax(artistCounts);
        this.topSong = findMax(songCounts);
        this.topGenre = findMax(genreCounts);        
    }

    private String findMax(HashMap<String, Integer> map){
        String winner = "None";
        int max = 0;

        for (Map.Entry<String, Integer> entry : map.entrySet()){
            if (entry.getValue() > max){
                max = entry.getValue();
                winner = entry.getKey();
            }
        }

        return winner;

    }

    public String getTopGenre(){
        return topGenre;
    }

    public String getTopArtist() {
        return topArtist;
    }

    public String getTopSong(){
        return topSong;
    }

    @Override
    public String toString() {

        return "Top Artist: " + topArtist + "\nTop Song: " + topSong + "\nTop Genre: " + topGenre + "\n";
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
