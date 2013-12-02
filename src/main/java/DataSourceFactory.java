import net.sf.hajdbc.SimpleDatabaseClusterConfigurationFactory;
import net.sf.hajdbc.cache.simple.SimpleDatabaseMetaDataCacheFactory;
import net.sf.hajdbc.dialect.postgresql.PostgreSQLDialectFactory;
import net.sf.hajdbc.sql.DataSourceDatabase;
import net.sf.hajdbc.sql.DataSourceDatabaseClusterConfiguration;
import net.sf.hajdbc.state.simple.SimpleStateManagerFactory;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

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
        cluster.setDatabaseMetaDataCacheFactory(new SimpleDatabaseMetaDataCacheFactory());
        cluster.setStateManagerFactory(new SimpleStateManagerFactory());

        net.sf.hajdbc.sql.DataSource source = new net.sf.hajdbc.sql.DataSource();
        source.setCluster("ha-cluster");
        source.setConfigurationFactory(new SimpleDatabaseClusterConfigurationFactory<DataSource, DataSourceDatabase>(cluster));

        return source;
    }

}
