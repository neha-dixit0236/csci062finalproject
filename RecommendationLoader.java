import java.io.*;
import java.util.*;

/**
 * Loads all the recommendation songs from the CSV
 */
public class RecommendationLoader {
    
    /**
     * The CSV format is "track number,track,artist name,genre"
     */
    public static List<RecommendationSong> loadSongs(String fileName){
        List<RecommendationSong> songs = new ArrayList<>();

        try{
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            
            //skipping the header row
            br.readLine();

            String line;

            while ((line = br.readLine()) != null){
                String[] parts = line.split(",");
                //we need at least 4 columns
                if (parts.length < 4){
                    continue;
                }

                //make the indices based on the csv
                String songName = parts[1].trim();
                String artist = parts[2].trim();
                String genre = parts[3].trim();

                RecommendationSong song = new RecommendationSong(songName, artist, genre);
                songs.add(song);
            }

            br.close();
        }
        catch (IOException e){
            System.out.println("There was an error reading the recommendation dataset.");
        }

        return songs;
    }
}