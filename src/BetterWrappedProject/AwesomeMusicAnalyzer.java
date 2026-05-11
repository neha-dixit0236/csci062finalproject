package BetterWrappedProject;
import java.util.List;

/**
 * Interface defining the core analysis features for the music application.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */
public interface AwesomeMusicAnalyzer{
//Feature 1: Listening Trend Analysis

/**
* Takes the raw dataset and categorizes each song into one of the 
* three subcategories based on user-provided dates. 
 * 
 * @param song the song information to categorize
 * @param timestamp the time the song was played
*/
public void categorizeData(SongInfo song, long timestamp);

/** 
 * Returns the "Musical Identity" (Top Artist/Genre) for a specific category. 
* This will iterate through the chosen ArrayList. 
 * 
 * @param categoryName the category to analyze
 * @return the top artist or genre for the category
*/ 
String getIdentityForCategory(String categoryName);

// Feature 2: Detecting Outliers

/** 
* Scans a specific category (e.g., "Normal") to find "Obsession" tracks. 
* These are tracks played at a frequency significantly higher than 
* the average for that period. 
 * 
* @param category The list to scan (Midterm, Break, or Normal) 
* @return A list of tracks that are statistical outliers. 
*/
List<SongInfo> detectFrequencyOutliers(String category); 

/** 
* Detects "Temporal Outliers" - days where the number of songs played 
* deviates significantly from the user's daily average. 
 * 
 * @param category the category to analyze
 * @return a list of timestamps representing the outlier days
*/
List<Long> detectOutliers(String category);

//Feature 3: Focused Recommendations
/**
 * Recommends tracks from the list that match the "vibe" of the chosen list. 
 * 
 * @param targetCategory the category to base recommendations on
 * @return a list of recommended songs
*/ 
List<SongInfo> getRecommendations(String targetCategory);
}
