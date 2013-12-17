
import net.sf.hajdbc.DatabaseClusterFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfiguration;
import net.sf.hajdbc.xml.XMLDatabaseClusterConfigurationFactory;

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

        ConnectionSource source =createHaJdbcConnectionSource("ha-jdbc-test-cluster.xml");
//        Experiment experiment = new Experiment(source, QUERY);
//        experiment.call();
        ExperimentExecutor executor = new ExperimentExecutor("ha-jdbc-account-2", 2, DURATION,
                source, QUERY);
        executor.run();
        System.exit(0); // to force stopping of ha-jdbc threads
    }

    private static ConnectionSource createHaJdbcConnectionSource(String configResource) {
        net.sf.hajdbc.sql.DataSource source = new net.sf.hajdbc.sql.DataSource();
        source.setCluster("ha-cluster");
        source.setConfigurationFactory(new XMLDatabaseClusterConfigurationFactory<DataSource, DataSourceDatabase>(
                DataSourceDatabaseClusterConfiguration.class, "ha-cluster", configResource
        ));
        ConnectionSource connectionSource = new DataSourceWrapper(source);
//        connectionSource = new ThreadConnectionSource(connectionSource, source.getDatabaseCluster());
        connectionSource = new ThreadConnectionSource(connectionSource);
        return connectionSource;
    }

    private Main() {
        //avoid instantiation
    }

}
