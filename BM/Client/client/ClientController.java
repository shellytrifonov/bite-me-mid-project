package client;

import java.io.IOException;
import java.time.LocalDate;

import entities.Message;
import entities.Order;
import entities.User;

/**
 * The ClientController class manages the connection between the UI and the ChatClient.
 * It implements the ChatIF interface to handle communication between the client and server.
 */
public class ClientController implements ChatIF {

    /**
     * The instance of the ChatClient that this controller manages.
     */
    private ChatClient client;

    /**
     * Constructs an instance of the ClientController.
     *
     * @param host The host to connect to.
     * @param port The port to connect on.
     * @throws IOException If the connection fails.
     */
    public ClientController(String host, int port) throws IOException {
        try {
            client = new ChatClient(host, port, this);
            client.join();
        } catch (IOException e) {
            throw new IOException("Connection Failed: " + e.getMessage());
        }
    }

    /**
     * This method sends a message to the server via the ChatClient.
     * 
     * @param msg The message to be sent to the server.
     */
    public void accept(Object msg) {
        if (msg instanceof Message<?>) {
            client.sendToServer((Message<?>) msg);
        } else {
            System.out.println("Invalid message type. Must be an instance of Message<?>");
        }
    }

    /**
     * Retrieves the response from the server for a given message type.
     * 
     * @param messageType The type of message to retrieve the response for.
     * @return The Message object containing the server's response, or null if no response is found.
     */
    public Message<?> getResponse(String messageType) {
        int retries = 10;
        while (retries > 0) {
            Message<?> response = client.getResponse(messageType);
            if (response != null) {
                return response;
            }
            retries--;
            try {
                Thread.sleep(100); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return null; 
    }


    /**
     * Displays a message. This method is required by the ChatIF interface.
     * 
     * @param message The message to be displayed.
     */
	@Override
	public void display(String message) {
	}
	
	/**
	 * Sends a login request to the server and retrieves the response.
	 * @param username The username of the user trying to log in.
	 * @param password The password of the user trying to log in.
	 * @return A Message object containing the server's response to the login request.
	 */
	public Message<?> handleLogin(String username, String password) {
	    User user = new User(username, password);
	    Message<User> loginMessage = new Message<>("LOGIN", user);
	    accept(loginMessage);
	    Message<?> response = getResponse("LOGIN");
	    if (response.getMessage().equals("LOGIN_SUCCESS")) {
	        if (ChatClient.currentUser == null) {
	            System.out.println("Warning: currentUser is null after successful login");
	        }
	    }
	    return response;
	}
	
    /**
     * Sends a report management request to the server and retrieves the response.
     * @param reportType is the type of report to be managed.
     * @return A Message object containing the server's response to the report management request.
     */
    public Message<?> handleReportManagement(String reportType) {
        Message<String> reportManagementMsg = new Message<>("MANAGE_REPORT", reportType);
        accept(reportManagementMsg);
        return getResponse("REPORT_MANAGEMENT");
    }
    
    /**
     * Sends an income report request to the server and retrieves the response.
     * @param startDate The start date for the report period.
     * @param endDate The end date for the report period.
     * @param managerId The ID of the manager requesting the report.
     * @return A Message object containing the server's response to the income report request.
     */
    public Message<?> handleIncomeReport(LocalDate startDate, LocalDate endDate, String managerId) {
        Object[] params = new Object[]{startDate, endDate, managerId};
        Message<Object[]> incomeReportMsg = new Message<>("INCOME_REPORT", params);
        accept(incomeReportMsg);
        return getResponse("INCOME_REPORT_RESPONSE");
    }

    /**
     * Sends an orders report request to the server and retrieves the response.
     * @param startDate The start date for the report period.
     * @param endDate The end date for the report period.
     * @param managerId The ID of the manager requesting the report.
     * @return A Message object containing the server's response to the orders report request.
     */
    public Message<?> handleOrdersReport(LocalDate startDate, LocalDate endDate, String managerId) {
        Object[] params = new Object[]{startDate, endDate, managerId};
        Message<Object[]> ordersReportMsg = new Message<>("ORDERS_REPORT", params);
        accept(ordersReportMsg);
        return getResponse("ORDER_REPORT_RESPONSE");
    }

    /**
     * Sends a performance report request to the server and retrieves the response.
     * @param startDate The start date for the report period.
     * @param endDate The end date for the report period.
     * @param managerId The ID of the manager requesting the report.
     * @return A Message object containing the server's response to the performance report request.
     */
    public Message<?> handlePerformanceReport(LocalDate startDate, LocalDate endDate, String managerId) {
        Object[] params = new Object[]{startDate, endDate, managerId};
        Message<Object[]> performanceReportMsg = new Message<>("PERFORMANCE_REPORT", params);
        accept(performanceReportMsg);
        return getResponse("PERFORMANCE_REPORT_RESPONSE");
    }

    /**
     * Sends a new customer registration request to the server and retrieves the response.
     * This method is used to register a new customer in the system.
     * @param newUser A User object containing all the necessary information for the new customer.
     * @return A Message object containing the server's response to the registration request.
     */
    public Message<?> handleNewCustomerRegistration(User newUser) {
        Message<User> newCustomerMsg = new Message<>("NEW_CUSTOMER_REGISTRATION", newUser);
        accept(newCustomerMsg);
        return getResponse("NEW_CUSTOMER_REGISTRATION");
    }
    
    /**
     * Sends a request to get all restaurants from the server and retrieves the response.
     * @return A Message object containing the server's response with the list of restaurants.
     */
    public Message<?> getRestaurants() {
        Message<Void> getRestaurantsMsg = new Message<>("GET_RESTAURANTS", null);
        accept(getRestaurantsMsg);
        return getResponse("GET_RESTAURANTS");
    }

    /**
     * Sends a request to get menu items for a specific restaurant from the server and retrieves the response.
     * @param restaurantId The ID of the restaurant to get menu items for.
     * @return A Message object containing the server's response with the list of menu items.
     */
    public Message<?> getMenuItems(String restaurantId) {
        System.out.println("Requesting menu items for restaurant ID: " + restaurantId);
        Message<String> getMenuItemsMsg = new Message<>("GET_MENU_ITEMS", restaurantId);
        accept(getMenuItemsMsg);
        Message<?> response = getResponse("GET_MENU_ITEMS");
        System.out.println("Received response for menu items: " + response);
        return response;
    }

    /**
     * Sends a request to place a new order to the server and retrieves the response.
     * @param order The Order object containing the details of the new order.
     * @return A Message object containing the server's response to the order placement.
     */
    public Message<?> placeOrder(Order order) {
        Message<Order> placeOrderMsg = new Message<>("PLACE_ORDER", order);
        accept(placeOrderMsg);
        return getResponse("PLACE_ORDER");
    }
    
    /**
     * Sends a request to get orders for a specific customer from the server and retrieves the response.
     * @param customerId The ID of the customer to get orders for.
     * @return A Message object containing the server's response with the list of orders.
     */
    public Message<?> getCustomerOrders(String customerId) {
        Message<String> getCustomerOrdersMsg = new Message<>("GET_CUSTOMER_ORDERS", customerId);
        accept(getCustomerOrdersMsg);
        return getResponse("GET_CUSTOMER_ORDERS");
    }
    
	 /**
	 * Sends a request to retrieve the menu items for a specified restaurant.
	 * This method creates a message containing the restaurant ID and sends it to the server.
	 * It then waits for a response with the menu items and returns it.
	 * @param restaurantid The ID of the restaurant to fetch menu items for.
	 * @return A message containing the restaurant's menu items or an empty message if no menu items were found.
	 */
	public Message<?> handleGetRestaurantOrders(String restaurantid) {
	    Message<String> getRestaurantOrdersMsg = new Message<>("RESTAURANT_ORDERS",(String)restaurantid);
	    accept(getRestaurantOrdersMsg);
	    return getResponse("RESTAURANT_ORDERS");
	}
	
    /**
     * Sends a request to update the status of an order.
     * 
     * @param orderId The ID of the order to be updated.
     * @param status The new status to be set for the order.
     * @return A Message object containing the server's response to the update request.
     */
	public Message<?> handleUpdateOrderStatus(int orderId, String status) {
	    Object[] params = new Object[]{orderId, status};
	    Message<Object> updateOrderStatusMsg = new Message<>("UPDATE_ORDER_STATUS", params);
	    accept(updateOrderStatusMsg);
	    return getResponse("UPDATE_ORDER_STATUS_RESPONSE");
	}
    
    /**
     * Sends a request to update a menu item.
     * 
     * @param updateMessage A Message object containing the details of the menu item to be updated.
     * @return A Message object containing the server's response to the update request.
     */
    public Message<?> updateMenuItem(Message<Object[]> updateMessage) {
        accept(updateMessage);
        return getResponse("UPDATE_MENU_ITEM_RESPONSE");
    }
}