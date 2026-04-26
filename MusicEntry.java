public class MusicEntry {
    private long timestamp;
    private SongInfo song;

    public MusicEntry(long timestamp, SongInfo song) {
        this.timestamp = timestamp;
        this.song = song;
    }

    public long getTimeStamp() {
        return timestamp;
    }

    public SongInfo getSong() {
        return song;
    }
}
