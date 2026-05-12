package BetterWrappedProject;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;
import java.time.LocalDate;

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
     * INSERT JAVADOC HERE!!!!!
     * @return allHistory
     */
    public List<KeyValuePair> getAllHistory() {
        return allHistory;
    }

    /**
     * Analyze listening history on weekdays (mon-fri) vs on weekends (sat-sun)
     */
    private void analyzeWeekdayVsWeekend(){
        List<Bucket> buckets = bucketWeekdayWeekend();
        printBucketStatistics(buckets);
        compareBuckets(buckets);
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
        compareBuckets(buckets);
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
        compareBuckets(buckets);
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

            System.out.println(bucket.getName() + " STATS");
            System.out.println(stats);
        }
    } 

    //////////////////////////////////////////////////////////
    //FEATURE 1 COMPARISON HELPERS

    /**
    * Compares buckets and prints how listening behavior changes.
    * @param buckets list of categorized buckets
    */
    private void compareBuckets(List<Bucket> buckets){
        System.out.println("Listening Trend Comparison:");

        for (int i = 0; i < buckets.size(); i++){
            for (int j = i + 1; j < buckets.size(); j++){
                Bucket first = buckets.get(i);
                Bucket second = buckets.get(j);

                compareTwoBuckets(first, second);
            }
        }
        System.out.println();
    }

    /**
    * Compare two listening buckets.
    * @param first first bucket
    * @param second second bucket
    */
    private void compareTwoBuckets(Bucket first, Bucket second){
        SongStatistics stats1 = new SongStatistics(first.getPlays());

        SongStatistics stats2 = new SongStatistics(second.getPlays());

        System.out.println(first.getName() + " vs " + second.getName());

        compareGenres(stats1, stats2);
        compareArtists(stats1, stats2);
        compareSongs(stats1, stats2);

        System.out.println("---------");
    }

    /**
    * Compare the top genres between two listening time windows
    * @param stats1 Song statistics of the first time window
    * @param stats2 Song statistics of the second time window
    */
    private void compareGenres(SongStatistics stats1, SongStatistics stats2){
        String genre1 = stats1.getTopGenre();
        String genre2 = stats2.getTopGenre();
        if (genre1.equals("None") || genre2.equals("None")){
            System.out.println("Not enough data to compare genres.");
        }
        else if (genre1.equals(genre2)){
            System.out.println("Your top genre stayed consistent. Your top genre was: " + genre1);
        }
        else{
            System.out.println("Your top genre shifted from " + genre1 + " to " + genre2);
        }
    }

    /**
    * Compare the top artists between two listening time windows
    * @param stats1 Song statistics of the first time window
    * @param stats2 Song statistics of the second time window
    */
    private void compareArtists(SongStatistics stats1, SongStatistics stats2){
        String artist1 = stats1.getTopArtist();
        String artist2 = stats2.getTopArtist();

        if (artist1.equals("None") || artist2.equals("None")){
            System.out.println("Not enough data to compare artists.");
        }
        else if (artist1.equals(artist2)){
            System.out.println("Favorite artist stayed the same: " + artist1);
        }
        else{
            System.out.println("Favorite artist changed from " + artist1 + " to " + artist2);
        }
    }

    /**
    * Compare the top songs between the two listening windows.
    * @param stats1 Song statistics of the first time window
    * @param stats2 Song statistics of the second time window
    */
    private void compareSongs(SongStatistics stats1, SongStatistics stats2){
        String song1 = stats1.getTopSong();
        String song2 = stats2.getTopSong();
        if (song1.equals("None") || song2.equals("None")){
            System.out.println("Not enough data to compare songs.");
        }
        else if (song1.equals(song2)){
            System.out.println("Top song stayed consistent: " + song1);
        }
        else{
            System.out.println("Top song changed from " + song1 + " to " + song2);
        }
    }


    //////////////////////////////////////////////////////////
    // Feature 1 helpers to determine the date range
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
        if (importantDates == null || importantDates.size() < 2) {
            return false;
        }

        for (int i = 0; i < importantDates.size() - 1; i +=2){
            Timestamp start = importantDates.get(i);
            Timestamp end = importantDates.get(i+1);

            if (!songTime.before(start) && !songTime.after(end)){
                return true;
            }
        }

        return false;
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










    //////////////////////////////////////////////////////////
    //FEATURE 3: Personalized Recommendations Based on Genre
    //////////////////////////////////////////////////////////


    /**
     * Generates recommendations based on listening trends for weekday and weekend periods.
     * @param recommendationFile the CSV dataset file containing available songs to recommend
     */
    public void recommendByWeekdayWeekend(String recommendationFile){
        List<Bucket> buckets = bucketWeekdayWeekend();  
        runRecommendations(buckets, recommendationFile);
    }

    public void recommendBySemester(List<Timestamp> midtermDates, List<Timestamp> breakDates, String recommendationFile){
        List<Bucket> buckets = bucketSemester(midtermDates, breakDates);
        runRecommendations(buckets, recommendationFile);
    }

    public void recommendByYear(List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates, String recFile){
        List<Bucket> buckets = bucketYear(springDates, summerDates, fallDates);

        runRecommendations(buckets, recFile);
    }



    //helper method for feature 3
    private void runRecommendations(List<Bucket> buckets, String recFile){
        List<RecommendationSong> recommendationSongs = RecommendationLoader.loadSongs(recFile);
        RecommendationEngine recEngine = new RecommendationEngine(recommendationSongs, allHistory);

        Map<String, List<RecommendationSong>> recommendedSongs = recEngine.recommendSongs(buckets);

        recEngine.printRecommendations(recommendedSongs);
    }



    /**
     * Main method to test all BetterWrapped features.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        BetterWrapped userWrapped = null;

        System.out.println("=========================================");
        System.out.println("Welcome to Better Wrapped Interactive!");
        System.out.println("=========================================");

        //Creating our recommendation file variable
        String recommendationFile = "src/BetterWrappedProject/MasterListofSongs(Feature3).csv";
        String userWindow = "";

        //Asking the user for a valid time window
        while (true){
            System.out.print("What time window would you like to analyze? (WEEKDAY_VS_WEEKEND, ONE_SEMESTER, FULL_YEAR): ");
            userWindow = scanner.nextLine().trim().toUpperCase();

            if (userWindow.equals("WEEKDAY_VS_WEEKEND") || userWindow.equals("ONE_SEMESTER") || userWindow.equals("FULL_YEAR")) {
                break;
            }
            else{
                System.out.println("This is not a valid time window. Please type one of the following: WEEKDAY_VS_WEEKEND, ONE_SEMESTER, FULL_YEAR ");
            }
        }

        //Creating variables for our date lists
        List<Timestamp> midtermDates = new ArrayList<>();
        List<Timestamp> breakDates = new ArrayList<>();
        List<Timestamp> springDates = new ArrayList<>();
        List<Timestamp> summerDates = new ArrayList<>();
        List<Timestamp> fallDates = new ArrayList<>();


        // ===================================
        // WEEKDAY_VS_WEEKEND
        // ===================================
        if (userWindow.equals("WEEKDAY_VS_WEEKEND")){
            userWrapped = new BetterWrapped("src/BetterWrappedProject/ScrobblesForOneWeek.csv");
        }

        // ===================================
        // ONE_SEMESTER -
        // ===================================
        if (userWindow.equals("ONE_SEMESTER")) {
            userWrapped = new BetterWrapped("src/BetterWrappedProject/ScrobblesForOneSemester.csv"); //so would i just change the name here if i'm assuming the user already has their csv uploaded properly?

            //figuring out the year
            int detectedYear = userWrapped.getAllHistory().get(0).getTimeStamp().toLocalDateTime().getYear();
            System.out.println("\nDetected listening history year: " + detectedYear);
            
            //creating the midterm/final list
            int numMidterms = 0;

            while (true) {
                try {
                    System.out.print( "\nHow many midterms/finals would you like to enter? ");
                    numMidterms = Integer.parseInt(scanner.nextLine().trim());
                
                    if (numMidterms >= 0) {
                        break;
                    }

                } 
                catch (Exception e) {
                    // ignore
                }

                System.out.println("Please enter a valid number. ");
            }

            for (int i = 1; i <= numMidterms; i++) {
                while (true) {
                    try {
                        System.out.print("Enter midterm/final #" + i + " date (MM-DD): ");

                        String input = scanner.nextLine().trim();
                        String[] parts = input.split("-");
                            
                        int month = Integer.parseInt(parts[0]);
                        int day = Integer.parseInt(parts[1]);
                            
                        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.of(detectedYear, month, day, 23, 59));
                        midtermDates.add(timestamp);
                        break;
                    } 
                    catch (Exception e) {
                        System.out.println("Invalid format. Please use MM-DD. Example: 09-29 ");
                    }
                }
            }


            // --------------------------
            // Multiple breaks
            // --------------------------

            int numBreaks = 0;

            while (true) {
                try {

                    System.out.println("How many breaks do you have? ");

                    numBreaks = Integer.parseInt(scanner.nextLine().trim());

                    if (numBreaks >= 0) {
                        break;
                    }
                } 
                catch (Exception e) {
                    //nothing happens
                }

                System.out.println("Please enter a valid number. ");
            }

            // enter each break
            for (int i = 1; i <= numBreaks; i++) {
                while (true) {
                    try {
                        System.out.print( "\nEnter BREAK #" + i + " START date (MM-DD): ");

                        String startInput = scanner.nextLine().trim();

                        String[] startParts = startInput.split("-");
                            

                        int startMonth = Integer.parseInt(startParts[0]);

                        int startDay = Integer.parseInt(startParts[1]);

                        System.out.print("Enter BREAK #" + i + " END date (MM-DD): ");

                        String endInput = scanner.nextLine().trim();
                            

                        String[] endParts = endInput.split("-");
                            

                        int endMonth = Integer.parseInt(endParts[0]);
                        int endDay = Integer.parseInt(endParts[1]);

                        Timestamp breakStart = Timestamp.valueOf(LocalDateTime.of(detectedYear, startMonth, startDay, 0, 0));

                        Timestamp breakEnd = Timestamp.valueOf(LocalDateTime.of(detectedYear, endMonth, endDay, 23, 59));
                        
                        // add as pair
                        breakDates.add(breakStart);
                        breakDates.add(breakEnd);

                        break;

                    } 
                    catch (Exception e) {

                        System.out.println("Invalid format. Please use MM-DD. Example: 11-20 ");
                    }
                }
            }
        }



        // ===================================
        // FULL YEAR -
        // ===================================

        if (userWindow.equals("FULL_YEAR")) {
            userWrapped = new BetterWrapped("src/BetterWrappedProject/ScrobblesForOneYear.csv");
            int detectedYear = userWrapped.getAllHistory().get(0).getTimeStamp().toLocalDateTime().getYear();
            
            springDates.add(Timestamp.valueOf(LocalDateTime.of(detectedYear, 1, 1, 0, 0)));
            springDates.add(Timestamp.valueOf(LocalDateTime.of(detectedYear, 4, 30, 23, 59)));
            
            summerDates.add(Timestamp.valueOf(LocalDateTime.of(detectedYear, 5, 1, 0, 0)));
            summerDates.add(Timestamp.valueOf(LocalDateTime.of(detectedYear, 8, 31, 23, 59)));

            fallDates.add(Timestamp.valueOf( LocalDateTime.of(detectedYear, 9, 1, 0, 0)));
            fallDates.add(Timestamp.valueOf(LocalDateTime.of(detectedYear, 12, 31, 23, 59)));
        }



        // ===================================
        // Generate Better Wrapped
        // ===================================

        System.out.println("\nGenerating your Better Wrapped...");

        if (userWindow.equals("WEEKDAY_VS_WEEKEND")) {
            userWrapped.analyze(userWindow, null, null, null, null, null);
            userWrapped.detectOutliersByWeekdayWeekend();
            userWrapped.recommendByWeekdayWeekend(recommendationFile);
        } 
        else if (userWindow.equals("ONE_SEMESTER")) {
            userWrapped.analyze(userWindow, midtermDates, breakDates, null, null, null);

            userWrapped.detectOutliersBySemester(midtermDates, breakDates);
            userWrapped.recommendBySemester(midtermDates, breakDates, recommendationFile);
        } 
        else {
            userWrapped.analyze(userWindow, null, null, springDates, summerDates, fallDates);
            userWrapped.detectOutliersByYear(springDates, summerDates, fallDates);
            userWrapped.recommendByYear(springDates, summerDates, fallDates, recommendationFile);
        }

        scanner.close();
    }   
} 
