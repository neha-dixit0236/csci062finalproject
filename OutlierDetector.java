import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.*;

public class OutlierDetector {
    // Define outlier:
    // 1. a day is an outlier if its dominant genre differs from the user's overall dominant genre
    // 2. a day is an outlier where the user listened way more or way less than usual -> should we do this tho?
    // 3. In a specific category (e.g. normal days), a day is an outlier if there are tracks played at a frequency significantly higher than 
    // the average for that period. -> should we do this???

    // we need an established period of time: for each feature 1 bucket?

}