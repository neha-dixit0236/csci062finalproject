import java.sql.Timestamp;
public class KeyValuePair {

    private Timestamp timeStamp; //should this be string or long?
    private SongInfo songObject;

    public KeyValuePair (Timestamp timeStamp, SongInfo songObject){
        this.timeStamp = timeStamp;
        this.songObject = songObject;
    }
    //key value pair key should be a time stamp object instead of a string to allow for easier comparisons

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


    public String toString(){
        java.sql.Timestamp ts = getTimeStamp();
        String artist = songObject.getArtist();
        String name = songObject.getName();
        String genre = songObject.getGenre();
        return String.format("%-25s | %-20s | %-20s | %-20s", ts, artist, name, genre) + "\n";
    }
    
}
