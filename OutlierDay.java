import java.time.LocalDate;

/**
 * A single outlier day flagged by OutlierDetector that stores the date, the top genre
 * on that day, and the baseline genre for that bucket, and number of plays on the day.
 * Helper class for feature 2
 */
public class OutlierDay {
    private LocalDate date; // we don't need LocalDateTime because for feature 2 we only care about the date
    private String dayGenre; // most played genre on that date
    private String baselineGenre; // most played genre across the bucket the date belongs to
    private String bucketName; // the bucket this date belongs to, defined from feature 1
    private int playCount; // number of plays happened on that date

    /**
     * Builds an OutlierDay object
     */
    public OutlierDay(LocalDate date, String dayGenre, String baselineGenre, String bucketName, int playCount){
        this.date = date;
        this.dayGenre = dayGenre;
        this.baselineGenre = baselineGenre;
        this.bucketName = bucketName;
        this.playCount = playCount;
    }

    public LocalDate getDate(){
        return date;
    }

    public String getDayGenre(){
        return dayGenre;
    }

    public String getBaselineGenre(){
        return baselineGenre;
    }
    
    public String getBucketName(){
        return bucketName;
    }

    public int getPlayCount(){
        return playCount;
    }

    /**
     * Overrides toString so it prints out nicely
     */
    @Override
    public String toString() {
        return date.toString() + ": played " + dayGenre + " " +
        playCount + " times, but usually " + baselineGenre + " during " + bucketName;
    }

}
