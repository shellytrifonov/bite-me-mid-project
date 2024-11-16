package server;

import java.io.IOException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
import controller.ServerController;
import entities.ConnectedClients;
import entities.MenuItem;
import entities.Message;
import entities.Order;
import entities.Restaurant;
import entities.User;
import database.DataBaseController;

/**
 * The EchoServer class extends AbstractServer to handle client-server communication
 * for the Bite Me system. It processes incoming messages from clients and manages
 * connected users.
 */
public class EchoServer extends AbstractServer {
	
    /** Reference to the ServerController for logging and UI updates */
    public static ServerController serverController;
    
    /** List of currently connected clients */
    public static ArrayList<ConnectedClients> connectedClients = new ArrayList<>();

    /**
     * Constructs an EchoServer that listens on the specified port.
     *
     * @param port The port number to listen on.
     */
    public EchoServer(int port) {
        super(port);
    }
    
    /**
     * Method called each time a new client connection is accepted.
     * Logs the event to the server console.
     */
    @Override
    protected void serverStarted() {
        serverController.logToConsole("Server listening for connections on port " + getPort());
    }

    /**
     * Method called each time the server stops accepting connections.
     * Logs the event to the server console.
     */
    @Override
    protected void serverStopped() {
        serverController.logToConsole("Server has stopped listening for connections.");
    }

    /**
     * Handles messages received from the client.
     *
     * @param msg The message received from the client.
     * @param client The connection from which the message originated.
     */
    @Override
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
        System.out.println("Received message from client: " + msg);
        serverController.logToConsole("Message received: " + msg + " from " + client);
        if (msg instanceof Message) {
            Message<?> message = (Message<?>) msg;
            switch (message.getMessage()) {
                case "LOGIN":
                    handleLogin(message, client);
                    break;
                case "LOGOUT":
                    handleLogout(message, client);
                    break;
                case "REPORT_MANAGEMENT":
                    handleManageReport(message, client);
                    break;
                case "NEW_CUSTOMER_REGISTRATION":
                    handleNewCustomerRegistration(message, client);
                case "GET_RESTAURANTS":
                    handleGetRestaurants(client);
                    break;
                case "GET_MENU_ITEMS":
                    handleGetMenuItems(message, client);
                    break;
                case "PLACE_ORDER":
                    handlePlaceOrder(message, client);
                    break;
                case "GET_CUSTOMER_ORDERS":
                    handleGetCustomerOrders(message, client);
                    break;
				case "RESTAURANT_ORDERS":
                	handleGetRestaurantOrders(message, client);
                    break;
				case "UPDATE_ORDER_STATUS":
					handleUpdateOrderStatus(message, client);
                    break;
                case "IncomeReport":
                	handleIncomeReport(message,client);
                	break;
                case "OrdersReport":
                	handleOrderReport(message,client);
                	break;
                case "PerformanceReport":
                	handlePerformenceReport(message,client);
                	break;
                case "QuarterlyReport":
                	handleQuarterlyReport(message,client);
                	break;
				case "UPDATE_MENU_ITEM":
                    handleUpdateMenuItem(message, client);
                    break;
                default:
                    serverController.logToConsole("Unknown message type: " + message.getMessage());
            }
        }
    }

    /**
     * Handles the login process for a client.
     *
     * @param message The login message from the client.
     * @param client The connection from which the login request originated.
     */
    private void handleLogin(Message<?> message, ConnectionToClient client) {
    	System.out.println("In handleLogin");
        User user = (User) message.getType();
        System.out.println("Attempting to validate user: " + user.getUserId());
        Message<User> response = DataBaseController.getInstance().checkUserLogin(user.getUserId(), user.getPassword());
        System.out.println("Database response: " + response.getMessage());

        if (response.getMessage().equals("LOGIN_SUCCESS")){
            ConnectedClients connectedClient = new ConnectedClients(
                    client.getInetAddress().getHostAddress(),
                    client.getInetAddress().getHostName(),
                    user.getUserId(),
                    user.getRole()
                );
                connectedClients.add(connectedClient);
                serverController.updateConnectedClients();
                response = new Message<>("LOGIN_SUCCESS", response.getType());
           }
        else if (response.getMessage().equals("User is already logged in")) {
            response = new Message<>("USER_ALREADY_LOGGED_IN", null);
        }
        else {
            response = new Message<>("LOGIN_FAILED", null);
        }
        
        try {
        	System.out.println("Send to client the response "+response);
            client.sendToClient(response);
        } catch (IOException e) {
            serverController.logToConsole("Error sending response to client: " + e.getMessage());
        }
    }

    /**
     * Handles the logout process for a client.
     *
     * @param message The logout message from the client.
     * @param client The connection from which the logout request originated.
     */
    private void handleLogout(Message<?> message, ConnectionToClient client) {
        User user = (User) message.getType();
        System.out.println("Handling logout for user: " + user.getUserId());
        Message<Void> dbResponse = DataBaseController.getInstance().logoutUser(user.getUserId());
        if (dbResponse.getMessage().equals("User logged out successfully")) {
            connectedClients.removeIf(c -> c.getId().equals(user.getUserId()));
            serverController.updateConnectedClients();
            try {
                Message<String> response = new Message<>("LOGOUT_SUCCESS", "User logged out successfully");
                System.out.println("Sending logout response: " + response);
                client.sendToClient(response);
            } catch (IOException e) {
                System.out.println("Error sending response to client: " + e.getMessage());
                serverController.logToConsole("Error sending response to client: " + e.getMessage());
            }
        } else {
            try {
                Message<String> response = new Message<>("LOGOUT_FAILED", "Failed to log out user");
                System.out.println("Sending logout response: " + response);
                client.sendToClient(response);
            } catch (IOException e) {
                System.out.println("Error sending response to client: " + e.getMessage());
                serverController.logToConsole("Error sending response to client: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handles a report management request from a client.
     * This method processes the request and sends the response back to the client.
     * @param message The Message object containing the report management request
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handleManageReport(Message<?> message, ConnectionToClient client) {
        String reportType = (String) message.getType();
        Message<String> response = DataBaseController.getInstance().handleReportManagement(reportType);
        try {
            client.sendToClient(response);
        } catch (IOException e) {
            serverController.logToConsole("Error sending response to client: " + e.getMessage());
        }
    }

    /**
     * Handles a new customer registration request from a client.
     * This method processes the registration request and sends the response back to the client.
     * @param message The Message object containing the new customer details
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handleNewCustomerRegistration(Message<?> message, ConnectionToClient client) {
    	User newUser = (User) message.getType();
        Message<String> response = DataBaseController.getInstance().handleNewCustomerRegistration(newUser);
        try {
            client.sendToClient(response);
        } catch (IOException e) {
            serverController.logToConsole("Error sending response to client: " + e.getMessage());
        }
    }
    
    /**
     * Handles a request to get all restaurants.
     * This method retrieves all restaurants from the database and sends them back to the client.
     *
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handleGetRestaurants(ConnectionToClient client) {
        Message<List<Restaurant>> response = DataBaseController.getInstance().loadRestaurants();
        System.out.println("Server: Loaded restaurants: " + response.getType());
        try {
            client.sendToClient(new Message<>("GET_RESTAURANTS_RESPONSE", response.getType()));
            System.out.println("Server: Sent restaurants to client");
        } catch (IOException e) {
            System.out.println("Server: Error sending response to client: " + e.getMessage());
            serverController.logToConsole("Error sending response to client: " + e.getMessage());
        }
    }

    /**
     * Handles a request to get menu items for a specific restaurant.
     * This method retrieves all menu items for the given restaurant from the database 
     * and sends them back to the client.
     *
     * @param message The Message object containing the restaurant details
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handleGetMenuItems(Message<?> message, ConnectionToClient client) {
        System.out.println("EchoServer: Handling GET_MENU_ITEMS request");
        if (message.getType() instanceof String) {
            String restaurantId = (String) message.getType();
            System.out.println("EchoServer: Fetching menu items for restaurant ID: " + restaurantId);
            Message<List<MenuItem>> response = DataBaseController.getInstance().loadItems(new Restaurant(restaurantId, null, null, null, null, null));
            System.out.println("EchoServer: Received response from DataBaseController: " + response);

            try {
                if (response.getType() != null) {
                    System.out.println("EchoServer: Sending " + response.getType().size() + " menu items to client");
                    client.sendToClient(new Message<>("GET_MENU_ITEMS_RESPONSE", response.getType()));
                } else {
                    System.out.println("EchoServer: No menu items found or error occurred, sending null to client");
                    client.sendToClient(new Message<>("GET_MENU_ITEMS_RESPONSE", null));
                }
            } catch (IOException e) {
                System.out.println("EchoServer: Error sending menu items to client: " + e.getMessage());
                e.printStackTrace();
                serverController.logToConsole("Error sending response to client: " + e.getMessage());
            }
        } else {
            System.out.println("EchoServer: Invalid restaurant data received for GET_MENU_ITEMS: " + message.getType());
            serverController.logToConsole("Invalid restaurant data received for GET_MENU_ITEMS");
        }
    }

    /**
     * Handles a request to place a new order.
     * This method processes the order details, saves the new order to the database,
     * and sends a confirmation or failure message back to the client.
     *
     * @param message The Message object containing the new order details
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handlePlaceOrder(Message<?> message, ConnectionToClient client) {
        if (message.getType() instanceof Order) {
            Order newOrder = (Order) message.getType();
            Message<String> response = DataBaseController.getInstance().newOrder(newOrder);
            try {
                if (response.getMessage().startsWith("New order registered successfully")) {
                    client.sendToClient(new Message<>("ORDER_PLACED_SUCCESSFULLY", response.getMessage()));
                } else {
                    client.sendToClient(new Message<>("ORDER_PLACEMENT_FAILED", response.getMessage()));
                }
            } catch (IOException e) {
                serverController.logToConsole("Error sending response to client: " + e.getMessage());
            }
        } else {
            serverController.logToConsole("Invalid order data received for PLACE_ORDER");
        }
    }
    
    /**
     * Handles the retrieval of customer orders and sends the results to the client.
     * 
     * This method processes a request to retrieve orders for a specific customer. 
     * It extracts the customer ID from the incoming message, calls the `getCustomerOrders` 
     * method of the `DataBaseController` to fetch the orders, and then sends the 
     * resulting list of orders back to the client. If an error occurs while sending 
     * the response, the error is logged to the server console.
     * 
     * @param message the message containing the customer ID for which to retrieve orders.
     * @param client  the client connection to which the response should be sent.
     */
    private void handleGetCustomerOrders(Message<?> message, ConnectionToClient client) {
        String customerId = (String) message.getType();
        Message<List<Order>> response = DataBaseController.getInstance().getCustomerOrders(customerId);
        try {
            client.sendToClient(new Message<>("GET_CUSTOMER_ORDERS_RESPONSE", response.getType()));
        } catch (IOException e) {
            serverController.logToConsole("Error sending customer orders to client: " + e.getMessage());
        }
    }

	 /**
	 * Handles a request to retrieve a list of orders for a given restaurant.
	 * This method expects the message type to be an integer representing the restaurant ID.
	 * It fetches the orders from the database controller based on the provided restaurant ID.
	 * The retrieved orders are sent back to the client as a response message.
	 * @param message The incoming message containing the restaurant ID as an integer.
	 * @param client The connection to the client.
	 */
	private void handleGetRestaurantOrders(Message<?> message, ConnectionToClient client) {
	    String restaurantId = (String) message.getType();
	    System.out.println("Fetching orders for restaurant ID: " + restaurantId);
	    Restaurant restaurant = new Restaurant(restaurantId,null,null,null,null,null);
		Message<List<Order>> response = DataBaseController.getInstance().handleGetRestaurantOrders(restaurant);
		System.out.println("restaurant orders response: " + response);
		try {
	        client.sendToClient(new Message<>("RESTAURANT_ORDERS_RESPONSE",response.getType()));
	        System.out.println("Sent restaurant orders to client for restaurant ID: " + restaurantId);
	    } catch (IOException e) {
	        serverController.logToConsole("Error sending response to client: " + e.getMessage());
	    }
	}
	
	/**
	 * Handles the request to update an order's status.
	 * This method updates the status of an order in the database and sends the result back to the client.
	 *
	 * @param message The Message object containing the order ID and new status
	 * @param client The ConnectionToClient object representing the client connection
	 */
	private void handleUpdateOrderStatus(Message<?> message, ConnectionToClient client) {
	    try {
	        // Extract parameters from the message
	        Object[] params = (Object[]) message.getType();
	        int orderId = (int) params[0];
	        String status = params[1].toString();

	        // Handle the update in the database
	        Message<String> response = DataBaseController.getInstance().handleUpdateOrderStatus(orderId, status);

	        // Send the response back to the client
	        client.sendToClient(new Message<>("UPDATE_ORDER_STATUS_RESPONSE", response.getMessage()));
	    } catch (ClassCastException e) {
	        serverController.logToConsole("Error: Invalid data types in order status update request - " + e.getMessage());
	        try {
	            client.sendToClient(new Message<>("UPDATE_ORDER_STATUS_RESPONSE", "Invalid data types in the request."));
	        } catch (IOException ioException) {
	            serverController.logToConsole("Error sending error message to client: " + ioException.getMessage());
	        }
	    } catch (IOException e) {
	        serverController.logToConsole("Error sending update order status response to client: " + e.getMessage());
	    } catch (Exception e) {
	        serverController.logToConsole("Unexpected error while updating order status: " + e.getMessage());
	        try {
	            client.sendToClient(new Message<>("UPDATE_ORDER_STATUS_RESPONSE", "Unexpected error occurred."));
	        } catch (IOException ioException) {
	            serverController.logToConsole("Error sending error message to client: " + ioException.getMessage());
	        }
	    }
	}


    /*
     * Handles the Quarterly report request from a client.
     * This method processes the request, retrieves the necessary data from the database,
     * and sends back the income report to the client.
     *
     * @param message The message containing the request details, including date range.
     * @param client The connection to the client that sent the request.
     */
     private void handleQuarterlyReport(Message<?> message, ConnectionToClient client) {
     	Object[] params = (Object[]) message.getType();
         int quarter = (int) params[0];
         int year = (int) params[1];
         String branch = (String)params[2];
       
 		try {
 			Map<String, Object>  reportData = DataBaseController.getInstance().getQuarterlyReportData(quarter, year,branch);
 			
 	        Message<Map<String, Object>> response = new Message<>("QuarterlyReportResponse", reportData);
 	        if(response!=null){
 	        	System.out.println("The Quarterly respone is not null ");
 	        }
 	       if(response.getType()!=null){
	        	System.out.println("The Quarterly respone type is not null ");
	        }
 			client.sendToClient(response);

 		} catch (IOException e) {
 	        serverController.logToConsole("Error sending income report to client: " + e.getMessage());
 		}
 	}
    
    /*
    * Handles the performence report request from a client.
    * This method processes the request, retrieves the necessary data from the database,
    * and sends back the income report to the client.
    *
    * @param message The message containing the request details, including date range.
    * @param client The connection to the client that sent the request.
    */
    private void handlePerformenceReport(Message<?> message, ConnectionToClient client) {
    	Object[] params = (Object[]) message.getType();
        LocalDate startDate = (LocalDate) params[0];
        LocalDate endDate = (LocalDate) params[1];
        String currentUser = (String)params[2];
        
		try {
			Map<String, Object>  reportData = DataBaseController.getInstance().generatePerformanceReport(startDate, endDate,currentUser);
	        Message<Map<String, Object>> response = new Message<>("PerformanceReportResponse", reportData);
	        System.out.println("Got to send to client the response");

	        System.out.println(reportData);
			client.sendToClient(response);

		} catch (IOException e) {
	        serverController.logToConsole("Error sending income report to client: " + e.getMessage());

		}
    }
    
    /*
     * Handles the order report request from a client.
     * This method processes the request, retrieves the necessary data from the database,
     * and sends back the income report to the client.
     *
     * @param message The message containing the request details, including date range.
     * @param client The connection to the client that sent the request.
     */
    private void handleOrderReport(Message<?> message, ConnectionToClient client) {
    	Object[] params = (Object[]) message.getType();
        LocalDate startDate = (LocalDate) params[0];
        LocalDate endDate = (LocalDate) params[1];
        String currentUser = (String)params[2];

        // Generate the report
		try {
			Map<String, Object>  reportData = DataBaseController.getInstance().generateOrdersReport(startDate, endDate,currentUser);
	        Message<Map<String, Object>> response = new Message<>("OrderReportResponse", reportData);
	        if(response!=null) {
	        	System.out.println("The order report is not null");
	        	
	        }
	        if(response.getType()!=null) {
	        	System.out.println("The order report data is not null");

	        }
			client.sendToClient(response);

		} catch (SQLException e) {
			  serverController.logToConsole("Error handling in SQL");
		}
		 catch (IOException e) {
	        serverController.logToConsole("Error sending income report to client: " + e.getMessage());

		}
    }
    
    /*
    * Handles the income report request from a client.
    * This method processes the request, retrieves the necessary data from the database,
    * and sends back the income report to the client.
    *
    * @param message The message containing the request details, including date range.
    * @param client The connection to the client that sent the request.
    */
    private void handleIncomeReport(Message<?> message, ConnectionToClient client) {
    	 Object[] params = (Object[]) message.getType();
         LocalDate startDate = (LocalDate) params[0];
         LocalDate endDate = (LocalDate) params[1];
         String currentUser = (String)params[2];

         // Generate the report
         Map<String, Object> reportData = DataBaseController.getInstance().generateIncomeReport(startDate, endDate,currentUser);
         Message<Map<String, Object>> response = new Message<>("IncomeReportResponse", reportData);
         try {
			client.sendToClient(response);
		} catch (IOException e) {
	        serverController.logToConsole("Error sending income report to client: " + e.getMessage());

		}
	}
    
    /**
     * Handles the request to update a menu item.
     * This method processes the update details, updates the menu item in the database,
     * and sends a confirmation or failure message back to the client.
     *
     * @param message The Message object containing the menu item update details
     * @param client The ConnectionToClient object representing the client connection
     */
    private void handleUpdateMenuItem(Message<?> message, ConnectionToClient client) {
        if (message.getType() instanceof Object[]) {
            Object[] updateData = (Object[]) message.getType();
            int itemId = (int) updateData[0];
            BigDecimal price = (BigDecimal) updateData[1];
            int quantity = (int) updateData[2];
            String restaurantId = (String) updateData[3];

            MenuItem updatedItem = new MenuItem();
            updatedItem.setItemId(itemId);
            updatedItem.setPrice(price);
            updatedItem.setQuantity(quantity);
            updatedItem.setRestaurantId(restaurantId);

            Message<String> response = DataBaseController.getInstance().updateMenuItem(updatedItem);
            try {
                client.sendToClient(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                client.sendToClient(new Message<>("UPDATE_FAILED", "Invalid update data"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}