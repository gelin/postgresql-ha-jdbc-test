import net.sf.hajdbc.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

import java.sql.*;

public class Main {

    static final String QUERY = "SELECT * FROM test_values;";
    static final int REPEAT = 10;

    static class RunResult {
        private final int rows;
        private final long millis;

        public RunResult(int rows, long millis) {
            this.rows = rows;
            this.millis = millis;
        }
    }

    static RunResult runQuery(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            long start = System.currentTimeMillis();
            statement = connection.createStatement();
            ResultSet result = statement.executeQuery(QUERY);
            int count = 0;
            int columns = result.getMetaData().getColumnCount();
            StringBuffer printable = new StringBuffer();
            while (result.next()) {
                printable.setLength(0);
                for (int i = 1; i <= columns; i++) {
                    String value = result.getString(i);
                    printable.append("data");
                    printable.append(i);
                    printable.append(": ");
                    printable.append(value);
                    printable.append("\t");
                }
                System.out.println(printable.toString());
                count++;
            }
            long end = System.currentTimeMillis();
            System.err.println("selected " + count + " rows in " + (end - start) / 1000.0 + " secs");
            return new RunResult(count, end - start);
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
        return new RunResult(0, 0);
    }

    static void runExperiment(Connection connection, String label) throws SQLException {
        System.err.println("selecting " + label + "...");
        int totalRows = 0;
        long totalTime= 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;
        for (int i = 0; i < REPEAT; i++) {
            RunResult result = runQuery(connection);
            totalRows += result.rows;
            totalTime += result.millis;
            minTime = Math.min(minTime, result.millis);
            maxTime = Math.max(maxTime, result.millis);
        }
        System.err.println(label + ": " + (double)totalTime / REPEAT / 1000.0 + " average time of the experiment");
        System.err.println(label + ": " + minTime + " min time of the experiment");
        System.err.println(label + ": " + maxTime + " max time of the experiment");
        System.err.println(label + ": " + (double)totalTime / totalRows + " average time for a row");
    }

    public static void main(String[] args) throws SQLException {
//        DataSource source = new DataSource();
//        source.setConfig("ha-jdbc-localhost.xml");
//        Connection connection = source.getConnection();

//        runExperiment(connection, "ha-jdbc");

        PGSimpleDataSource source = new PGSimpleDataSource();
        source.setServerName("localhost");
        source.setUser("gelin");
        source.setPassword("gelin");
        source.setDatabaseName("json_test");
        Connection connection = source.getConnection();

        runExperiment(connection, "pg-simple-ds");

        connection.close();
    }

}
