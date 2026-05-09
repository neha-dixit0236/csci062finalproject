import java.util.*;

/**
 * Most of the work for feature 3
 */
public class RecommendationEngine {
    private List<RecommendationSong> recommendationSongs;
    private List<KeyValuePair> listeningHistory;

    /**
     * Constructor for the RecommendationEngine class
     * @param recommendationSongs
     * @param listeningHistory
     */
    public RecommendationEngine (List<RecommendationSong> recommendationSongs, List<KeyValuePair> listeningHistory){
        this.recommendationSongs = recommendationSongs;
        this.listeningHistory = listeningHistory;   
    }

    /**
     * Putting in some recommended songs for each bucket/subcategory
     */
    public Map<String, List<RecommendationSong>> recommendSongs(List<Bucket> buckets){
        Map<String, List<RecommendationSong>> result = new HashMap<>();

        for (Bucket bucket : buckets){
            List<KeyValuePair> plays = bucket.getPlays();
            List<RecommendationSong> recommendations = new ArrayList<>();

            if (plays == null || plays.isEmpty()){
                result.put(bucket.getName(), recommendations);
                continue;
            }

            SongStatistics stats = new SongStatistics(plays);
            String topGenre = stats.getTopGenre();

            //building a list of songs that the user has already listened to
            List<String> listenedSongs = new ArrayList<>();

            for (KeyValuePair entry : listeningHistory){
                String songName = entry.getSongObject().getName().toLowerCase();
                listenedSongs.add(songName);
            }

            //traversing the recommended songs dataset
            for (RecommendationSong song : recommendationSongs){
                if (!song.getGenre().equalsIgnoreCase(topGenre)){
                    continue;
                }

                //check to see if the song has already been listened to
                boolean alreadyListened = false;
                for (String listenedSong : listenedSongs){
                    if (listenedSong.equalsIgnoreCase(song.getSongName())){
                        alreadyListened = true;
                        break;
                    }
                }

                if (alreadyListened){
                    continue;
                }

                recommendations.add(song);

                //making sure the list of recommended songs is not longer than 15 songs
                if (recommendations.size() == 15){
                    break;
                }
            }

            result.put(bucket.getName(), recommendations);
        }

        return result;

    }
    
}
