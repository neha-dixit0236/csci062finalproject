package BetterWrappedProject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BetterWrappedTesting {
    public static void main(String[] args){
        // 1. Create the instance
        BetterWrapped myWrapped = new BetterWrapped("testScrobbles.csv");

        // 2. Create sample test dates (Break and Midterm overlap to test priority)
        Timestamp testMidterm = Timestamp.valueOf(LocalDateTime.of(2023, 9, 29, 23, 59));
        Timestamp testMidtermOverlap = Timestamp.valueOf(LocalDateTime.of(2023, 11, 24, 23, 59));
        Timestamp testBreakStart = Timestamp.valueOf(LocalDateTime.of(2023, 11, 20, 0, 0));
        Timestamp testBreakEnd = Timestamp.valueOf(LocalDateTime.of(2023, 11, 26, 23, 59));
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
        System.out.println("Testing ONE_SEMESTER for Feature 1 (Checking Overlap Priority):");
        System.out.println("There is a break from Nov 20-26, and an overlapping midterm on Nov 24.");
        System.out.println("Break takes priority over midterms, so songs played during this overlap will fall into the BREAK bucket.");
        System.out.println(".");
        List<Timestamp> midtermDates = new ArrayList<>();
        midtermDates.add(testMidterm);
        midtermDates.add(testMidtermOverlap);
        List<Timestamp> breakDates = new ArrayList<>();
        breakDates.add(testBreakStart);
        breakDates.add(testBreakEnd);
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
        System.out.println("Testing WEEKDAY_VS_WEEKEND for Feature 2 (Checking Top 5 Cap):");
        System.out.println("Notice that the outlier list is capped at a maximum of 5 days per bucket.");
        myWrapped.detectOutliersByWeekdayWeekend();

        // ----------ONE_SEMESTER----------
        System.out.println("Testing ONE_SEMESTER for Feature 2:");
        myWrapped.detectOutliersBySemester(midtermDates, breakDates);

        // ----------FULL_YEAR----------
        System.out.println("Testing FULL_YEAR for Feature 2:");
        myWrapped.detectOutliersByYear(springDates, summerDates, fallDates);



        // 5. Running tests for FEATURE 3 - Focused Recommendations Based on Listening History

        System.out.println("Testing FEATURE 3 (Recommendations):");
        System.out.println("Recommendations will be shuffled on each run. Empty buckets will be skipped entirely instead of printing the 'Sorry' message.");

        myWrapped.recommendByWeekdayWeekend("MasterListofSongs(Feature3).csv");

        myWrapped.recommendBySemester(midtermDates, breakDates, "MasterListofSongs(Feature3).csv");
        
        myWrapped.recommendByYear(springDates, summerDates, fallDates,"MasterListofSongs(Feature3).csv");
    }
}
