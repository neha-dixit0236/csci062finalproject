import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */


/**
 * Main class to execute all features
 */
public class BetterWrapped {
    
    // threshold for a day to be counted as an outlier
    private static final int MIN_PLAYS_PER_DAY = 4;
    //list of all of the listening history: not sure how to upload the data set and squeeze it into this list
    private List<KeyValuePair> allHistory;

    /**
     * Builds a BetterWrapped2 object by loading listening history from a CSV
     * @param fileName path to the CSV file
     */
    public BetterWrapped(String fileName){
        this.allHistory = MusicDataLoader.CSVAnalysis(fileName);
    }

    //////////////////////////////////////////////////////////
    // FEATURE 1: Listening Trend Analysis
    //////////////////////////////////////////////////////////

    /**
     * The most important method to execute Feature 1.
     * @param comparisonType one of "WEEKDAY_VS_WEEKEND", "ONE_SEMESTER", and "FULL_YEAR"
     * @param midtermDates list of midterm deadline timestamps, used by ONE_SEMESTER
     * @param breakDates list of timestamps that bound the academic break, used by ONE_SEMESTER
     * @param springDates list of timestamps that bound the spring semester, used by FULL_YEAR
     * @param summerDates list of timestamps that bound the summer break, used by FULL_YEAR
     * @param fallDates list of timestamps that bound the fall semester, used by FULL_YEAR
     */
    public void analyze(String comparisonType, List<Timestamp> midtermDates, List<Timestamp> breakDates, List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates) {

        if (comparisonType.equals("WEEKDAY_VS_WEEKEND")){
            analyzeWeekdayVsWeekend();
        }
        else if (comparisonType.equals("ONE_SEMESTER")){
            analyzeSemester(midtermDates, breakDates);
        }
        else if (comparisonType.equals("FULL_YEAR")){
            analyzeYear(springDates, summerDates, fallDates);
        }
        else{
            System.out.println("Not a valid time window.");
        }
    }

    /**
     * Analyze listening history on weekdays (mon-fri) vs on weekends (sat-sun)
     */
    private void analyzeWeekdayVsWeekend(){
        List<Bucket> buckets = bucketWeekdayWeekend();
        printBucketStatistics(buckets);
    }

    /**
     * Helper method to bucket listening history into WEEKDAY and WEEKEND.
     * @return a list containing the weekday and weekend buckets
     */
    private List<Bucket> bucketWeekdayWeekend() {
        Bucket weekday = new Bucket("WEEKDAY");
        Bucket weekend = new Bucket("WEEKEND");

        for (KeyValuePair entry : allHistory) {
            DayOfWeek day = entry.getTimeStamp().toLocalDateTime().getDayOfWeek();

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekend.addPlay(entry);
            } 
            else {
                weekday.addPlay(entry);
            }
        }

        List<Bucket> result = new ArrayList<>();
        result.add(weekday);
        result.add(weekend);

        return result;
    }


    /**
     * Analyze listening history by midterm season, academic break, and normal days
     * @param midtermDates list of midterm deadline timestamps
     * @param breakDates list of timestamps describing the break window
     */
    private void analyzeSemester(List<Timestamp> midtermDates, List<Timestamp> breakDates){
        List<Bucket> buckets = bucketSemester(midtermDates, breakDates);
        printBucketStatistics(buckets);
    }

    /**
     * Helper method to bucket listening history into MIDTERM, BREAK, and NORMAL periods.
     * @param midtermDates list of midterm deadline timestamps
     * @param breakDates list of timestamps bounding the break
     * @return a list containing the categorized buckets
     */
    private List<Bucket> bucketSemester(List<Timestamp> midtermDates, List<Timestamp> breakDates){
        Bucket midterm = new Bucket("MIDTERM");
        Bucket academicBreak = new Bucket("BREAK");
        Bucket normal = new Bucket("NORMAL");

        for (KeyValuePair entry : allHistory){
            Timestamp songTime = entry.getTimeStamp();
            if (isWithinWindow(songTime, midtermDates, 5)){
                midterm.addPlay(entry);
            }
            else if (isWithinDateRange(songTime, breakDates)){
                academicBreak.addPlay(entry);
            }
            else{
                normal.addPlay(entry);
            }
        }

        List<Bucket> result = new ArrayList<>();

        result.add(midterm);
        result.add(academicBreak);
        result.add(normal);

        return result;
    }

    /**
     * Analyze a year's listening history by spring, summer, and fall semesters
     * @param springDates list of timestamps that bound the spring semester
     * @param summerDates list of timestamps that bound the summer break
     * @param fallDates list of timestamps that bound the fall semester
     */
    private void analyzeYear(List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates){
        List<Bucket> buckets = bucketYear(springDates, summerDates, fallDates);
        printBucketStatistics(buckets);
    }

    /**
     * Helper method to bucket listening history into SPRING, SUMMER, and FALL semesters.
     * @param springDates timestamps bounding the spring semester
     * @param summerDates timestamps bounding the summer semester
     * @param fallDates timestamps bounding the fall semester
     * @return a list containing the categorized buckets
     */
    private List<Bucket> bucketYear(List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates) {
        Bucket spring = new Bucket("SPRING");
        Bucket summer = new Bucket("SUMMER");
        Bucket fall = new Bucket("FALL");

        for (KeyValuePair entry : allHistory) {
            Timestamp songTime = entry.getTimeStamp();

            if (isWithinDateRange(songTime, springDates)) {
                spring.addPlay(entry);
            } else if (isWithinDateRange(songTime, summerDates)) {
                summer.addPlay(entry);
            } else if (isWithinDateRange(songTime, fallDates)) {
                fall.addPlay(entry);
            }
        }

        List<Bucket> result = new ArrayList<>();

        result.add(spring);
        result.add(summer);
        result.add(fall);

        return result;
    }


    /**
     * Helper method to print song statistics for a given list of buckets.
     * @param buckets the list of buckets to analyze and print
     */
    private void printBucketStatistics(List<Bucket> buckets) {
        for (Bucket bucket : buckets) {
            SongStatistics stats = new SongStatistics(bucket.getPlays());

            System.out.println(bucket.getName() + "STATS");
            System.out.println(stats);
        }
    } //neha's version of bucket helpers ends here

    //////////////////////////////////////////////////////////
    // helpers to determine the date range
    //////////////////////////////////////////////////////////

    /**
     * Checks if a song is played within the inclusive range from the earliest to the latest date in importantDates. 
     * For breaks and semesters of a year.
     * Assumes importantDates is a single continuous period, like only one break
     * @param songTime the timestamp of the song played
     * @param importantDates list of timestamps that bound a period of time
     * @return true if the song was played within the period of time
     */
    private boolean isWithinDateRange (Timestamp songTime, List<Timestamp> importantDates) {
        if (importantDates == null || importantDates.isEmpty()) {
            return false;
        }

        Timestamp start = Collections.min(importantDates);
        Timestamp end = Collections.max(importantDates);

        return !songTime.before(start) && !songTime.after(end);
    }

    /**
     * Checks if a song is played within n days before a deadline. For midterms.
     * @param songTime the timestamp of the song played
     * @param importantDates list of deadline timestamps
     * @param daysBefore how many days before each deadline count as part of the window
     * @return true if the song was played during the deadline "window"
     */
    private boolean isWithinWindow(Timestamp songTime, List<Timestamp> importantDates, int daysBefore){
        if (importantDates == null || importantDates.isEmpty()) {
            return false;
        }

        for (Timestamp targetDate: importantDates){
            LocalDateTime target = targetDate.toLocalDateTime();
            LocalDateTime startWindow = target.minusDays(daysBefore);

            Timestamp startTimestamp = Timestamp.valueOf(startWindow);

            if (!songTime.before(startTimestamp) && !songTime.after(targetDate)){
                return true;
            }
        }

        return false;
    } //what is the difference between iswithindaterange and iswithinwindow?

    //////////////////////////////////////////////////////////
    // Olivia: bucketing helpers for feature 1 and 2
    //////////////////////////////////////////////////////////

    /**
     * Bucketing helper for feature 1 and 2. Groups allHistory into WEEKDAY or WEEKEND buckets
     * @return map from bucket name to plays in that bucket
     */
    private Map<String, List<KeyValuePair>> bucketByWeekdayWeekend(){
        Map <String, List<KeyValuePair>> result = new LinkedHashMap<>();
        result.put("WEEKDAY", new ArrayList<>());
        result.put("WEEKEND", new ArrayList<>());

        for (KeyValuePair entry : allHistory) {
            Timestamp timestamp = entry.getTimeStamp();
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            DayOfWeek day = dateTime.getDayOfWeek();

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                result.get("WEEKEND").add(entry);
            } else {
                result.get("WEEKDAY").add(entry);
            }
        }
        return result;
    }

    /**
     * Bucketing helper for feature 1 and 2. Groups allHistory into MIDTERM, BREAK, or NORMAL buckets
     * @param midtermDates list of midterm deadline timestamps
     * @param breakDates list of timestamps describing the break window
     * @return map from bucket name to plays in that bucket
     */
    private Map<String, List<KeyValuePair>> bucketBySemester(List<Timestamp> midtermDates, List<Timestamp> breakDates){
        Map <String, List<KeyValuePair>> result = new LinkedHashMap<>();
        result.put("MIDTERM", new ArrayList<>());
        result.put("BREAK", new ArrayList<>());
        result.put("NORMAL", new ArrayList<>());

        for (KeyValuePair entry : allHistory) {
            Timestamp songTime = entry.getTimeStamp();

            // we assume the "window" starts from 5 days before midterm starts
            if (isWithinWindow(songTime, midtermDates, 5)){
                result.get("MIDTERM").add(entry);
            }
            else if (isWithinDateRange(songTime, breakDates)){
                result.get("BREAK").add(entry);
            }
            else{
                result.get("NORMAL").add(entry);
            }
        }
        return result;
    }

    /**
     * Bucketing helper for feature 1 and 2. Groups allHistory into SPRING, SUMMER, or FALL buckets
     * @param springDates list of timestamps that bound the spring semester
     * @param summerDates list of timestamps that bound the summer break
     * @param fallDates list of timestamps that bound the fall semester
     * @return map from bucket name to plays in that bucket
     */
    private Map<String, List<KeyValuePair>> bucketByYear(
            List<Timestamp> springDates, 
            List<Timestamp> summerDates, 
            List<Timestamp> fallDates) {

        Map <String, List<KeyValuePair>> result = new LinkedHashMap<>();
        result.put("SPRING", new ArrayList<>());
        result.put("SUMMER", new ArrayList<>());
        result.put("FALL", new ArrayList<>());

        for (KeyValuePair entry: allHistory){
            Timestamp songTime = entry.getTimeStamp();

            if (isWithinDateRange(songTime, springDates)){
                result.get("SPRING").add(entry);
            }
            else if (isWithinDateRange(songTime, summerDates)){
                result.get("SUMMER").add(entry);
            }
            else if (isWithinDateRange(songTime, fallDates)){
                result.get("FALL").add(entry);
            }
            // songs that are not in spring, summer, or fall windows are intentionally ignored
        }
        return result;
    }

    //////////////////////////////////////////////////////////
    // FEATURE 2: Detecting Outliers
    //////////////////////////////////////////////////////////

    /**
     * Execute feature 2 for weekday and weekend
     */
    public void detectOutliersByWeekdayWeekend(){
        List<Bucket> buckets = bucketWeekdayWeekend();
        runOutlierDetector(buckets);
    }

    /**
     * Execute feature 2 for midterm, break, and normal days
     */
    public void detectOutliersBySemester(List<Timestamp> midtermDates, List<Timestamp> breakDates){
        List<Bucket> buckets = bucketSemester(midtermDates, breakDates);
        runOutlierDetector(buckets);
    }

    /**
     * Execute feature 2 for spring, summer, and fall semester
     */
    public void detectOutliersByYear(List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates){
        List<Bucket> buckets = bucketYear(springDates, summerDates, fallDates);
        runOutlierDetector(buckets);
    }

    /**
     * Runs the outlier detection process for a given list of buckets and prints the results.
     * @param buckets the list of categorized buckets to scan for outliers
     */
    private void runOutlierDetector(List<Bucket> buckets) { // private or public????????
        OutlierDetector detect = new OutlierDetector(buckets, MIN_PLAYS_PER_DAY);
        List<OutlierDay> outliers = detect.findOutliers();
        detect.printOutliers(outliers);
    }

    //FEATURE 3 LAST FEATURE LAST FEATURE!!
    /**
     * Generates recommendations based on listening trends for weekday and weekend periods.
     * @param recommendationFile the CSV dataset file containing available songs to recommend
     */
    public void recommendByWeekdayWeekend(String recommendationFile){
        List<Bucket> buckets = bucketWeekdayWeekend();
        List<RecommendationSong> recommendationSongs = RecommendationLoader.loadSongs(recommendationFile);

        RecommendationEngine engine = new RecommendationEngine(recommendationSongs, allHistory);
        Map<String, List<RecommendationSong>> recommendations = engine.recommendSongs(buckets);
        engine.printRecommendations(recommendations);        
    }

    /**
     * Main method to test all BetterWrapped features.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        // 1. Create the instance
        BetterWrapped myWrapped = new BetterWrapped("testScrobbles.csv");

        // 2. Create sample test dates (Midterms on Sep 29, Break on Nov 24, and each semester starts at a reasonable time)
        Timestamp testMidterm = Timestamp.valueOf(LocalDateTime.of(2023, 9, 29, 23, 59));
        Timestamp testBreak = Timestamp.valueOf(LocalDateTime.of(2023, 11, 24, 23, 59));
        Timestamp testSpringStart = Timestamp.valueOf(LocalDateTime.of(2023,1, 19, 23, 59));
        Timestamp testSpringEnd = Timestamp.valueOf(LocalDateTime.of(2023,5, 15, 23, 59));
        Timestamp testSummerStart = Timestamp.valueOf(LocalDateTime.of(2023,5, 16, 23, 59));
        Timestamp testSummerEnd = Timestamp.valueOf(LocalDateTime.of(2023,8, 23, 23, 59));
        Timestamp testFallStart = Timestamp.valueOf(LocalDateTime.of(2023,8, 24, 23, 59));
        Timestamp testFallEnd = Timestamp.valueOf(LocalDateTime.of(2023,12, 11, 23, 59));

        // 3. Run the test for FEATURE 1 - Listening Trend Analysis

        // ----------WEEKDAY_VS_WEEKEND----------
        System.out.println("Testing WEEKDAY_VS_WEEKEND for Feature 1:");
        System.out.println("Both weekday and weekend are expected to have Dua Lipa as the top artist and Pop as the top genre");
        System.out.println(".");
        myWrapped.analyze("WEEKDAY_VS_WEEKEND", null, null, null, null, null);
        System.out.println("---------");

        // ----------ONE_SEMESTER----------
        System.out.println("Testing ONE_SEMESTER for Feature 1:");
        System.out.println("Those played on Sep 29 should be within the midterm window. Those played on Sep 30 should be within the normal days. Break should be empty");
        System.out.println(".");
        List<Timestamp> midtermDates = new ArrayList<>();
        midtermDates.add(testMidterm);
        List<Timestamp> breakDates = new ArrayList<>();
        breakDates.add(testBreak);
        myWrapped.analyze("ONE_SEMESTER", midtermDates, breakDates, null, null, null);
        System.out.println("---------");

        // ----------FULL_YEAR----------
        System.out.println("Testing FULL_YEAR for Feature 1:");
        System.out.println("All 26 plays should be within the fall semester window. Summer and spring should be empty");
        System.out.println(".");
        List<Timestamp> springDates = new ArrayList<>();
        springDates.add(testSpringStart);
        springDates.add(testSpringEnd);
        List<Timestamp> summerDates = new ArrayList<>();
        summerDates.add(testSummerStart);
        summerDates.add(testSummerEnd);
        List<Timestamp> fallDates = new ArrayList<>();
        fallDates.add(testFallStart);
        fallDates.add(testFallEnd);
        myWrapped.analyze("FULL_YEAR", null, null, springDates, summerDates, fallDates);
        System.out.println("---------");

        // 4. Run the test for FEATURE 2 - Detecting Outliers

        // ----------WEEKDAY_VS_WEEKEND----------
        System.out.println("Testing WEEKDAY_VS_WEEKEND for Feature 2:");
        myWrapped.detectOutliersByWeekdayWeekend();

        // ----------ONE_SEMESTER----------
        System.out.println("Testing ONE_SEMESTER for Feature 2:");
        myWrapped.detectOutliersBySemester(midtermDates, breakDates);

        // ----------FULL_YEAR----------
        System.out.println("Testing FULL_YEAR for Feature 2:");
        myWrapped.detectOutliersByYear(springDates, summerDates, fallDates);
        

    }   
        
} 
