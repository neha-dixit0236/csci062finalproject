package BetterWrappedProject;
import java.time.LocalDate;

/**
 * A single outlier day flagged by OutlierDetector that stores the date, the top genre
 * on that day, and the baseline genre for that bucket, and number of plays on the day.
 * Helper class for feature 2
 * 
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */

public class OutlierDay {
    private LocalDate date; // we don't need LocalDateTime because for feature 2 we only care about the date
    private String dayGenre; // most played genre on that date
    private String baselineGenre; // most played genre across the bucket the date belongs to
    private String bucketName; // the bucket this date belongs to, defined from feature 1
    private int playCount; // number of plays happened on that date

    /**
     * Builds an OutlierDay object
     * 
     * @param date the date of the outlier day
     * @param dayGenre the most played genre on that date
     * @param baselineGenre the most played genre across the bucket
     * @param bucketName the name of the bucket this date belongs to
     * @param playCount the number of plays that occurred on that date
     */
    public OutlierDay(LocalDate date, String dayGenre, String baselineGenre, String bucketName, int playCount){
        this.date = date;
        this.dayGenre = dayGenre;
        this.baselineGenre = baselineGenre;
        this.bucketName = bucketName;
        this.playCount = playCount;
    }

    /**
     * Gets the date of the outlier.
     * @return the outlier date
     */
    public LocalDate getDate(){
        return date;
    }

    /**
     * Gets the most played genre on the outlier day.
     * @return the genre of the day
     */
    public String getDayGenre(){
        return dayGenre;
    }

    /**
     * Gets the baseline top genre for the bucket.
     * @return the baseline genre
     */
    public String getBaselineGenre(){
        return baselineGenre;
    }
    
    /**
     * Gets the name of the bucket this outlier belongs to.
     * @return the bucket name
     */
    public String getBucketName(){
        return bucketName;
    }

    /**
     * Gets the total number of plays on the outlier date.
     * @return the play count
     */
    public int getPlayCount(){
        return playCount;
    }

    /**
     * Returns a formatted string describing the outlier day.
     * @return the formatted string
     */
    @Override
    public String toString() {
        return date.toString() + ": played " + dayGenre + " " +
        playCount + " times, but usually " + baselineGenre + " during " + bucketName;
    }

}
