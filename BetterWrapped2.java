import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;


//this will be for all the features?
public class BetterWrapped2 {
    //list of all of the listening history: not sure how to upload the data set and squeeze it into this list
    private List<KeyValuePair> allHistory;

    public BetterWrapped2(String fileName){
        this.allHistory = MusicDataLoader.CSVAnalysis(fileName);
    }

    //this will be the most important method for feature 1
    public void analyze(String comparisonType, List<String> midtermDates, List<String> breakDates, List<String> springDates, List<String> summerDates, List<String> fallDates) {

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


    //might do what i did with scrobbling and make a new class that reads a text file of a bunch of dates and simplifies that into a list
    //also not sure on whether the list should be filled with longs or strings? and with the timestamp stuff too should that be longs or strings?
    private void analyzeSemester(List<String> midtermDates, List<String> breakDates){

    }


    private void analyzeYear(List<String> springDates, List<String> summerDates, List<String> fallDates){
        
    }

    








}
