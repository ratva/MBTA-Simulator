import java.util.*;

public class DeboardEvent implements Event {
  public final Passenger p; public final Train t; public final Station s;
  public DeboardEvent(Passenger p, Train t, Station s) {
    // Don't change
    this.p = p; this.t = t; this.s = s;
  }
  public boolean equals(Object o) {
    // Don't change
    if (o instanceof DeboardEvent e) {
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
    return "Passenger " + p + " deboards " + t + " at " + s;
    
  }
  public List<String> toStringList() {
    // Don't change
    return List.of(p.toString(), t.toString(), s.toString());
  }
  public void replayAndCheck(MBTA m) {
    // Can change
    if (m.debugStatements) System.out.println("Starting event:  " + this.toString());
    // Passenger info
    Station atStation; // Should be null
    Optional<Station> sOptional = m.getPasLocMap().get(p);
    if (sOptional.isPresent()){
      atStation = sOptional.get();
    } else {
      atStation = null;
    }

    List<Station> journeyStops = m.getJourneysMap().get(p.toString());
    Station nextDest = journeyStops.get(0); // Index 0 is the deboard station.
    
    // Train info
    Station curTrainStation = m.getTrainLocMap().get(t);
    Integer onBoardIdx = m.getOnBoardMap().get(t).indexOf(p); // -1 if passenger is not on train.
    
    // Check that the passenger is going to this station, the train is at this station, and that the passenger is on this train.
    if ( (!nextDest.equals(s)) | (!curTrainStation.equals(s)) | (onBoardIdx.equals(-1)) | (atStation != null)) {
      throw new RuntimeException("Deboard event " + this.toString() + " is invalid. ");
    }
    
    // Move passenger from train to platform
    m.setPasLocMap(p, s);
    List<Passenger> onBoard = m.getOnBoardMap().get(t);
    onBoard.remove((int) onBoardIdx);
    m.setOnBoardMap(t, onBoard);

    // No need to edit the journey since we want to end with one item, the final destination, and we remove old stations when we board to leave them.
    if (m.debugStatements) System.out.println("Completed event: " + this.toString());
    
  }
}
