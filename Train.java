import java.util.HashMap;

public class Train extends Entity {
  private static HashMap<String, Train> trains = new HashMap<>();
  private Train(String name) {
    // Don't change
    super(name); 
  }

  public static Train make(String name) {
    // Can change this method!
    // Getter-setter method for singleton design pattern so that only one instance of each train exists.
    if (trains.containsKey(name)) {
      return trains.get(name);
    }
    else {
      Train train = new Train(name);
      trains.put(name, train);
      return train;
    }
  }
  
}
