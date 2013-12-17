import net.sf.hajdbc.pool.sql.ConnectionFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverConnectionFactory implements ConnectionFactory {

    private final String connectionUrl;
    private final String user;
    private final String password;

    public DriverConnectionFactory(String connectionUrl, String user, String password) {
        this.connectionUrl = connectionUrl;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.connectionUrl, this.user, this.password);
    }

}
