import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

//this will be for all the features?
public class BetterWrapped2 {
    //list of all of the listening history: not sure how to upload the data set and squeeze it into this list
    private List<KeyValuePair> allHistory;

    public BetterWrapped2(String fileName){
        this.allHistory = MusicDataLoader.CSVAnalysis(fileName);
    }

    //this will be the most important method for feature 1
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

    private void analyzeWeekdayVsWeekend(){
        List<KeyValuePair> weekdayList = new ArrayList<>();
        List<KeyValuePair> weekendList = new ArrayList<>();

        for (KeyValuePair entry: allHistory){
            Timestamp timestamp = entry.getTimeStamp();
            LocalDateTime dateTime = timestamp.toLocalDateTime();
            DayOfWeek day = dateTime.getDayOfWeek(); // DayOfWeek is enum with exactly 7 values

            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekendList.add(entry);
            }
            else {
                weekdayList.add(entry);
            }
        }

        SongStatistics weekdayStats = new SongStatistics(weekdayList);
        SongStatistics weekendStats = new SongStatistics(weekendList);
        
        System.out.println("WEEKDAY STATS");
        System.out.println(weekdayStats);

        System.out.println("WEEKEND STATS");
        System.out.println(weekendStats);

    }



    private void analyzeSemester(List<Timestamp> midtermDates, List<Timestamp> breakDates){
        List<KeyValuePair> midtermList = new ArrayList<>();
        List<KeyValuePair> breakList = new ArrayList<>();
        List<KeyValuePair> normalList = new ArrayList<>();

        for (KeyValuePair entry: allHistory){
            Timestamp songTime = entry.getTimeStamp();

            // we assume the "range" starts from 5 days before midterm starts
            if (isWithinWindow(songTime, midtermDates, 5)){
                midtermList.add(entry);
            }
            else if (isWithinDateRange(songTime, breakDates)){
                breakList.add(entry);
            }
            else{
                normalList.add(entry);
            }
        }

        SongStatistics midtermStats = new SongStatistics(midtermList);
        SongStatistics breakStats = new SongStatistics(breakList);
        SongStatistics normalStats = new SongStatistics(normalList);

        System.out.println("MIDTERM STATS"); //might make these print statements a bit more descriptive (like actually comparing instead of just listing stuff)
        System.out.println(midtermStats);

        System.out.println("BREAK STATS");
        System.out.println(breakStats);

        System.out.println("NORMAL DAY STATS");
        System.out.println(normalStats);

    }



    /**
     * Checks if a song is played within the inclusive range from the earliest to the latest date in importantDates. 
     * For breaks and semesters of a year.
     * Assumes importantDates is a single continuous period, like only one break
     * @param songTime
     * @param importantDates
     * @return
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
     * @param songTime
     * @param importantDates
     * @param daysBefore
     * @return
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


    private void analyzeYear(List<Timestamp> springDates, List<Timestamp> summerDates, List<Timestamp> fallDates){
        List<KeyValuePair> springList = new ArrayList<>();
        List<KeyValuePair> summerList = new ArrayList<>();
        List<KeyValuePair> fallList = new ArrayList<>();

        for (KeyValuePair entry: allHistory){
            Timestamp songTime = entry.getTimeStamp();

            if (isWithinDateRange(songTime, springDates)){
                springList.add(entry);
            }
            else if (isWithinDateRange(songTime, summerDates)){
                summerList.add(entry);
            }
            else if (isWithinDateRange(songTime, fallDates)){
                fallList.add(entry);
            }
            // songs that are not in spring, summer, or fall windows are intentionally ignored
        }

        SongStatistics springStats = new SongStatistics(springList);
        SongStatistics summerStats = new SongStatistics(summerList);
        SongStatistics fallStats = new SongStatistics(fallList);

        System.out.println("SPRING STATS");
        System.out.println(springStats);

        System.out.println("SUMMER STATS");
        System.out.println(summerStats);

        System.out.println("FALL STATS");
        System.out.println(fallStats);
    }




    public static void main(String[] args) {
        // 1. Create the instance
        BetterWrapped2 myWrapped = new BetterWrapped2("testScrobbles.csv");

        // 2. Create sample test dates (Midterms on 9 29, Break on Nov 24)
        Timestamp testMidterm = Timestamp.valueOf(LocalDateTime.of(2023, 9, 29, 23, 59));
        Timestamp testBreak = Timestamp.valueOf(LocalDateTime.of(2023, 11, 24, 23, 59));

        // 3. Run the test
        System.out.println("Starting Analysis...\n");

        // test analyzeWeekdayVsWeekend
        myWrapped.analyze("WEEKDAY_VS_WEEKEND", null, null, null, null, null);

        //myWrapped.analyzeSemester(testMidterm, testBreak);
    }   
        
    } 

