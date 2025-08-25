import java.util.HashMap;

public class Station extends Entity {
  private static HashMap<String, Station> stations = new HashMap<String, Station>();

  private Station(String name) {
    // Don't change
    super(name); 
  }

  public static Station make(String name) {
    // Can change this method!

    // Getter-setter method for singleton design pattern so that only one instance of each station exists.
    if (stations.containsKey(name)) {
      return stations.get(name);
    }
    else {
      Station station = new Station(name);
      stations.put(name, station);
      return station;
    }
  }
}
