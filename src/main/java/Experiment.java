import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

public class Experiment implements Callable<Experiment.Result> {

    public static class Result {

        public final int rows;
        public final long millis;
        private Result(int rows, long millis) {
            this.rows = rows;
            this.millis = millis;
        }

    }

    private final String label;
    private final DataSource source;
    private final String query;

    public Experiment(String label, DataSource source, String query) {
        this.label = label;
        this.source = source;
        this.query = query;
    }

    @Override
    public Result call() throws Exception {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = this.source.getConnection();
            long start = System.currentTimeMillis();
            statement = connection.createStatement();
            ResultSet result = statement.executeQuery(this.query);
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
            System.err.println(this.label + ": selected " + count + " rows in " + (end - start) / 1000.0 + " secs");
            return new Result(count, end - start);
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
        return new Result(0, 0);
    }

}
