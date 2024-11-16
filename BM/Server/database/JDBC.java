package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import controller.ServerController;
import server.EchoServer;

/**
 * Class JDBC For Connecting to Database
 */
public class JDBC {

    /** Static entity for the connection to database */
    public static Connection connection;

    /**
     * Method to connect to db
     *
     * @param db_username username for database
     * @param db_password password for database
     *
     * @return true if connection success, otherwise false
     */
    public static boolean connectionToDB(String db_username, String db_password, ServerController controller) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            controller.logToConsole("Driver definition succeed");
        } catch (Exception ex) {
            controller.logToConsole("Driver definition failed: " + ex.getMessage());
            return false;
        }

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost/biteme?serverTimezone=UTC", db_username, db_password);
            controller.logToConsole("SQL connection succeed");
        } catch (SQLException ex) {
            controller.logToConsole("SQL connection Failed");
            controller.logToConsole("SQLException: " + ex.getMessage());
            controller.logToConsole("SQLState: " + ex.getSQLState());
            controller.logToConsole("VendorError: " + ex.getErrorCode());
            return false;
        }
        return true;
    }

    /**
     * Method to get the database connection
     *
     * @return Connection object
     * @throws SQLException if connection is null or closed
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Database connection is not established.");
        }
        return connection;
    }

    /**
     * Method to close the database connection
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                EchoServer.serverController.logToConsole("Database connection closed successfully");
            }
        } catch (SQLException e) {
            EchoServer.serverController.logToConsole("Error closing database connection: " + e.getMessage());
        }
    }
}