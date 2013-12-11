
import javax.sql.DataSource;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.*;

public class Main {

    static final boolean DEBUG = false;
    static final String QUERY = "SELECT * FROM porter.main_account;";
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

//        ConnectionSource source = new DriverConnectionSource("jdbc:ha-jdbc:test-cluster", "est", "est");
        DataSourceFactory factory = new DataSourceFactory();
        ConnectionSource source = new DataSourceWrapper(factory.createHAJDBCDataSource("ha-jdbc-test-cluster.xml"));
        Experiment experiment = new Experiment(source, QUERY);
        experiment.call();
//        ExperimentExecutor executor = new ExperimentExecutor("ha-jdbc-account-2", 2, DURATION,
//                source, QUERY);
//        executor.run();
        System.exit(0); // to force stopping of ha-jdbc threads
    }

    private Main() {
        //avoid instantiation
    }

}
