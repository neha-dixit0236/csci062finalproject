
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;


public class MusicDataLoader {

    // Define the format to match "30 Sep 2023, 0:31"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, H:mm", Locale.ENGLISH);
    private List<KeyValuePair> listeningHistory;

    public static List<KeyValuePair> CSVAnalysis(String file){
        List<KeyValuePair> listeningHistory = new ArrayList<>();

        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null){

                // Use a regex to split by commas but ignore commas inside quotes
                // This is important because your date "30 Sep 2023, 0:31" has a comma in it!
                // THANKS TO LLM 
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                //creating the KeyValuePair and SongInfo variables
                //Process the timestamp
                // Remove the quotes around the date string: "30 Sep 2023, 0:31" -> 30 Sep 2023, 0:31
                String rawDate = parts[0].replace("\"", "").trim();
                Timestamp timestamp = convertToTimestamp(rawDate);

                String artist = parts[1];
                String songName = parts[3];
                String genre = parts[4];

                //creating the SongInfo Object
                SongInfo song = new SongInfo(artist, songName, genre);

                //creating the key value pair
                KeyValuePair newEntry = new KeyValuePair(timestamp, song);

                //adding this key value pair to the listening history list
                listeningHistory.add(newEntry);

            }  
            reader.close();
        }
        catch (Exception e){
            System.out.println("There was an error reading the CSV file: " + e.getMessage());
        }

        return listeningHistory;
    }

    //process date time to store the date time object with the song -> timestamp instead of a string is a date time object
    //helper method here
    private static Timestamp convertToTimestamp(String dateString) {
        LocalDateTime ldt = LocalDateTime.parse(dateString, FORMATTER);
        return Timestamp.valueOf(ldt);
    }

    public List<KeyValuePair> getListeningHistory(){ //might not need this?
        return listeningHistory;
    }

    public static void main(String[] args) {
        List<KeyValuePair> history = CSVAnalysis("testScrobbles.csv");
        for (KeyValuePair entry : history) {

        // Print formatted output to see if columns are aligned correctly
        System.out.println(entry.toString());
    }
    }
}

