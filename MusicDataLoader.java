
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class MusicDataLoader {

    private List<KeyValuePair> listeningHistory;

    public static List<KeyValuePair> CSVAnalysis(String file){
        List<KeyValuePair> listeningHistory = new ArrayList<>();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null){
                
                //Splitting the CSV row into the parts we need
                String[] parts = line.split(",");

                //creating the KeyValuePair and SongInfo variables
                String timestamp = parts[0] + parts[1];

                String artist = parts[2];
                String songName = parts[3];
                String genre = parts[5];

                //creating the SongInfo Object
                SongInfo song = new SongInfo(artist, songName, genre);

                //creating the key value pair
                KeyValuePair newEntry = new KeyValuePair(timestamp, song);

                //adding this key value pair to the listening history list
                listeningHistory.add(newEntry);

                reader.close();
            }  
        }
        catch (Exception e){
            System.out.println("There was an error reading the CSV file: " + e.getMessage());
        }

        return listeningHistory;
    }

    public List<KeyValuePair> getListeningHistory(){ //might not need this?
        return listeningHistory;
    }
}

