public class RecommendationSong {

    private String songName;
    private String artist;
    private String genre;

    public RecommendationSong(String songName, String artist, String genre) {
        this.songName = songName;
        this.artist = artist;
        this.genre = genre;
    }

    public String getSongName() {
        return songName;
    }

    public String getArtist() {
        return artist;
    }

    public String getGenre() {
        return genre;
    }

    @Override
    public String toString() {
        return songName + " by " + artist + " (" + genre + ")";
    }
}