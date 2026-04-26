import com.sun.source.tree.NewArrayTree;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class BetterWrapped implements AwesomeMusicAnalyzer {
    // The 3 categories: midTerm, Break, Finals
    private List<MusicEntry> midtermList = new ArrayList<>();
    private List<MusicEntry> breakList = new ArrayList<>();
    private List<MusicEntry> normalList = new ArrayList<>();
    private List<MusicEntry> allHistory = new ArrayList<>();

    //Giant list to record everything
    private List<Long> midTermDates;
    private List<Long> breakDates;

    public BetterWrapped(List<Long> midTermDates, List<Long> breakDates) {
        this.midTermDates = midTermDates;
        this.breakDates = breakDates;
    }

    /**
    * Takes the raw dataset and categorizes each song into one of the 
    * three subcategories based on user-provided dates. 
    */ 
    @Override
    public void categorizeData(SongInfo song, long timestamp) {
        MusicEntry entry = new MusicEntry(timestamp, song);

    }

    public boolean isBreak(long timestamp) {
        pass;
    }

    public boolean isMidterm(long timestamp) {
        pass;
    }

}
