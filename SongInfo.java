public class SongInfo {
    private String artist;
    private String songName;
    private String genre;

    public SongInfo(String artist, String songName, String genre) {
        this.artist = artist;
        this.songName = songName;
        this.genre = genre;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return songName;
    }

    public String getGenre() {
        return genre;
    }


}
