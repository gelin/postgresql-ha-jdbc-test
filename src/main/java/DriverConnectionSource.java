import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DriverConnectionSource {

    private final String connectionUrl;
    private final String user;
    private final String password;

    public DriverConnectionSource(String connectionUrl, String user, String password) {
        this.connectionUrl = connectionUrl;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.connectionUrl, this.user, this.password);
    }

}
