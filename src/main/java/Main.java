
import net.sf.hajdbc.pool.sql.ConnectionFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfiguration;
import net.sf.hajdbc.xml.XMLDatabaseClusterConfigurationFactory;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.io.PrintStream;

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

        ConnectionFactory source = createHaJdbcConnectionFactory("ha-jdbc-test-cluster.xml");
//        Experiment experiment = new Experiment(source, QUERY);
//        experiment.call();
        ExperimentExecutor executor = new ExperimentExecutor("ha-jdbc-account-2", 2, DURATION,
                source, QUERY);
        executor.run();
        System.exit(0); // to force stopping of ha-jdbc threads
    }

    private static ConnectionFactory createHaJdbcConnectionFactory(String configResource) {
        net.sf.hajdbc.sql.DataSource source = new net.sf.hajdbc.sql.DataSource();
        source.setCluster("ha-cluster");
        source.setConfigurationFactory(new XMLDatabaseClusterConfigurationFactory<DataSource, DataSourceDatabase>(
                DataSourceDatabaseClusterConfiguration.class, "ha-cluster", configResource
        ));
        ConnectionFactory connectionFactory = new DataSourceWrapper(source);
//        connectionSource = new ThreadConnectionFactory(connectionSource, source.getDatabaseCluster());
        connectionFactory = new ThreadConnectionFactory(connectionFactory);
        return connectionFactory;
    }

    private Main() {
        //avoid instantiation
    }

}
