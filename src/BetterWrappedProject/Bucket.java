package BetterWrappedProject;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a named category (or time period) that groups a list of play events.
 *
 * @author Neha Dixit
 * @author Olivia Ma
 * @author Stefanie Nguyen
 */
public class Bucket {
    private String name;
    private List<KeyValuePair> plays;

    /**
     * Constructs an empty bucket with the specified name.
     * 
     * @param name the name of the bucket
     */
    public Bucket(String name){
        this.name = name;
        this.plays = new ArrayList<>();
    }

    /**
     * Gets the name of the bucket.
     * 
     * @return the bucket name
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the list of play events in this bucket.
     * 
     * @return the list of plays
     */
    public List<KeyValuePair> getPlays(){
        return plays;
    }

    /**
     * Adds a play event to the bucket.
     * 
     * @param entry the play event to add
     */
    public void addPlay(KeyValuePair entry){
        plays.add(entry);
    }
}