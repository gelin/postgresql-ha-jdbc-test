import net.sf.hajdbc.pool.sql.ConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceWrapper implements ConnectionFactory {

    private final DataSource source;

    public DataSourceWrapper(DataSource source) {
        this.source = source;
    }

    public Connection getConnection() throws SQLException {
        return this.source.getConnection();
    }

}
