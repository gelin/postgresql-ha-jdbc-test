import net.sf.hajdbc.DatabaseCluster;
import net.sf.hajdbc.DatabaseClusterListener;
import net.sf.hajdbc.state.DatabaseEvent;

import java.sql.Connection;
import java.sql.SQLException;

public class ThreadConnectionSource implements ConnectionSource {

    private final ConnectionSource source;
    private boolean reconnect;

    public ThreadConnectionSource(ConnectionSource source, DatabaseCluster cluster) {
        this.source = source;
        cluster.addListener(new ClusterListener());
    }

    @Override
    public synchronized Connection getConnection() {
        return getConnection(this);
    }

    private class ClusterListener implements DatabaseClusterListener {
        @Override
        public synchronized void activated(DatabaseEvent event) {
            ThreadConnectionSource.this.reconnect = true;
            System.err.println("activated " + event);
        }
        @Override
        public void deactivated(DatabaseEvent event) {
            //nothing to do
            System.err.println("deactivated " + event);
        }
    }

    static ThreadLocal<Connection> threadConnection = null;

    private static synchronized Connection getConnection(final ThreadConnectionSource source) {
        if (threadConnection == null || source.reconnect) {
            threadConnection = new ThreadLocal<Connection>() {
                @Override
                protected Connection initialValue() {
                    try {
                        System.err.println("opening a new connection for " + Thread.currentThread());
                        return source.source.getConnection();
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
