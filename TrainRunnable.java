public class TrainRunnable implements Runnable {
    private final Train train;
    private final MBTA m;
    private final Log log;

    public TrainRunnable(Train train, MBTA m, Log log) {
        this.train = train;
        this.m = m;
        this.log = log;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                m.runTrain(train, log); // Define train logic in MBTA
                Thread.sleep(10); // Pause at station
            }
        } catch (InterruptedException e) {
            // Thread interrupted; exit gracefully
        }
    }
}
