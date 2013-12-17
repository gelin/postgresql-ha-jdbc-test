import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.DatabaseClusterListener;
import net.sf.hajdbc.pool.sql.ConnectionFactory;
import net.sf.hajdbc.state.DatabaseEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadConnectionFactory implements ConnectionFactory {

    private final ConnectionFactory factory;
    private boolean reconnect;

    public ThreadConnectionFactory(ConnectionFactory factory) {
        this.factory = factory;
    }

    public ThreadConnectionFactory(ConnectionFactory factory, DatabaseCluster cluster) {
        this(factory);
        cluster.addListener(new ClusterListener());
    }

    @Override
    public synchronized Connection getConnection() {
        return getConnection(this);
    }

    private class ClusterListener implements DatabaseClusterListener {
        @Override
        public synchronized void activated(DatabaseEvent event) {
            ThreadConnectionFactory.this.reconnect = true;
            System.err.println("activated " + event);
        }
        @Override
        public void deactivated(DatabaseEvent event) {
            //nothing to do
            System.err.println("deactivated " + event);
        }
    }

    static ThreadLocal<Connection> threadConnection = null;

    private static synchronized Connection getConnection(final ThreadConnectionFactory factory) {
        if (threadConnection == null || factory.reconnect) {
            threadConnection = new ThreadLocal<Connection>() {
                @Override
                protected Connection initialValue() {
                    try {
                        System.err.println("opening a new connection for " + Thread.currentThread());
                        return factory.factory.getConnection();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }
        return threadConnection.get();
    }

}
