import java.util.HashMap;

public class Passenger extends Entity {
  private static HashMap<String, Passenger> passengers = new HashMap<>();

  private Passenger(String name) {
    // Don't change
    super(name); 
  }

  public static Passenger make(String name) {
    // Can change this method!
    if (passengers.containsKey(name)) {
      return passengers.get(name);
    }
    else {
      Passenger passenger = new Passenger(name);
      passengers.put(name, passenger);
      return passenger;
    }
  }
  
}
