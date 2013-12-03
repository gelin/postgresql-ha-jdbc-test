
import javax.sql.DataSource;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.*;

public class Main {

    static final boolean DEBUG = false;
    static final String QUERY = "SELECT * FROM test_values;";
    static final long DURATION = 5 * 60 * 1000; // 5 min

    public static void main(String[] args) throws Exception {
        if (!DEBUG) {
            System.setOut(
                    new PrintStream(
                            new OutputStream() {
                                public void close() {}
                                public void flush() {}
                                public void write(byte[] b) {}
                                public void write(byte[] b, int off, int len) {}
                                public void write(int b) {}
                            }
                    )
            );
        }

        DataSourceFactory factory = new DataSourceFactory(
                new String[] {"192.168.7.92", "192.168.7.36"},
                "est", "est", "est");
//        Experiment experiment = new Experiment("test", factory.createHAJDBCDataSource("postgres", "postgres"), QUERY);
//        experiment.call();
        ExperimentExecutor executor = new ExperimentExecutor("ha-jdbc-4", 4, DURATION,
                factory.createHAJDBCDataSource("postgres", "postgres"), QUERY);
        executor.run();
    }

    private Main() {
        //avoid instantiation
    }

}
