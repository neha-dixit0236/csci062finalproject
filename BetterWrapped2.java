import java.sql.Timestamp;
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

    }

    private void analyzeSemester(Timestamp midtermDates, Timestamp breakDates){
        List<KeyValuePair> midtermList = new ArrayList<>();
        List<KeyValuePair> breakList = new ArrayList<>();
        List<KeyValuePair> normalList = new ArrayList<>();

        //they input the date into the console, and then we put that into a list of our own to classify it as a midterm date

        for (KeyValuePair entry : allHistory) {
            Timestamp songTime = entry.getTimeStamp();

            if (isWithinWindow(songTime, midtermDates, 5)) {
                midtermList.add(entry);
            } else if (isWithinWindow(songTime, breakDates, 5)) {
                breakList.add(entry);
            } else {
                normalList.add(entry);
            }
            }

        System.out.println("======= MIDTERM PERIOD STATS =======");
        SongStatistics midtermStats = new SongStatistics(midtermList);
        System.out.println(midtermStats);

    }

    /**
     * Checks if a song was played within a specific number of days BEFORE a target date.
    */

    private boolean isWithinWindow(Timestamp songTime, Timestamp targetDate, int daysBefore) {
        LocalDateTime ldt = targetDate.toLocalDateTime();
        LocalDateTime startDateLDT = ldt.minusDays(daysBefore);
        Timestamp startDate = Timestamp.valueOf(startDateLDT);

        return songTime.after(startDate) && songTime.before(targetDate);

    }

    // private void analyzeYear(List<String> springDates, List<String> summerDates, List<String> fallDates){
    //     pass;
    // }

    public static void main(String[] args) {
        // 1. Create the instance
        BetterWrapped2 myWrapped = new BetterWrapped2("testScrobbles.csv");

        // 2. Create sample test dates (Midterms on 9 29, Break on Nov 24)
        Timestamp testMidterm = Timestamp.valueOf(LocalDateTime.of(2023, 9, 29, 23, 59));
        Timestamp testBreak = Timestamp.valueOf(LocalDateTime.of(2023, 11, 24, 23, 59));

        // 3. Run the test
        System.out.println("Starting Analysis...\n");
        myWrapped.analyzeSemester(testMidterm, testBreak);
    }   
        
    } 

