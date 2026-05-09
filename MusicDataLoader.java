
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for loading music listening history from a CSV file.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */
public class MusicDataLoader {

    // Define the format to match "30 Sep 2023, 0:31"
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, H:mm", Locale.ENGLISH);
    private List<KeyValuePair> listeningHistory;

    /**
     * Reads and parses a CSV file into a list of listening history records.
     * 
     * @param file the path to the CSV file
     * @return a list of KeyValuePair objects representing the listening history
     */
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

                String artist = parts[1].trim();
                String songName = parts[3].trim();
                String genre = parts[4].trim();

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

    /**
     * Converts a formatted date string to a Timestamp object.
     * 
     * @param dateString the formatted date string to parse
     * @return the parsed Timestamp object
     */
    private static Timestamp convertToTimestamp(String dateString) {
        LocalDateTime ldt = LocalDateTime.parse(dateString, FORMATTER);
        return Timestamp.valueOf(ldt);
    }

    /**
     * Gets the listening history.
     * 
     * @return the listening history list
     */
    public List<KeyValuePair> getListeningHistory(){ //might not need this?
        return listeningHistory;
    }

    /**
     * Main method to test data loading functionality.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        List<KeyValuePair> history = CSVAnalysis("testScrobbles.csv");
        for (KeyValuePair entry : history) {

        // Print formatted output to see if columns are aligned correctly
        System.out.println(entry.toString());
    }
    }
}
