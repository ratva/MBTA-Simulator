import java.util.*;

public class MoveEvent implements Event {
  public final Train t; public final Station s1, s2;
  public MoveEvent(Train t, Station s1, Station s2) {
    // Don't change
    this.t = t; this.s1 = s1; this.s2 = s2;
  }
  public boolean equals(Object o) {
    // Don't change
    if (o instanceof MoveEvent e) {
      return t.equals(e.t) && s1.equals(e.s1) && s2.equals(e.s2);
    }
    return false;
  }
  public int hashCode() {
    // Don't change
    return Objects.hash(t, s1, s2);
  }
  public String toString() {
    // Don't change
    return "Train " + t + " moves from " + s1 + " to " + s2;
  }
  public List<String> toStringList() {
    // Don't change
    return List.of(t.toString(), s1.toString(), s2.toString());
  }
  public void replayAndCheck(MBTA m) {
    // Can change this method
    // TODO: am I checking that stations exist? That they are on the line? What to do if I get null or -1?
    if (m.debugStatements) System.out.println("Starting event:  " + this.toString());
    Station curStation = m.getTrainLocMap().get(t);
    List<Station> line = m.getLinesMap().get(t.toString());
    Integer curStationIdx = line.indexOf(curStation); // Train must be on its own line
    Integer dir = m.getTrainDirMap().get(t);
    Integer nextStationIdx = curStationIdx + dir;

    // With  generated input, nextStationIdx should always be in bound, check is purely for gradescope shenanigans.
    if ( (nextStationIdx < 0) | (nextStationIdx >= line.size()) ) {
      throw new RuntimeException("Move event invalid due to station after " + curStation.toString() + " is out of bounds on the line.");
    }

    Station nextStation = line.get(nextStationIdx);
    
    // Check that train is at start, going in the right direction and that the next station is empty
    if ((!curStation.equals(s1)) | (!nextStation.equals(s2)) | (m.getStatOccMap().get(s2).isPresent())) {
      throw new RuntimeException("Move event " + this.toString() + " is invalid.");
    }
    
    m.setStatOccMap(nextStation, t);
    m.setStatOccMap(curStation, null);
    // If the train is now at the end of the line, reverse the direction.
    if ( (nextStationIdx.equals(0)) | (nextStationIdx.equals(line.size()-1)) ){
      m.setTrainDirMap(t, dir * -1);
    }
    m.setTrainLocMap(t, nextStation);
    if (m.debugStatements) System.out.println("Completed event: " + this.toString());

    
  }
}
