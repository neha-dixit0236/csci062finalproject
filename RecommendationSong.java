/**
 * Represents a song that can be recommended to a user.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */

public class RecommendationSong {

    private String songName;
    private String artist;
    private String genre;

    /**
     * Constructs a RecommendationSong object.
     * 
     * @param songName the name of the song
     * @param artist the artist of the song
     * @param genre the genre of the song
     */
    public RecommendationSong(String songName, String artist, String genre) {
        this.songName = songName;
        this.artist = artist;
        this.genre = genre;
    }

    /**
     * Gets the name of the recommended song.
     * @return the song name
     */
    public String getSongName() {
        return songName;
    }

    /**
     * Gets the artist of the recommended song.
     * @return the artist name
     */
    public String getArtist() {
        return artist;
    }

    /**
     * Gets the genre of the recommended song.
     * @return the genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Returns a string representation of the recommended song.
     * @return formatted string describing the song
     */
    @Override
    public String toString() {
        return songName + " by " + artist + " (" + genre + ")";
    }
}