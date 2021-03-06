import net.sf.hajdbc.SimpleDatabaseClusterConfigurationFactory;
import net.sf.hajdbc.SynchronizationStrategy;
import net.sf.hajdbc.balancer.random.RandomBalancerFactory;
import net.sf.hajdbc.cache.lazy.SharedLazyDatabaseMetaDataCacheFactory;
import net.sf.hajdbc.dialect.postgresql.PostgreSQLDialectFactory;
import net.sf.hajdbc.durability.none.NoDurabilityFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfiguration;
import net.sf.hajdbc.state.simple.SimpleStateManagerFactory;
import net.sf.hajdbc.sync.PassiveSynchronizationStrategy;
import net.sf.hajdbc.util.concurrent.cron.CronExpression;
import net.sf.hajdbc.xml.XMLDatabaseClusterConfigurationFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSourceFactory {

    private final String[] serverName;
    private final String user;
    private final String password;
    private final String databaseName;

    public DataSourceFactory(String[] serverName, String user, String password, String databaseName) {
        this.serverName = serverName;
        this.user = user;
        this.password = password;
        this.databaseName = databaseName;
    }

    public DataSourceFactory() {
        this.serverName = new String[] {};
        this.user = "";
        this.password = "";
        this.databaseName = "";
    }

    public DataSource createPGSimpleDataSource() {
        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setServerName(this.serverName[0]);
        source.setUser(this.user);
        source.setPassword(this.password);
        source.setDatabaseName(this.databaseName);
        return source;
    }

    public DataSource createHAJDBCDataSource(String adminUser, String adminPassword) {
        return createHAJDBCDataSource(adminUser, adminPassword, "org.postgresql.ds.PGSimpleDataSource");
    }

    public DataSource createHAJDBCDataSource(String adminUser, String adminPassword, String location) {
        List<DataSourceDatabase> dbs = new ArrayList<DataSourceDatabase>();
        for (String server : this.serverName) {
            DataSourceDatabase db = new DataSourceDatabase();
            db.setId(server);
            db.setWeight(1);
            db.setLocation(location);
            db.setUser(adminUser);
            db.setPassword(adminPassword);
            db.setProperty("serverName", server);
            db.setProperty("user", this.user);
            db.setProperty("password", this.password);
            db.setProperty("databaseName", this.databaseName);
            dbs.add(db);
        }

        DataSourceDatabaseClusterConfiguration cluster = new DataSourceDatabaseClusterConfiguration();
        cluster.setDatabases(dbs);
        cluster.setDialectFactory(new PostgreSQLDialectFactory());
        cluster.setStateManagerFactory(new SimpleStateManagerFactory());
        Map<String, SynchronizationStrategy> syncs = new HashMap<String, SynchronizationStrategy>();
        syncs.put("passive", new PassiveSynchronizationStrategy());
        cluster.setSynchronizationStrategyMap(syncs);
        cluster.setDefaultSynchronizationStrategy("passive");
        cluster.setBalancerFactory(new RandomBalancerFactory());
        cluster.setDurabilityFactory(new NoDurabilityFactory());
        cluster.setDatabaseMetaDataCacheFactory(new SharedLazyDatabaseMetaDataCacheFactory());
        cluster.setSequenceDetectionEnabled(false);
        cluster.setIdentityColumnDetectionEnabled(false);
        try {
            cluster.setAutoActivationExpression(new CronExpression("*/15 * * ? * *"));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        net.sf.hajdbc.sql.DataSource source = new net.sf.hajdbc.sql.DataSource();
        source.setCluster("ha-cluster");
        source.setConfigurationFactory(new SimpleDatabaseClusterConfigurationFactory<DataSource, DataSourceDatabase>(cluster));

        return source;
    }

    public DataSource createHAJDBCPooledDataSource(String adminUser, String adminPassword) {
        List<DataSourceDatabase> dbs = new ArrayList<DataSourceDatabase>();
        for (String server : this.serverName) {
            DataSourceDatabase db = new DataSourceDatabase();
            db.setId(server);
            db.setWeight(1);
            db.setLocation("com.mchange.v2.c3p0.ComboPooledDataSource");
            db.setUser(adminUser);
            db.setPassword(adminPassword);
            db.setProperty("driverClass", "org.postgresql.Driver");
            db.setProperty("jdbcUrl", "jdbc:postgresql://" + server + "/" + this.databaseName);
            db.setProperty("user", this.user);
            db.setProperty("password", this.password);
            db.setProperty("acquireIncrement", "1");
            db.setProperty("initialPoolSize", "0");
            db.setProperty("maxIdleTimeExcessConnections", "5");
            db.setProperty("preferredTestQuery", "SELECT 1");
            db.setProperty("testConnectionOnCheckin", "true");
            db.setProperty("usesTraditionalReflectiveProxies", "true");
            dbs.add(db);
        }

        DataSourceDatabaseClusterConfiguration cluster = new DataSourceDatabaseClusterConfiguration();
        cluster.setDatabases(dbs);
        cluster.setDialectFactory(new PostgreSQLDialectFactory());
        cluster.setStateManagerFactory(new SimpleStateManagerFactory());
        Map<String, SynchronizationStrategy> syncs = new HashMap<String, SynchronizationStrategy>();
        syncs.put("passive", new PassiveSynchronizationStrategy());
        cluster.setSynchronizationStrategyMap(syncs);
        cluster.setDefaultSynchronizationStrategy("passive");
        cluster.setBalancerFactory(new RandomBalancerFactory());
        cluster.setDurabilityFactory(new NoDurabilityFactory());
        cluster.setDatabaseMetaDataCacheFactory(new SharedLazyDatabaseMetaDataCacheFactory());
        cluster.setSequenceDetectionEnabled(false);
        cluster.setIdentityColumnDetectionEnabled(false);
        try {
            cluster.setAutoActivationExpression(new CronExpression("*/15 * * ? * *"));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        net.sf.hajdbc.sql.DataSource source = new net.sf.hajdbc.sql.DataSource();
        source.setCluster("ha-cluster");
        source.setConfigurationFactory(new SimpleDatabaseClusterConfigurationFactory<DataSource, DataSourceDatabase>(cluster));

        return source;
    }

}
