import java.util.List;

public interface AwesomeMusicAnalyzer{
	//Feature 1: Listening Trend Analysis
/**
* Takes the raw dataset and categorizes each song into one of the 
* three subcategories based on user-provided dates. 
*/
 void categorizeData(List<Long> midtermDates, List<Long> breakRanges);

/** 
 * Returns the "Musical Identity" (Top Artist/Genre) for a specific category. 
* This will iterate through the chosen ArrayList. 
*/ 
String getIdentityForCategory(String categoryName);

// Feature 2: Detecting Outliers

/** 
*
* Scans a specific category (e.g., "Normal") to find "Obsession" tracks. 
* These are tracks played at a frequency significantly higher than 
* the average for that period. 
* @param category The list to scan (Midterm, Break, or Normal) 
* @return A list of tracks that are statistical outliers. 
*/
 
List<SongInfo> detectFrequencyOutliers(String category); 

/** 
* Detects "Temporal Outliers" - days where the number of songs played 
* deviates significantly from the user's daily average. 
*/

List<Long> detectOutliers(String category);

//Feature 3: Focused Recommendations
/**
* Recommends tracks from the list that match the "vibe" * of the chosen list. 
*/ 
List<SongInfo> getRecommendations(String targetCategory);


}
