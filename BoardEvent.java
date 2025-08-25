import java.util.*;

public class BoardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public BoardEvent(Passenger p, Train t, Station s) {
    // Don't change
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    // Don't change
    if (o instanceof BoardEvent e) {
      return p.equals(e.p) && t.equals(e.t) && s.equals(e.s);
    }
    return false;
  }
  public int hashCode() {
    // Don't change
    return Objects.hash(p, t, s);
  }
  public String toString() {
    // Don't change
    return "Passenger " + p + " boards " + t + " at " + s;
  }
  public List<String> toStringList() {
    // Don't change
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA m) {
    // Can change
    if (m.debugStatements) System.out.println("Starting event:  " + this.toString());
    // Passenger info
    Station curPasStation;
    Optional<Station> sOptional = m.getPasLocMap().get(p);
    if (sOptional.isPresent()){
      curPasStation = sOptional.get();
    } else {
      curPasStation = null;
    }

    List<Station> journeyStops = m.getJourneysMap().get(p.toString());
    Station nextDest = journeyStops.get(1); // Index 0 is the current station.

    // Train info
    Station curTrainStation = m.getTrainLocMap().get(t);
    List<Station> line = m.getLinesMap().get(t.toString());

    // Check destination is on this line
    Integer nextDestIdx = line.indexOf(nextDest); // returns -1 if not found.

    // Check that the passenger is at the start, the train is at start, and the destination is on this line
    if ( (!curPasStation.equals(s)) | (!curTrainStation.equals(s)) | (nextDestIdx.equals(-1)) ) {
      throw new RuntimeException("Board event " + this.toString() + " is invalid.");
    }
    
    // Move passenger from platform to train
    m.setPasLocMap(p, null);
    List<Passenger> onBoard = m.getOnBoardMap().get(t);
    onBoard.add(p);
    m.setOnBoardMap(t, onBoard);

    // Update journey to remove departing station.
    journeyStops.remove(0); 
    m.setJourneysMap(p.toString(), journeyStops);
    if (m.debugStatements) System.out.println("Completed event: " + this.toString());
  }
}
