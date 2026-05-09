import java.util.ArrayList;
import java.util.List;

public class Bucket {
    private String name;
    private List<KeyValuePair> plays;

    public Bucket(String name){
        this.name = name;
        this.plays = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public List<KeyValuePair> getPlays(){
        return plays;
    }

    public void addPlay (KeyValuePair entry){
        plays.add(entry);
    }
}