public class PassengerRunnable implements Runnable {
    private final Passenger passenger;
    private final MBTA m;
    private final Log log;

    public PassengerRunnable(Passenger passenger, MBTA m, Log log) {
        this.passenger = passenger;
        this.m = m;
        this.log = log;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            m.runPassenger(passenger, log); // Define passenger logic in MBTA
        }
    }
}
