// Can change this whole file
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import com.google.gson.*;



public class MBTA {
  // Only the address is final, contents can be changed. Instance as can have many MBTA objects.
  // Overall state, read only after initialisation (no lock needed).
  private final ConcurrentHashMap<String, List<Station>> MBTALines = new ConcurrentHashMap<>();
  // Tracks passenger - lock on MBTAPassengerJourneys
  private final ConcurrentHashMap<String, List<Station>> MBTAPassengerJourneys = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Passenger, Optional<Station>> MBTAPassengerLocations = new ConcurrentHashMap<>();
  // Tracks train - lock on MBTATrainLocations
  private final ConcurrentHashMap<Train, Station> MBTATrainLocations = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Train, Integer> MBTATrainDirections = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Station, Optional<Train>> MBTAStationOccupied = new ConcurrentHashMap<>();
  // Tracks train and passenger - first lock MBTATrainLocations then lock MBTAPassengerJourneys
  private final ConcurrentHashMap<Train, List<Passenger>> MBTAPassengersOnBoard = new ConcurrentHashMap<>();
  
  public final boolean debugStatements = false;
  
  // Creates an initially empty simulation
  public MBTA() { }

  // Getter / setter methods for the maps
  public ConcurrentHashMap<String, List<Station>> getLinesMap(){
    return MBTALines;
  }

  public ConcurrentHashMap<String, List<Station>> getJourneysMap(){
    return MBTAPassengerJourneys;
  }

  public ConcurrentHashMap<Passenger, Optional<Station>> getPasLocMap(){
    return MBTAPassengerLocations;
  }

  public ConcurrentHashMap<Train, Station> getTrainLocMap(){
    return MBTATrainLocations;
  }

  public ConcurrentHashMap<Train, Integer>  getTrainDirMap(){
    return MBTATrainDirections;
  }

  public ConcurrentHashMap<Station, Optional<Train>>  getStatOccMap(){
    return MBTAStationOccupied; 
  }

  public ConcurrentHashMap<Train, List<Passenger>>  getOnBoardMap(){
    return MBTAPassengersOnBoard; 
  }
  
  public void setLinesMap(String line, List<Station> stations){
    MBTALines.put(line, stations);
  }

  public void setJourneysMap(String passenger, List<Station> journey){
    MBTAPassengerJourneys.put(passenger, journey);
  }

  public void setPasLocMap(Passenger passenger, Station station){
    if (station == null) {
      MBTAPassengerLocations.put(passenger, Optional.empty()); 
    } else {
      MBTAPassengerLocations.put(passenger, Optional.of(station)); 
    }
  }

  public void setTrainLocMap(Train train, Station station){
    MBTATrainLocations.put(train, station);
  }

  public void setTrainDirMap(Train train, Integer direction){
    MBTATrainDirections.put(train, direction);
  }

  public void setStatOccMap(Station station, Train train){
    if (train == null) {
      MBTAStationOccupied.put(station, Optional.empty()); 
    } else {
      MBTAStationOccupied.put(station, Optional.of(train)); 
    }
    
  }

  public void setOnBoardMap(Train train, List<Passenger> passengers){
    MBTAPassengersOnBoard.put(train, passengers);
  }

  // Adds a new transit line with given name and stations. Called directly or through loadConfig.
  public void addLine(String curLineName, List<String> curStationStrList) {
    synchronized(MBTALines) {
      if (debugStatements) System.out.println("* Registering line: " + curLineName); // eg red

      // Add train to starting station on their line.
      Train curTrain = Train.make(curLineName);
      MBTATrainLocations.put(curTrain, Station.make(curStationStrList.get(0)));

      MBTATrainDirections.put(curTrain, 1); // Start going forward

      // Start with no passengers
      MBTAPassengersOnBoard.put(curTrain, new ArrayList<>());

      List<Station> lineStationList = new LinkedList<>(); // Empty
      if (debugStatements) System.out.println("Stations to store for line: " + curStationStrList); // list of strings
      
      for (String curStationStr : curStationStrList) {
        Station curStation = Station.make(curStationStr);
        lineStationList.add(curStation);

        if (lineStationList.size() == 1) {
          MBTAStationOccupied.putIfAbsent(curStation, Optional.of(curTrain));
        } else {
          MBTAStationOccupied.putIfAbsent(curStation, Optional.empty());
        }
      }
      MBTALines.put(curLineName, lineStationList);
      if (debugStatements) System.out.println("Stations stored for line: " + lineStationList.toString());
    }
  }

  // Adds a new planned journey to the simulation. Called directly or through loadConfig.
  public void addJourney(String curPassengerName, List<String> curTripStrList) {
    synchronized(MBTAPassengerJourneys) {
      if (debugStatements) System.out.println("* Registering Passenger: " + curPassengerName); // eg red

      List<Station> curStopList = new LinkedList<>(); // Empty
      if (debugStatements) System.out.println("Stations to store for Passenger: " + curTripStrList); // list of strings

      for (String curStopStr : curTripStrList) {
        Station curStop = Station.make(curStopStr);
        curStopList.add(curStop);
      }
      MBTAPassengerJourneys.put(curPassengerName, curStopList);
      if (debugStatements) System.out.println("Stations stored for Passenger: " + curStopList.toString());

      // Add passenger to starting station on their route
      Passenger curPassenger = Passenger.make(curPassengerName);
      MBTAPassengerLocations.put(curPassenger, Optional.of(Station.make(curTripStrList.get(0)) ));
    }
  }

  // Return normally if initial simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkStart() {
    // Check that train lines loaded from the config file have been initialised.
    Set<String> lines = MBTALines.keySet();

    for (String line : lines) {
      Station firstStation = MBTALines.get(line).get(0);
      Train train = Train.make(line);
      if (!firstStation.equals(MBTATrainLocations.get(train))){
        throw new RuntimeException("The train for the " + line + " is not at the starting location.");
      }
    }

    // Check that passengers loaded from the config file have been initialised.
    Set<String> passengers = MBTAPassengerJourneys.keySet();

    for (String passengerStr : passengers) {
      Station firstStation = MBTAPassengerJourneys.get(passengerStr).get(0);
      Passenger passenger = Passenger.make(passengerStr);
      Optional<Station> sOptional = MBTAPassengerLocations.get(passenger);
      if (sOptional.isEmpty() | !firstStation.equals(sOptional.get())){
        throw new RuntimeException("The passenger " + passengerStr + " is not at the start of their journey.");
      }
    }

    // Initialisation is in order.
    if (debugStatements) System.out.println("Check Start has completed with no issues.");
  }

  // Return normally if final simulation conditions are satisfied, otherwise
  // raises an exception
  public void checkEnd() {
    // Check that passengers have reached their destinations.
    Set<String> passengers = MBTAPassengerJourneys.keySet();

    for (String passengerStr : passengers) {
      List<Station> passengerJourney = MBTAPassengerJourneys.get(passengerStr);
      Station lastStation = passengerJourney.get(passengerJourney.size() - 1);
      Passenger passenger = Passenger.make(passengerStr);

      Optional<Station> sOptional = MBTAPassengerLocations.get(passenger);
      if (debugStatements) System.out.println("Passenger " + passengerStr + " going to " + lastStation + ", currently at : " + sOptional.get());
      if (sOptional.isEmpty() | !lastStation.equals(sOptional.get())){
        throw new RuntimeException("The passenger " + passengerStr + " is not at their destination.");
      }
    }

    // End of simulation is in order.
    if (debugStatements) System.out.println("Check End has completed with no issues.");
  }

  // reset to an empty simulation
  public void reset() {
    synchronized (MBTALines) {
      MBTALines.clear();
    }
    synchronized (MBTAPassengerJourneys) {
        MBTAPassengerJourneys.clear();
    }
    synchronized (MBTAPassengerLocations) {
        MBTAPassengerLocations.clear();
    }
    synchronized (MBTATrainLocations) {
        MBTATrainLocations.clear();
    }
    synchronized (MBTATrainDirections) {
        MBTATrainDirections.clear();
    }
    synchronized (MBTAStationOccupied) {
        MBTAStationOccupied.clear();
    }
    synchronized (MBTAPassengersOnBoard) {
        MBTAPassengersOnBoard.clear();
    }
    // FIX: How to I make sure no other threads are running that would change these after reset?
  }

  // adds simulation configuration from a file
  public void loadConfig(String filename) {
    if (debugStatements) System.out.println("\n*** Starting file read from: " + filename);
    // Reset old states
    this.reset();

    // Read config file to memory
    String jsonString;
    Gson gson = new Gson();

    try {
      jsonString = new String(Files.readAllBytes(Paths.get(filename)));
      // Process the JSON content
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    JsonConfig config = gson.fromJson(jsonString, JsonConfig.class);
    
    // Parse lines
    Set<String> lineNames = config.lines.keySet();
    synchronized (MBTALines) {
      for (String curLineName : lineNames){
        // Register line
        List<String> lineStations = config.lines.get(curLineName);
        addLine(curLineName, lineStations);
      }
    }

    if (debugStatements) System.out.println("\n== Lines loaded. Parsing journeys.\n");
    // Parse journeys
    synchronized (MBTAPassengerJourneys){
      Set<String> passengerNames = config.trips.keySet();
      
      for (String curPassengerName : passengerNames){
        // Register passenger trip
        List<String> route = config.trips.get(curPassengerName);
        addJourney(curPassengerName, route);
      }
    }
    if (debugStatements) System.out.println("*** Reading configuration from: " + filename + " complete.\n");
  }

  public void runPassenger(Passenger p, Log log) {
    // Case 1 - at destination -> kill thread.
    // Case 2 - On train 1, no train at next stop -> wait.
    // Case 3 - On train 1, train 1 at next stop -> deboard.
    // Case 4 - On train 1, train 2 at next stop -> wait.
    // Case 5 - At station, no train at station -> wait.
    // Case 6 - At station, wrong train at station -> wait.
    // Case 7 - At station, right train at station -> board.

    // Passenger info
    Station curPasStation;
    Optional<Station> sOptional = MBTAPassengerLocations.get(p);
    if (sOptional.isPresent()){
      curPasStation = sOptional.get();
    } else {
      curPasStation = null;
    }
    
    List<Station> journeyStops = MBTAPassengerJourneys.get(p.toString());

    // Case 1
    // If passenger has no more stops, and is at their final station, kill the thread.
    if ( (curPasStation != null) & (journeyStops.size() == 1) & (journeyStops.get(0).equals(curPasStation)) ){
      // Journey complete - kill this thread
      Thread.currentThread().interrupt();
      return; 
    }

    // On train, check for deboard conditions.
    if (curPasStation == null) {
      // Index 0 is the next station as I pop the departure station at boarding.
      Station nextStop = journeyStops.get(0); 
      if (debugStatements) System.out.println("Next stop for passenger " + p + " is " + nextStop);
      // Block passenger's next target destination to prevent trains from arriving/leaving while I read.
      synchronized(nextStop) {
        Train tPotential;
        Optional<Train> tOptional = MBTAStationOccupied.get(nextStop);
        if (tOptional.isEmpty()){
          tPotential = null;
        } else {
          tPotential = tOptional.get();
        }

        // Case 2
        while (tPotential == null){
          // No train is at the next stop.
          try {
            nextStop.wait(); // Don't keep checking, wait for a train to notify.
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return; // Exit on interruption
          }

          // Update while loop conditions before check.
          tOptional = MBTAStationOccupied.get(nextStop);
          if (tOptional.isEmpty()){
            tPotential = null;
          } else {
            tPotential = tOptional.get();
          }
        }

        // Case 3 & 4
        // synchronized(tPotential) {
        while (tPotential != null) {
          List<Passenger> passengerList = MBTAPassengersOnBoard.get(tPotential);

          if (passengerList.contains(p)){
            // The passenger is on the train at their next stop, so should deboard.
            // Add station to passenger location
            MBTAPassengerLocations.put(p, Optional.of(nextStop));
            // Remove passenger from list people on board train.
            passengerList.remove(p);
            MBTAPassengersOnBoard.put(tPotential, passengerList);
            // Make a deboard log.
            log.passenger_deboards(p, tPotential, nextStop);
            nextStop.notifyAll();
            return;
            // Don't wait as next loop will either kill the thread or wait on the next destination.
          } else {
            // The passenger's train is not at their next stop, another train is.
            try {
              nextStop.wait(); // Don't keep checking, wait for a train to notify.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return; // Exit on interruption
            }
          } // End if p is on tPotential. 

          // Update which train is at the destination and check if p is on board it.
          tOptional = MBTAStationOccupied.get(nextStop);
          if (tOptional.isEmpty()){
            tPotential = null;
          } else {
            tPotential = tOptional.get();
          }
        } // End while tPotential != null;

        // Re-enter so that all states are updated.
        return;
      } // End synchronize on next stop
    } // End passenger on train.


    // Passenger is at a station.
    synchronized (curPasStation) {
      if (debugStatements) System.out.println("Passenger " + p + " is at a station " + curPasStation + " with journey: " + journeyStops);
      Station nextDest = journeyStops.get(1); // Index 0 is the current station.

      // Train info
      Optional<Train> tOptional = MBTAStationOccupied.get(curPasStation);

      // Case 5
      while (tOptional.isEmpty()) {
        // No train at this station.
        try {
          curPasStation.wait(); // Don't keep checking, wait for a train to notify.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; // Exit on interruption
        }
        tOptional = MBTAStationOccupied.get(curPasStation); // Recheck condition
      } 

      // A train is at this station.
      Train tPotential = tOptional.get();
      List<Station> line = this.getLinesMap().get(tPotential.toString());
      // Check destination is on this line
      Integer nextDestIdx = line.indexOf(nextDest); // returns -1 if not found.
      // Case 6
      // Train doesn't go to the destination.
      while (nextDestIdx == -1) {
        try {
          curPasStation.wait(); // Don't keep checking, wait for a train to notify.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return; // Exit on interruption
        }
        // Recheck condition
        // Get train at curStation or null.
        tOptional = MBTAStationOccupied.get(curPasStation); 
        if (tOptional.isPresent()) {
          tPotential = tOptional.get();
          line = MBTALines.get(tPotential.toString());
          // System.out.println(line);
          nextDestIdx = line.indexOf(nextDest);
        }
      }
      
      // Case 7
      // Train goes to the next stop - board.
      // Move passenger from platform to train
      MBTAPassengerLocations.put(p, Optional.empty());
      List<Passenger> onBoard = this.getOnBoardMap().get(tPotential);
      onBoard.add(p);
      MBTAPassengersOnBoard.put(tPotential, onBoard);

      // Update journey to remove departing station.
      journeyStops.remove(0); 
      MBTAPassengerJourneys.put(p.toString(), journeyStops);
      
      log.passenger_boards(p, tPotential, curPasStation);
      curPasStation.notifyAll();
      return;

    } // End synchronize on station curPasStation.
  }

  public void runTrain(Train t, Log log) { 
    // Case 1 - next station is occupied -> wait().
    // Case 2 - next station is free -> move and notify on both stations. Check dir.

    Station curStation = MBTATrainLocations.get(t);
    Integer direction = MBTATrainDirections.get(t);

    List<Station> line = this.getLinesMap().get(t.toString());
    int curStationIdx = line.indexOf(curStation);
    Station nextStation = line.get(curStationIdx + direction);

    Station firstLockStation;
    Station secondLockStation;

    if (direction > 0) {
      firstLockStation = curStation;
      secondLockStation = nextStation;
    } else {
      firstLockStation = nextStation;
      secondLockStation = curStation;
    }
     
    synchronized(firstLockStation){
      synchronized(secondLockStation){
        // Check if next station is occupied.
        Optional<Train> tOptional = MBTAStationOccupied.get(nextStation);
        // if (tOptional.isEmpty()){
        //   occTrain = null;
        // } else {
        //   occTrain = tOptional.get();
        // }


        // Case 1
        // Next station is occupied.
        while (tOptional.isPresent()){
          try {
            nextStation.wait(); // Don't keep checking, wait for a train to notify.
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
              return; // Exit on interruption
          }
          // Recheck condition
          tOptional = MBTAStationOccupied.get(nextStation);
        }

        // Case 2
        // Next station is free.
      
        MBTAStationOccupied.put(curStation, Optional.empty());
        MBTAStationOccupied.put(nextStation, Optional.of(t));
        MBTATrainLocations.put(t, nextStation);

        if ( (line.indexOf(nextStation) == 0) | (line.indexOf(nextStation) == line.size()-1) ){
          MBTATrainDirections.put(t, direction * -1);
        }

        log.train_moves(t, curStation, nextStation);
        curStation.notifyAll();
        nextStation.notifyAll();
      } // End synchronize second station
    } // End synchronize first station
  } // End runTrain

} // End MBTA class.
