
import net.sf.hajdbc.pool.sql.ConnectionFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfiguration;
import net.sf.hajdbc.xml.XMLDatabaseClusterConfigurationFactory;
import org.apache.commons.dbcp.PoolingDriver;

import javax.sql.DataSource;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        ConnectionFactory factory = createHaJdbcConnectionFactory("ha-jdbc-test-cluster.xml");
//        ConnectionFactory factory = createC3P0PooledConnectionFactory();
//        ConnectionFactory factory = createDBCPPooledConnectionFactory();

//        Experiment experiment = new Experiment(factory, QUERY);
//        experiment.call();
        ExperimentExecutor executor = new ExperimentExecutor("ha-jdbc-account-2", 2, DURATION, factory, QUERY);
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
//        connectionFactory = new ThreadConnectionFactory(connectionFactory);
        return connectionFactory;
    }

    private static ConnectionFactory createC3P0PooledConnectionFactory() {
        DataSourceFactory factory = new DataSourceFactory(new String[] {"192.168.7.92", "192.168.7.36"},
                "est", "est", "est");
        DataSource source = factory.createHAJDBCPooledDataSource("postgres", "postgres");
        return new ThreadConnectionFactory(new DataSourceWrapper(source));
    }

    private static ConnectionFactory createDBCPPooledConnectionFactory() throws SQLException {
        DriverManager.registerDriver(new PoolingDriver());
        return new DriverConnectionFactory("jdbc:ha-jdbc:test-cluster-dbcp-pool", "est", "est");
    }

    private Main() {
        //avoid instantiation
    }

}
