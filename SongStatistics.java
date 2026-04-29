

//This will be part of feature 1. It takes in a data set and outputs summary statistics
public class SongStatistics {
    private String topArtist;
    private String topSong;
    private String topGenre;
    private double totalMinutes; //should we use this?

    public SongStatistics (String topArtist, String topSong, String topGenre, double totalMinutes){
        this.topArtist = topArtist;
        this.topSong = topSong;
        this.topGenre = topGenre;
        this.totalMinutes = totalMinutes;
    }


    public String toString(){
        return "Top artist: " + topArtist + "\nTop song: " + topSong +"\nTop genre: " + topGenre + "\nMinutes listened: " + totalMinutes;
    }
























    
}
