// Can change this whole file
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Sim {

  public static void run_sim(MBTA mbta, Log log) {
    List<Thread> passengerThreads = new ArrayList<>();
    List<Thread> trainThreads = new ArrayList<>();

    // Create and start passenger threads
    for (Passenger passenger : mbta.getPasLocMap().keySet()) {
      PassengerRunnable passengerRunnable = new PassengerRunnable(passenger, mbta, log);
      Thread passengerThread = new Thread(passengerRunnable);
      passengerThreads.add(passengerThread);
      passengerThread.start();
    }
  
    // Create and start train threads
    for (Train train : mbta.getTrainLocMap().keySet()) {
      TrainRunnable trainRunnable = new TrainRunnable(train, mbta, log);
      Thread trainThread = new Thread(trainRunnable);
      trainThreads.add(trainThread);
      trainThread.start();
    }

    // Run sim needs to wait for all passenger threads to finish
    for (Thread passengerThread : passengerThreads) {
      try {
        passengerThread.join(); // Better than Thread.getState() as that repeatedly polls but this waits.
      } catch (InterruptedException e) {
          // Handle exception if main thread is interrupted
          System.err.println("Passenger thread interrupted: " + e.getMessage());
      }
    }

    // Terminate train threads once passengers are done
    for (Thread trainThread : trainThreads) {
      trainThread.interrupt(); // Signal train threads to stop
    }

    // Wait for all train threads to finish
    for (Thread trainThread : trainThreads) {
      try {
        trainThread.join(); // Better than Thread.getState() as that repeatedly polls but this waits.
      } catch (InterruptedException e) {
          // Handle exception if main thread is interrupted
          System.err.println("Train thread interrupted: " + e.getMessage());
      }
    }

  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("usage: ./sim <config file>");
      System.exit(1);
    }

    MBTA mbta = new MBTA();
    mbta.loadConfig(args[0]);

    Log log = new Log();

    run_sim(mbta, log);

    String s = new LogJson(log).toJson();
    PrintWriter out = new PrintWriter("log.json");
    out.print(s);
    out.close();

    mbta.reset();
    mbta.loadConfig(args[0]);
    Verify.verify(mbta, log);
  }
}
