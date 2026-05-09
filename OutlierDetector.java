import java.time.LocalDate;
import java.time.LocalDateTime;
import java.sql.Timestamp;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Class for feature 2 - detecting outliers
 */
public class OutlierDetector {
    // a day is an outlier if its dominant genre differs from the user's overall dominant genre in an
    // established period of time (for each feature 1 bucket, like midterm, normal, weekday, etc)

    private List<Bucket> buckets;
    private int minPlaysPerDay; // minimum plays per day for it to be flagged as an outlier (like a threshold)

    /**
     * Builds an OutlierDetector object for one's listening history, for all buckets
     * @param bucketHistory map from bucket (e.g. MIDTERM, WEEKDAY) to the songs played in that bucket
     */
    public OutlierDetector(List<Bucket> bucketHistory, int minPlaysPerDay) {
        this.buckets = bucketHistory;
        this.minPlaysPerDay = minPlaysPerDay;
    } //might have to change comment on this

    public List<OutlierDay> findOutliers(){
        List<OutlierDay> outliers = new ArrayList<>();

        for (Bucket bucket : buckets){
            List<KeyValuePair> plays = bucket.getPlays();

            if (plays == null || plays.isEmpty()){
                continue;
            }

            String baselineGenre = new SongStatistics(plays).getTopGenre();
            List<LocalDate> checkedDates = new ArrayList<>();

            for (KeyValuePair entry : plays){
                LocalDate date = entry.getTimeStamp().toLocalDateTime().toLocalDate();

                if (checkedDates.contains(date)){
                    continue;
                }

                checkedDates.add(date);

                List<KeyValuePair> oneDay = new ArrayList<>();

                for (KeyValuePair other : plays){
                    LocalDate otherDate = other.getTimeStamp().toLocalDateTime().toLocalDate();

                    if (otherDate.equals(date)){
                        oneDay.add(other);
                    }
                }

                if (oneDay.size() < minPlaysPerDay){
                    continue;
                }

                String dayGenre = new SongStatistics(oneDay).getTopGenre();

                if (!dayGenre.equals(baselineGenre)){
                    OutlierDay outlier = new OutlierDay(date, dayGenre, baselineGenre, bucket.getName(), oneDay.size());

                    outliers.add(outlier);
                }
            }
        }

        return outliers;
    }


    /**
     * For every bucket, find the bucket's top genre (baseline) and group the plays by date to find outlier days
     * @return dates whose most played genre doesn't match the baseline
     */
    //public List<OutlierDay> findOutliers() {
        //List<OutlierDay> result = new ArrayList<>();

        // for each bucket
        //for (Map.Entry<String, List<KeyValuePair>> bucket : bucketHistory.entrySet()) {
            //String bucketName = bucket.getKey();
            //List<KeyValuePair> plays = bucket.getValue();
            
            // when nothing is played on that day
            //if (plays.isEmpty() || plays == null) {
               // continue;
            //}

            // top genre of that bucket
            //String baselineGenre = new SongStatistics(plays).getTopGenre();

            // group a list of plays by date, then sort dates
            //Map<LocalDate, List<KeyValuePair>> groupedMap = groupByDate(plays);
            //List<LocalDate> dateSorted = new ArrayList<>(groupedMap.keySet());
            //Collections.sort(dateSorted);

            // for each date
            //for (LocalDate date : dateSorted) {
                //List<KeyValuePair> dayPlays = groupedMap.get(date);
                
                // the number of plays is below our threshold (4)
                //if (dayPlays.size() < minPlaysPerDay) {
                //    continue;
                //}

                // top genre of that day
 //               String dayGenre = new SongStatistics(dayPlays).getTopGenre();

                // if the top genre of that day is not the same as the top genre of that bucket
   //             if (!dayGenre.equals(baselineGenre)) {
     //               OutlierDay od = new OutlierDay(date, dayGenre, baselineGenre, bucketName, dayPlays.size());
      //              result.add(od);
   //             }
    //        }
     //   }

     //   return result;
    //}

    /**
     * Group a list of plays by date. Helper for findOutliers
     * @param plays number of plays from one bucket
     * @return map from date to songs played on that day
     */
    //private Map<LocalDate, List<KeyValuePair>> groupByDate (List<KeyValuePair> plays) {
       // Map<LocalDate, List<KeyValuePair>> groupedMap = new LinkedHashMap<>();

        // for each song played
        //for (KeyValuePair entry : plays) {
           // LocalDate key = entry.getTimeStamp().toLocalDateTime().toLocalDate(); // key
            //List<KeyValuePair> list = groupedMap.get(key); // value
            
            //if (list == null) {
                // make a new list and add the date and the list of plays as a key-value pair to groupedMap
            //    list = new ArrayList<>();
            //    groupedMap.put(key, list);
            //}

            //list.add(entry); // add that play to the list of plays on that day
       // }
        //return groupedMap;
   // }

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

    // ------------------testing helpers------------------

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

    /**
     * Helper method for checking if a condition is true (pass) or not (fail)
     * @param test what we're testing
     * @param condition a boolean condition to check it
     */
    private static void check (String test, boolean condition){
        if (condition) {
            System.out.println("Pass: " + test);
        } else {
            System.out.println("Fail: " + test);
        }
    }

    public static void main(String[] args) {

        // ------------------Test 1: no outliers------------------
        System.out.println("Test 1: bucket has the same genre");
        List<KeyValuePair> test1 = new ArrayList<>();
        addPlaysHelper(test1, 2023, 9, 1, "Pop", 5);
        addPlaysHelper(test1, 2023, 9, 2, "Pop", 10);
        addPlaysHelper(test1, 2023, 9, 3, "Pop", 4);
        Bucket normalBucket1 = new Bucket("NORMAL");

        for (KeyValuePair entry : test1){
            normalBucket1.addPlay(entry);
        }

        List<Bucket> test1Buckets = new ArrayList<>();
        test1Buckets.add(normalBucket1);

        OutlierDetector od1 = new OutlierDetector(test1Buckets, 4);
        List<OutlierDay> r1 = od1.findOutliers();
        check("Test 1 no outliers", r1.isEmpty());

        System.out.println("------------------");

       
        // ------------------Test 2: one outlier------------------
        System.out.println("Test 2: one clear outlier");
        List<KeyValuePair> test2 = new ArrayList<>();
        addPlaysHelper(test2, 2023, 9, 1, "Pop", 8);
        addPlaysHelper(test2, 2023, 9, 2, "Pop", 10);
        addPlaysHelper(test2, 2023, 9, 3, "Classical", 4);
        Bucket normalBucket2 = new Bucket("NORMAL");

        for (KeyValuePair entry : test2){
            normalBucket2.addPlay(entry);
        }

        List<Bucket> test2Buckets = new ArrayList<>();
        test2Buckets.add(normalBucket2);

        OutlierDetector od2 = new OutlierDetector(test2Buckets, 4);

        List<OutlierDay> r2 = od2.findOutliers();

        check("Test 2 one outlier", r2.size() == 1);
        check("Test 2 outlier is Sep 3", r2.get(0).getDate().equals(LocalDate.of(2023, 9, 3)));
        check("Test 2 outlier genre is Classical", r2.get(0).getDayGenre().equals("Classical"));

        System.out.println("------------------");

        // ------------------Test 3: the outlier day plays below threshold------------------
        System.out.println("Test 3: outlier day has below threshold plays and doesn't count");
        List<KeyValuePair> test3 = new ArrayList<>();
        addPlaysHelper(test3, 2023, 9, 1, "Pop", 8);
        addPlaysHelper(test3, 2023, 9, 2, "Pop", 10);
        addPlaysHelper(test3, 2023, 9, 3, "Classical", 2); // below threshold (4)
        Bucket normalBucket3 = new Bucket("NORMAL");

        for (KeyValuePair entry : test3){
            normalBucket3.addPlay(entry);
        }

        List<Bucket> test3Buckets = new ArrayList<>();
        test3Buckets.add(normalBucket3);

        OutlierDetector od3 = new OutlierDetector(test3Buckets, 4);
        List<OutlierDay> r3 = od3.findOutliers();

        check("Test 3 outlier filtered out", r3.isEmpty());

        System.out.println("------------------");

        // ------------------Test 4: empty buckets edge case------------------
        System.out.println("Test 4: outlier day has below threshold plays and doesn't count");
        Bucket emptyBucket = new Bucket("MIDTERM");
        Bucket normalBucket4 = new Bucket("NORMAL");

        for (KeyValuePair entry : test2){
            normalBucket4.addPlay(entry);
        }

        List<Bucket> test4Buckets = new ArrayList<>();

        test4Buckets.add(emptyBucket);
        test4Buckets.add(normalBucket4);

        OutlierDetector od4 = new OutlierDetector(test4Buckets, 4);
        List<OutlierDay> r4 = od4.findOutliers();

        check("Test 4 empty bucket ignored", r4.size() == 1);
        check("Test 4 outlier comes from NORMAL", r4.get(0).getBucketName().equals("NORMAL"));

        System.out.println("------------------");
    }
}