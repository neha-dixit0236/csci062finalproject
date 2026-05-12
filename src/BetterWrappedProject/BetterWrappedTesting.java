package BetterWrappedProject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BetterWrappedTesting {
    public static void main(String[] args){
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



        // 5. Running tests for FEATURE 3 - Focused Recommendations Based on Listening History

        System.out.println("Testing FEATURE 3:");

        myWrapped.recommendByWeekdayWeekend("MasterListofSongs(Feature3).csv");

        myWrapped.recommendBySemester(midtermDates, breakDates, "MasterListofSongs(Feature3).csv");
        
        myWrapped.recommendByYear(springDates, summerDates, fallDates,"MasterListofSongs(Feature3).csv");
    }
}
