package BetterWrappedProject;
import java.sql.Timestamp;

/**
 * Represents a single play event, associating a timestamp with a played song.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */
public class KeyValuePair {

    private Timestamp timeStamp;
    private SongInfo songObject;

    /**
     * Constructs a KeyValuePair for a listening event.
     * 
     * @param timeStamp the time the song was played
     * @param songObject the information of the played song
     */
    public KeyValuePair (Timestamp timeStamp, SongInfo songObject){
        this.timeStamp = timeStamp;
        this.songObject = songObject;
    }

    /**
     * Gets the timestamp of the play event.
     * 
     * @return the timestamp
     */
    public Timestamp getTimeStamp(){
        return timeStamp;
    }

    /**
     * Gets the song information of the play event.
     * 
     * @return the song object
     */
    public SongInfo getSongObject(){
        return songObject;
    }

    /**
     * Sets the timestamp of the play event.
     * 
     * @param newTimeStamp the new timestamp to set
     */
    public void setTimeStamp(Timestamp newTimeStamp){
        timeStamp = newTimeStamp;
    }

    /**
     * Sets the song information of the play event.
     * 
     * @param song the new song object to set
     */
    public void setSongObject(SongInfo song){
        songObject = song;
    }

    /**
     * Returns a formatted string representation of the play event.
     * 
     * @return the formatted string
     */
    @Override
    public String toString(){
        java.sql.Timestamp ts = getTimeStamp();
        String artist = songObject.getArtist();
        String name = songObject.getName();
        String genre = songObject.getGenre();
        return String.format("%-25s | %-20s | %-20s | %-20s", ts, artist, name, genre) + "\n";
    }

    /**
     * Main method to test the KeyValuePair class.
     * 
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args){
        // Create a sample SongInfo object
        SongInfo song = new SongInfo("The Weeknd", "Blinding Lights", "Pop");

        // Create a timestamp
        Timestamp ts = Timestamp.valueOf("2026-05-08 12:30:00");

        // Create the KeyValuePair object
        KeyValuePair pair = new KeyValuePair(ts, song);

        // Test the toString() method
        System.out.println(pair.toString());
    }

    
}
