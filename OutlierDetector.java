import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Main class to execute Feature 2 - detecting outliers
 */
public class OutlierDetector {
    // a day is an outlier if its dominant genre differs from the user's overall dominant genre in an
    // established period of time (for each feature 1 bucket, like midterm, normal, weekday, etc)

    private Map<String,List<KeyValuePair>> bucketHistory;
    private int minPlaysPerDay = 4; // minimum plays per day for it to be flagged as an outlier (like a threshold)

    /**
     * Builds an OutlierDetector object for one's listening history, for all buckets
     * @param bucketHistory map from bucket (e.g. MIDTERM, WEEKDAY) to the songs played in that bucket
     */
    public OutlierDetector(Map<String,List<KeyValuePair>> bucketHistory) {
        this.bucketHistory = bucketHistory;
    }

    /**
     * For every bucket, find the bucket's top genre (baseline) and group the plays by date to find outlier days
     * @return dates whose most played genre doesn't match the baseline
     */
    public List<OutlierDay> findOutliers() {
        List<OutlierDay> result = new ArrayList<>();

        // for each bucket
        for (Map.Entry<String, List<KeyValuePair>> bucket : bucketHistory.entrySet()) {
            String bucketName = bucket.getKey();
            List<KeyValuePair> plays = bucket.getValue();
            
            // when nothing is played on that day
            if (plays.isEmpty() || plays == null) {
                continue;
            }

            // top genre of that bucket
            String baselineGenre = new SongStatistics(plays).getTopGenre();

            // group a list of plays by date, then sort dates
            Map<LocalDate, List<KeyValuePair>> groupedMap = groupByDate(plays);
            List<LocalDate> dateSorted = new ArrayList<>(groupedMap.keySet());
            Collections.sort(dateSorted);

            // for each date
            for (LocalDate date : dateSorted) {
                List<KeyValuePair> dayPlays = groupedMap.get(date);
                
                // the number of plays is below our threshold (4)
                if (dayPlays.size() < minPlaysPerDay) {
                    continue;
                }

                // top genre of that day
                String dayGenre = new SongStatistics(dayPlays).getTopGenre();

                // if the top genre of that day is not the same as the top genre of that bucket
                if (!dayGenre.equals(baselineGenre)) {
                    OutlierDay od = new OutlierDay(date, dayGenre, baselineGenre, bucketName, dayPlays.size());
                    result.add(od);
                }
            }
        }

        return result;
    }

    /**
     * Group a list of plays by date. Helper for findOutliers
     * @param plays number of plays from one bucket
     * @return map from date to songs played on that day
     */
    private Map<LocalDate, List<KeyValuePair>> groupByDate (List<KeyValuePair> plays) {
        Map<LocalDate, List<KeyValuePair>> groupedMap = new LinkedHashMap<>();

        // for each song played
        for (KeyValuePair entry : plays) {
            LocalDate key = entry.getTimeStamp().toLocalDateTime().toLocalDate(); // key
            List<KeyValuePair> list = groupedMap.get(key); // value
            
            if (list == null) {
                // make a new list and add the date and the list of plays as a key-value pair to groupedMap
                list = new ArrayList<>();
                groupedMap.put(key, list);
            }

            list.add(entry); // add that play to the list of plays on that day
        }
        return groupedMap;
    }

    /**
     * Prints a human-readable result of outlier days and grouped by bucket
     * @param outliers the list returned by findOutlier method
     */
    public void printOutliers (List<OutlierDay> outliers) {
        System.out.println("Outlier Days:");

        if (outliers == null || outliers.isEmpty()) {
            System.out.println("Did not detect any outlier days");
            return;
        }

        // group printed things by bucket
        String currentBucket = null;
        for (OutlierDay outlier : outliers) {

            if (!outlier.getBucketName().equals(currentBucket)) {
                currentBucket = outlier.getBucketName();
                System.out.println("-----" +currentBucket + "-----");
            }

            System.out.println(outlier);
        }

    }

    // ------------------Testing------------------

    /**
     * Helper method for testing by "playing" something on a specific date with a genre of that song
     */
    private static KeyValuePair playHelper (int year, int month, int day, String genre) {
        Timestamp ts = Timestamp.valueOf(LocalDateTime.of(year, month, day, 23, 59));
        SongInfo si = new SongInfo("Artist", "Song Name", genre);
        return new KeyValuePair(ts, si);
    }

    /**
     * Helper method for adding a number of plays of a genre into a list on an given date
     */
    private static void addPlaysHelper (List<KeyValuePair> list, int year, int month, int day, String genre, int count) {
        for (int i = 0; i < count; i++) {
            list.add(playHelper(year, month, day, genre));
        }
    }

    public static void main(String[] args) {
        // ------------------Test 1: No Outliers------------------
        System.out.println("Test 1: bucket has the same genre so no outliers");

    }



}