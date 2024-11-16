package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import server.EchoServer;
import database.JDBC;
import entities.ConnectedClients;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller class for the server GUI.
 * Handles server start/stop operations and manages the server console.
 */
public class ServerController implements Initializable {

    /**
     * Text field for inputting the port number on which the server will listen.
     */
    @FXML private TextField portTextField;

    /**
     * Text field for inputting the database username for connection.
     */
    @FXML private TextField dbUsernameTextField;

    /**
     * Text field for inputting the database password for connection.
     */
    @FXML private TextField dbPasswordTextField;

    /**
     * Text area for displaying server console messages and logs.
     */
    @FXML private TextArea consoleTextArea;

    /**
     * Button for initiating the server start process.
     */
    @FXML private Button startButton;

    /**
     * Button for initiating the server stop process.
     */
    @FXML private Button stopButton;

    /**
     * ListView for displaying the list of currently connected clients.
     */
    @FXML private ListView<String> connectedClientsListView;

    /**
     * The instance of EchoServer that this controller manages.
     */
    private EchoServer server;

    /**
     * Initializes the controller.
     * This method is automatically called after the FXML file has been loaded.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        stopButton.setDisable(true);
    }

    /**
     * Handles the action of starting the server.
     * Attempts to connect to the database and start the server on the specified port.
     */
    @FXML
    private void handleStartServer() {
        String port = portTextField.getText();
        String dbUsername = dbUsernameTextField.getText();
        String dbPassword = dbPasswordTextField.getText();

        // Check input
        if (port.isEmpty() || dbUsername.isEmpty() || dbPassword.isEmpty()) {
            logToConsole("Please fill in all fields");
            return;
        }
        
        try {
        	// Creates the server instance for the use of start and stop the server
            server = new EchoServer(Integer.parseInt(port));
            // Allows the EchoServer to access the ServerController methods
            EchoServer.serverController = this;

            if (JDBC.connectionToDB(dbUsername, dbPassword, this)) {
                server.listen();
                startButton.setDisable(true);
                stopButton.setDisable(false);
                logToConsole("Server started on port " + port);
            } else {
                logToConsole("Failed to connect to the database");
            }
        } catch (Exception e) {
            logToConsole("Error starting server: " + e.getMessage());
        }
    }

    /**
     * Handles the action of stopping the server.
     * Closes the server and updates the UI accordingly.
     */
    @FXML
    private void handleStopServer() {
        if (server != null) {
            try {
                server.close();
                startButton.setDisable(false);
                stopButton.setDisable(true);
                logToConsole("Server stopped");
            } catch (IOException e) {
                logToConsole("Error stopping server: " + e.getMessage());
            }
        }
    }

    /**
     * Logs a message to the console text area.
     * This method is thread-safe and can be called from any thread.
     *
     * @param message The message to be logged to the console.
     */
    public void logToConsole(String message) {
        Platform.runLater(() -> consoleTextArea.appendText(message + "\n"));
    }

    /**
     * Updates the list of connected clients in the UI.
     * This method is thread-safe and can be called from any thread.
     */
    public void updateConnectedClients() {
        Platform.runLater(() -> {
            // Clear existing items
            connectedClientsListView.getItems().clear();
            // Add all connected clients to the ListView
            for (ConnectedClients client : EchoServer.connectedClients) {
                connectedClientsListView.getItems().add(
                    String.format("%s (%s) - %s", client.getHostName(), client.getIp(), client.getRole())
                );
            }
        });
    }
}