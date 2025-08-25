// Don't change the whole file
import java.util.*;

public interface Event {
  public List<String> toStringList();
  public void replayAndCheck(MBTA mbta);
}
