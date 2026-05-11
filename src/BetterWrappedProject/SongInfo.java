package BetterWrappedProject;
/**
 * Represents the metadata (artist, name, genre) for a given song.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */
public class SongInfo {
    private String artist;
    private String songName;
    private String genre;

    /**
     * Constructs a SongInfo object.
     * 
     * @param artist the artist of the song
     * @param songName the name of the song
     * @param genre the genre of the song
     */
    public SongInfo(String artist, String songName, String genre) {
        this.artist = artist;
        this.songName = songName;
        this.genre = genre;
    }

    /**
     * Gets the artist of the song.
     * @return the artist name
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Gets the name of the song.
     * @return the song name
     */
    public String getName() {
        return songName;
    }

    /**
     * Gets the genre of the song.
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }


}
