import java.sql.Timestamp;
public class KeyValuePair {

    private Timestamp timeStamp;
    private SongInfo songObject;

    public KeyValuePair (Timestamp timeStamp, SongInfo songObject){
        this.timeStamp = timeStamp;
        this.songObject = songObject;
    }

    public Timestamp getTimeStamp(){
        return timeStamp;
    }

    public SongInfo getSongObject(){
        return songObject;
    }

    public void setTimeStamp(Timestamp newTimeStamp){
        timeStamp = newTimeStamp;
    }

    public void setSongObject(SongInfo song){
        songObject = song;
    }


    @Override
    public String toString(){
        java.sql.Timestamp ts = getTimeStamp();
        String artist = songObject.getArtist();
        String name = songObject.getName();
        String genre = songObject.getGenre();
        return String.format("%-25s | %-20s | %-20s | %-20s", ts, artist, name, genre) + "\n";
    }

    //testing the keyvalue pair class
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


