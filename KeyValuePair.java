public class KeyValuePair {

    private String timeStamp; //shoud this be string or long?
    private SongInfo songObject;

    public KeyValuePair (String timeStamp, SongInfo songObject){
        this.timeStamp = timeStamp;
        this.songObject = songObject;
    }

    public String getTimeStamp(){
        return timeStamp;
    }

    public SongInfo getSongObject(){
        return songObject;
    }

    public void setTimeStamp(String newTimeStamp){
        timeStamp = newTimeStamp;
    }

    public void setSongObject(SongInfo song){
        songObject = song;
    }

    public String toString(){
        return "Timestamp: " + timeStamp + "\nSong: " + songObject.toString();
    }
    
}
