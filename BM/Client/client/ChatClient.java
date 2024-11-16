package client;

import java.io.IOException;
import java.util.HashMap;

import entities.Message;
import entities.User;
import ocsf.client.AbstractClient;

/**
 * A class that extends the `AbstractClient` class for network communication.
 * This class handles the client-side operations for the Bite Me application.
 */
public class ChatClient extends AbstractClient {
	
	/**
	 * The interface type variable. It allows the implementation of the display
	 * method in the client.
	 */
    ChatIF clientUI;

	/**
     * A static flag indicating whether the client is currently awaiting a response from the server.
     */
    public static boolean awaitResponse = false;

    /**
     * A static HashMap that stores messages received from the server.
     */
    public static HashMap<String, Message<?>> serverMsg;

    /**
     * A static User object that represents the currently logged-in user.
     */
    public static User currentUser;
    

    /**
     * Constructs an instance of the client controller.
     *
     * @param host The server to connect to.
     * @param port The port number to connect on.
     * @param clientUI The interface type variable.
     */
    public ChatClient(String host, int port, ChatIF clientUI) throws IOException {
        super(host, port);
        this.clientUI = clientUI;
        serverMsg = new HashMap<>();
    }
    
	/**
	 * Initiates the client's connection process to the server.
	 *
	 * @throws IOException If an I/O error occurs while connecting to the server.
	 */
    public void join() throws IOException {
        openConnection();
        System.out.println("Connection opened to server: " + getHost() + ":" + getPort());    }

    /**
     * Handles messages received from the server.
     *
     * @param msg The message received from the server.
     */
    @Override
    protected void handleMessageFromServer(Object msg) {
        if (msg instanceof Message) {
            Message<?> messageFromServer = (Message<?>) msg;
            String message = messageFromServer.getMessage();
            switch (message) {
                case "LOGIN_SUCCESS":
                    currentUser = (User) messageFromServer.getType();
                    serverMsg.put("LOGIN", messageFromServer);
                    System.out.println("User logged in: " + currentUser.getUserId());
                    break;
                case "LOGIN_FAILED":
                case "USER_ALREADY_LOGGED_IN":
                    serverMsg.put("LOGIN", messageFromServer);
                    break;
                case "LOGOUT_SUCCESS":
                    serverMsg.put("LOGOUT", messageFromServer);
                    currentUser = null;
                    break;
                case "LOGOUT_FAILED":
                    serverMsg.put("LOGOUT", messageFromServer);
                    if (message.equals("LOGOUT_SUCCESS")) {
                        currentUser = null;
                    }
                    break;
                case "REPORT_MANAGEMENT_RESPONSE":
                    serverMsg.put("REPORT_MANAGEMENT", messageFromServer);
                    break;
                case "NEW_CUSTOMER_REGISTRATION_SUCCESS":
                case "NEW_CUSTOMER_REGISTRATION_FAILED":
                case "NEW_CUSTOMER_REGISTRATION_ERROR":
                    serverMsg.put("NEW_CUSTOMER_REGISTRATION", messageFromServer);
                    break;
                case "GET_RESTAURANTS_RESPONSE":
                    serverMsg.put("GET_RESTAURANTS", messageFromServer);
                    break;
                case "GET_MENU_ITEMS_RESPONSE":
                    serverMsg.put("GET_MENU_ITEMS", messageFromServer);
                    break;
                case "ORDER_PLACED_SUCCESSFULLY":
                case "ORDER_PLACEMENT_FAILED":
                    serverMsg.put("PLACE_ORDER", messageFromServer);
                    break;
                case "GET_CUSTOMER_ORDERS_RESPONSE":
                    serverMsg.put("GET_CUSTOMER_ORDERS", messageFromServer);
                    break;
				case "RESTAURANT_ORDERS_RESPONSE":
               	 serverMsg.put("RESTAURANT_ORDERS",messageFromServer);
                   break;
				case "UPDATE_ORDER_STATUS":
				    serverMsg.put("UPDATE_ORDER_STATUS_RESPONSE", messageFromServer);
				    break;
                case "IncomeReportResponse":
                	serverMsg.put("IncomeReportResponse", messageFromServer);
                	break;
                case "OrderReportResponse":
                	serverMsg.put("OrderReportResponse", messageFromServer);
                	break;
                case "PerformanceReportResponse":
                	serverMsg.put("PerformanceReportResponse", messageFromServer);
                	break;
                case "QuarterlyReportResponse":
                	serverMsg.put("QuarterlyReportResponse", messageFromServer);
                	break;
                case "UPDATE_MENU_ITEM_RESPONSE":
                    serverMsg.put("UPDATE_MENU_ITEM_RESPONSE", messageFromServer);
                    break;
                case "ITEM_UPDATED":
                case "ITEM_NOT_FOUND":
                case "UPDATE_FAILED":
                    serverMsg.put("UPDATE_MENU_ITEM_RESPONSE", messageFromServer);
                    break;
                default:
                    serverMsg.put(messageFromServer.getMessage(), messageFromServer);
                    break;
            }
        }
        awaitResponse = false;
    }

    /**
     * Sends a message to the server and waits for a response.
     *
     * @param message The message to send to the server.
     */
    public void sendToServer(Message<?> message) {
        try {
            awaitResponse = true;
            super.sendToServer(message);
            
            // Wait for response
            while (awaitResponse) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the most recent response from the server for a given message type.
     *
     * @param messageType The type of message to retrieve the response for.
     * @return The Message object containing the server's response, or null if no response is found.
     */
    public Message<?> getResponse(String messageType) {
    	System.out.println(messageType);
        return serverMsg.get(messageType);
    }
}