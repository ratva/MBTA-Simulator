import static org.junit.Assert.*;
import org.junit.*;
import java.util.*;

public class Tests {
  @Test public void testPass() {
    assertTrue("true should be true", true);
  }

  @Test
  public void readConfigFile() {
    MBTA m = new MBTA();
    if (m.debugStatements) System.out.println("\n");
    m.loadConfig("sample.json");

    // System.out.println("\n" + config.lines);

    // Set<String> lineNames = config.lines.keySet();
    
    // for (String line : lineNames){
    //   System.out.println(line); // eg red

    //   System.out.println(config.lines.get(line)); // list of strings
    // }

    // System.out.println("\n" + config.trips);
    // Set<String> trips = config.trips.keySet();
    
    // for (String trip : trips){
    //   System.out.println(trip);
    //   System.out.println(config.trips.get(trip));
    // }
    
    assertTrue("true should be true", true);
    // TODO: change from print statement to actual tests.
  }

  @Test
  public void runVerify() {
    MBTA m = new MBTA();
    // m.loadConfig("sample.json");
    if (m.debugStatements) System.out.println("\n");

    m.addLine("green", List.of("Copley", "Park Street", "East Somerville", "Tufts"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));
    m.addJourney("Dean", List.of("Park Street", "Tufts")); // Same as Bob
    m.addJourney("Fred", List.of("Park Street", "Park Street")); // Start and end at same station
    
    Passenger bob = Passenger.make("Bob");
    Passenger dean = Passenger.make("Dean");
    Passenger fred = Passenger.make("Fred");
    Train green = Train.make("green");
    Station cop = Station.make("Copley");
    Station par = Station.make("Park Street");
    Station eas = Station.make("East Somerville");
    Station tuf = Station.make("Tufts");

    List<Event> events = new ArrayList<>();
    events.add(new MoveEvent(green, cop, par));
    events.add(new BoardEvent(bob, green, par));
    events.add(new BoardEvent(dean, green, par));
    events.add(new BoardEvent(fred, green, par));
    events.add(new MoveEvent(green, par, eas));
    events.add(new MoveEvent(green, eas, tuf));
    events.add(new DeboardEvent(bob, green, tuf));
    events.add(new DeboardEvent(dean, green, tuf));
    events.add(new MoveEvent(green, tuf, eas));
    events.add(new MoveEvent(green, eas, par));
    events.add(new DeboardEvent(fred, green, par));

    if (m.debugStatements) System.out.println("\n");
     Log log = new Log(events);
     try{
      Verify.verify(m, log);
     }
     catch(Exception e) {
      fail("Failed to verify log. " + e.getMessage());
     }
     if (m.debugStatements) System.out.println("\n");
  }

  @Test
  public void testValidSingleJourney() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));

    Log log = new Log(events);
    try {
        Verify.verify(m, log);
    } catch (Exception e) {
        fail("Failed to verify valid single journey. " + e.getMessage());
    }
  }

  @Test
  public void testCircularSingleJourney() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall", "Davis"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");
    Station park = Station.make("Park");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));
    events.add(new BoardEvent(alice, red, kendall));
    events.add(new MoveEvent(red, kendall, park));
    events.add(new MoveEvent(red, park, kendall));
    events.add(new MoveEvent(red, kendall, harvard));
    events.add(new MoveEvent(red, harvard, davis));
    events.add(new DeboardEvent(alice, red, davis));

    Log log = new Log(events);
    try {
        Verify.verify(m, log);
    } catch (Exception e) {
        fail("Failed to verify circular journey. " + e.getMessage());
    }
  }

  @Test
  public void testInvalidExtraStopJourney() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new DeboardEvent(alice, red, harvard));
    events.add(new BoardEvent(alice, red, harvard));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Deboard event"));
  }

  @Test
  public void testInvalidBoardLate() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");

    List<Event> events = new ArrayList<>();
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Board event"));
  }

  @Test
  public void testInvalidDeboardingTooEarly() {
    MBTA m = new MBTA();
    m.addLine("green", List.of("Copley", "Park Street", "Tufts"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));

    Passenger bob = Passenger.make("Bob");
    Train green = Train.make("green");
    Station cop = Station.make("Copley");
    Station par = Station.make("Park Street");

    List<Event> events = new ArrayList<>();
    events.add(new MoveEvent(green, cop, par));
    events.add(new BoardEvent(bob, green, par));
    events.add(new DeboardEvent(bob, green, par)); // Invalid: Bob deboards at start station

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    assertTrue(exception.getMessage().contains("Deboard event"));
  }


@Test
  public void testInvalidTrainMove() {
    MBTA m = new MBTA();
    m.addLine("blue", List.of("Bowdoin", "State", "Aquarium", "Maverick"));

    Train blue = Train.make("blue");
    Station bowdoin = Station.make("Bowdoin");
    Station maverick = Station.make("Maverick");

    List<Event> events = new ArrayList<>();
    events.add(new MoveEvent(blue, bowdoin, maverick)); // Invalid: Skipping intermediate stations

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Move event Train blue moves from Bowdoin to Maverick is invalid."));
  }


  @Test
  public void testValidMultiplePassengersAndLines() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addLine("green", List.of("Copley", "Park Street", "Tufts"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));

    Passenger alice = Passenger.make("Alice");
    Passenger bob = Passenger.make("Bob");
    Train red = Train.make("red");
    Train green = Train.make("green");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");
    Station cop = Station.make("Copley");
    Station par = Station.make("Park Street");
    Station tuf = Station.make("Tufts");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(green, cop, par));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));
    events.add(new BoardEvent(bob, green, par));
    events.add(new MoveEvent(green, par, tuf));
    events.add(new DeboardEvent(bob, green, tuf));

    Log log = new Log(events);
    try {
        Verify.verify(m, log);
    } catch (Exception e) {
        fail("Failed to verify valid multiple passengers and lines. " + e.getMessage());
    }
  }


  @Test
  public void testInvalidStartingStopPassengers() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addLine("green", List.of("Copley", "Park Street", "Tufts"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));

    Passenger alice = Passenger.make("Alice");
    Passenger bob = Passenger.make("Bob");
    Train red = Train.make("red");
    Train green = Train.make("green");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");
    Station par = Station.make("Park Street");
    Station tuf = Station.make("Tufts");

    List<Event> events = new ArrayList<>();
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));
    events.add(new MoveEvent(green, par, tuf));
    events.add(new BoardEvent(bob, green, par));
    events.add(new DeboardEvent(bob, green, tuf));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Board event Passenger Alice boards red at Davis is invalid."));
  }


  @Test
  public void testInvalidNullBoards() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addLine("green", List.of("Copley", "Park Street", "Tufts"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));

    Passenger alice = Passenger.make("Alice");
    Passenger bob = Passenger.make("Bob");
    Train red = Train.make("red");
    Train green = Train.make("green");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");
    Station cop = Station.make("Copley");
    Station par = Station.make("Park Street");
    Station tuf = Station.make("Tufts");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(green, cop, par));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));
    events.add(new BoardEvent(bob, green, par));
    events.add(new BoardEvent(null, green, par));
    events.add(new MoveEvent(green, par, tuf));
    events.add(new DeboardEvent(bob, green, tuf));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Cannot invoke \"Object.hashCode()\" because \"key\" is null"));
  }


  @Test
  public void testNullNameBoards() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addLine("green", List.of("Copley", "Park Street", "Tufts"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));
    m.addJourney("Bob", List.of("Park Street", "Tufts"));

    Passenger alice = Passenger.make("Alice");
    Passenger bob = Passenger.make("Bob");
    Passenger nullName = Passenger.make(null);
    Train red = Train.make("red");
    Train green = Train.make("green");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");
    Station cop = Station.make("Copley");
    Station par = Station.make("Park Street");
    Station tuf = Station.make("Tufts");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(green, cop, par));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));
    events.add(new BoardEvent(bob, green, par));
    events.add(new BoardEvent(nullName, green, par));
    events.add(new MoveEvent(green, par, tuf));
    events.add(new DeboardEvent(bob, green, tuf));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("\"this.name\" is null"));
  }


  @Test
  public void testInvalidBoardingWithoutJourney() {
    MBTA m = new MBTA();
    m.addLine("orange", List.of("Ruggles", "Back Bay", "Tufts Medical Center"));
    // No journey for Carol

    Passenger carol = Passenger.make("Carol");
    Train orange = Train.make("orange");
    Station rug = Station.make("Ruggles");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(carol, orange, rug)); // Invalid: No train is at Ruggles

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception.getMessage());
    assertTrue(exception.getMessage().contains("Cannot invoke \"java.util.Optional.isPresent()\""));
  }

  @Test
  public void testInvalidBoardTwiceJourney() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));

    Log log = new Log(events);
    Exception exception = assertThrows(Exception.class, () -> Verify.verify(m, log));
    // System.out.println(exception);
    assertTrue(exception.toString().contains("java.lang.IndexOutOfBoundsException"));
  }


  @Test
  public void testInvalidAmbiguousLineJourney() {
    MBTA m = new MBTA();
    m.addLine("red", List.of("Davis", "Harvard", "Davis", "Kendall", "Park"));
    m.addJourney("Alice", List.of("Davis", "Kendall"));

    Passenger alice = Passenger.make("Alice");
    Train red = Train.make("red");
    Station davis = Station.make("Davis");
    Station harvard = Station.make("Harvard");
    Station kendall = Station.make("Kendall");

    List<Event> events = new ArrayList<>();
    events.add(new BoardEvent(alice, red, davis));
    events.add(new MoveEvent(red, davis, harvard));
    events.add(new MoveEvent(red, harvard, kendall));
    events.add(new DeboardEvent(alice, red, kendall));

    Log log = new Log(events);
    try {
        Verify.verify(m, log);
    } catch (Exception e) {
        fail("Failed to verify testInvalidAmbiguousLineJourney. " + e.getMessage());
    }
  }
} // End Tests 

