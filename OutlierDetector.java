import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main class for Feature 2 - detecting outliers
 */
public class OutlierDetector {
    // Define outlier:
    // 1. a day is an outlier if its dominant genre differs from the user's overall dominant genre
    // 2. a day is an outlier where the user listened way more or way less than usual -> should we do this tho?
    // 3. In a specific category (e.g. normal days), a day is an outlier if there are tracks played at a frequency significantly higher than 
    // the average for that period. -> should we do this???

    // we need an established period of time: for each feature 1 bucket?

    private Map<String,List<KeyValuePair>> bucketHistory;
    private int minPlaysPerday = 3; // minimum plays per day for it to be flagged as an outlier (like a threshold)

    /**
     * Builds an OutlierDetector object for one bucket's listening history
     * @param bucketHistory map from bucket (e.g. MIDTERM, WEEKDAY) to the songs played in that bucket
     */
    public OutlierDetector(Map<String,List<KeyValuePair>> bucketHistory) {

    }

    /**
     * For every bucket, find the bucket's top genre (baseline) and group the plays by date
     * @return dates whose most played genre doesn't match the baseline
     */
    public List<OutlierDay> findOutlier() {
        return null;

    }

    /**
     * Prints a human-readable result of outlier days and grouped by bucket
     * @param outliers the list returned by findOutlier method
     */
    public void printOutliers (List<OutlierDay> outliers) {

    }



}