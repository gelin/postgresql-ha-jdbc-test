import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExperimentExecutor {

    private final String label;
    private final int threads;
    private final long duration;
    private final ExecutorService pool;
    private final List<Future<Experiment.Result>> results = new ArrayList<Future<Experiment.Result>>();
    private final DataSource source;
    private final String query;
    private int count = 0;
    private long totalRows = 0;
    private long totalTime = 0;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;

    public ExperimentExecutor(String label, int threads, long duration, DataSource source, String query) {
        this.label = label;
        this.threads = threads;
        this.duration = duration;
        this.pool = Executors.newFixedThreadPool(threads);
        this.source = source;
        this.query = query;
    }

    public void run() {
        System.err.println("selecting " + this.label + "...");
        long start = System.currentTimeMillis();
        try {
            for (int i = 0; i < this.threads; i++) {
                results.add(this.pool.submit(new Experiment(String.valueOf(this.count), this.source, this.query)));     //TODO better label for experiment
            }
            while (System.currentTimeMillis() < start + this.duration) {
                Thread.sleep(100);
                waitAndAggregate();
            }
            pool.shutdown();
            //TODO final aggregation
        } catch (Exception e) {
            e.printStackTrace();
            pool.shutdown();
        }
    }

    private void waitAndAggregate() throws ExecutionException, InterruptedException {
        synchronized (this.results) {
            ListIterator<Future<Experiment.Result>> i = this.results.listIterator();
            while (i.hasNext()) {
                Future<Experiment.Result> future = i.next();
                if (!future.isDone()) {
                    continue;
                }
                Experiment.Result result = future.get();
                if (result == null) {
                    continue;
                }
                this.count++;
                this.totalRows += result.rows;
                this.totalTime += result.millis;
                this.minTime = Math.min(this.minTime, result.millis);
                this.maxTime = Math.max(this.maxTime, result.millis);
                System.err.println(this.label + ": " + (double) this.totalTime / this.count / 1000.0 + " average time of the experiment");
                System.err.println(this.label + ": " + this.minTime / 1000.0 + " min time of the experiment");
                System.err.println(this.label + ": " + this.maxTime / 1000.0 + " max time of the experiment");
                System.err.println(this.label + ": " + (double) this.totalTime / this.totalRows / 1000.0 + " average time for a row");
                i.remove();
                i.add(this.pool.submit(new Experiment(String.valueOf(this.count), this.source, this.query)));
            }
        }
    }

}
