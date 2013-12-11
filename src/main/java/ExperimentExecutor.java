import javax.sql.DataSource;
import java.util.concurrent.*;

public class ExperimentExecutor {

    private final String label;
    private final int threads;
    private final long duration;
    private final ExecutorService pool;
    private final ExecutorCompletionService<Experiment.Result> completionService;
    private final DriverConnectionSource source;
    private final String query;
    private int runs = 0;
    private int failures = 0;
    private long totalRows = 0;
    private long totalTime = 0;
    private long minTime = Long.MAX_VALUE;
    private long maxTime = 0;

    public ExperimentExecutor(String label, int threads, long duration, DriverConnectionSource source, String query) {
        this.label = label;
        this.threads = threads;
        this.duration = duration;
        this.pool = Executors.newFixedThreadPool(threads);
        this.completionService = new ExecutorCompletionService<Experiment.Result>(this.pool);
        this.source = source;
        this.query = query;
    }

    public void run() {
        System.err.println("selecting " + this.label + "...");
        long end = System.currentTimeMillis() + this.duration;
        int tasks = 0;
        try {
            for (int i = 0; i < this.threads; i++) {
                tasks++;
                this.completionService.submit(new Experiment(this.source, this.query));
            }
            while (tasks > 0) {
                Future<Experiment.Result> future = this.completionService.take();
                tasks--;
                aggregate(future.get());
                if (System.currentTimeMillis() < end) {
                    tasks++;
                    this.completionService.submit(new Experiment(this.source, this.query));
                } else {
                    this.pool.shutdown();
                }
            }
            System.err.println(this.label + ": " + this.runs + " experiments run");
            System.err.println(this.label + ": " + this.failures + " experiments failed");
            System.err.println(this.label + ": " + (double) this.totalTime / this.runs / 1000.0 + " average time of the experiment");
            System.err.println(this.label + ": " + this.minTime / 1000.0 + " min time of the experiment");
            System.err.println(this.label + ": " + this.maxTime / 1000.0 + " max time of the experiment");
            System.err.println(this.label + ": " + (double) this.totalTime / this.totalRows / 1000.0 + " average time for a row");
        } catch (Exception e) {
            e.printStackTrace();
            this.pool.shutdown();
        }
    }

    private void aggregate(Experiment.Result result) throws ExecutionException, InterruptedException {
        if (result == null) {
            return;
        }
        this.runs++;
        if (!result.success) {
            this.failures++;
        }
        this.totalRows += result.rows;
        this.totalTime += result.millis;
        this.minTime = Math.min(this.minTime, result.millis);
        this.maxTime = Math.max(this.maxTime, result.millis);
    }

}
