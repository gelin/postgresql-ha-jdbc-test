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

    static RunResult run_query(Connection connection) throws SQLException {
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
                for (int i = 0; i < columns; i++) {
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

    static void run_experiment(Connection connection, String label) throws SQLException {
        System.err.println("selecting " + label + "...");
        int total_rows = 0;
        long total_time = 0;
        long min_time = Long.MAX_VALUE;
        long max_time = 0;
        for (int i = 0; i < REPEAT; i++) {
            RunResult result = run_query(connection);
            total_rows += result.rows;
            total_time += result.millis;
            min_time = Math.min(min_time, result.millis);
            max_time = Math.max(max_time, result.millis);
        }
        System.err.println(label + ": " + total_time / REPEAT / 1000.0 + " average time of the experiment");
        System.err.println(label + ": " + min_time + " min time of the experiment");
        System.err.println(label + ": " + max_time + " max time of the experiment");
        System.err.println(label + ": " + total_time / total_rows + " average time for a row");
    }

    public static void main(String[] args) throws SQLException {
        Connection connection = DriverManager.getConnection("jdbc:ha-jdbc:localhost", "", "");

        run_experiment(connection, "ha-jdbc");

        connection.close();
    }

}
