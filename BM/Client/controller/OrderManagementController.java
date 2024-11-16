package controller;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.util.Duration;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import client.ClientController;
import entities.Message;
import entities.Order;

/**
 * Controller class for managing orders in the restaurant system.
 * This class handles the UI and logic for viewing and updating order statuses.
 */
public class OrderManagementController implements Initializable {
	
	/**
	 * The ListView that displays the orders for the restaurant.
	 */
	@FXML private ListView<String> ordersListView;

	/**
	 * Button to accept an order.
	 */
	@FXML private Button acceptOrderButton;

	/**
	 * Button to reject an order.
	 */
	@FXML private Button rejectOrderButton;

	/**
	 * Button to mark an order as ready.
	 */
	@FXML private Button readyOrderButton;

	/**
	 * Label to display the status of the selected order.
	 */
	@FXML private Label statusLabel;

	/**
	 * Label to display a timer for order preparation or delivery.
	 */
	@FXML private Label timerLabel;

	/**
	 * The client controller used to handle communication with the server.
	 */
	private ClientController clientController;

	/**
	 * The ID of the restaurant currently being managed.
	 */
	private String restaurantId;

	/**
	 * The observable list that holds the order strings for the ListView.
	 */
	private ObservableList<String> ordersList;

	/**
	 * A map to store orders by their ID for quick retrieval.
	 */
	private Map<Integer, Order> ordersMap;

	/**
	 * Utility class for handling communication between controllers.
	 */
	private ControllerCommunicationUtility communicationUtility;

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if unknown.
     * @param resources The resources used to localize the root object, or null if not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startOrderRefreshTimer();
        ordersList = FXCollections.observableArrayList();
        ordersMap = new HashMap<>();
        ordersListView.setItems(ordersList);
        ordersListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        acceptOrderButton.setOnAction(event -> handleAcceptOrder());
        rejectOrderButton.setOnAction(event -> handleRejectOrder());
        readyOrderButton.setOnAction(event -> handleReadyOrder());
        
        ordersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            boolean isOrderSelected = (newValue != null);
            acceptOrderButton.setDisable(!isOrderSelected);
            rejectOrderButton.setDisable(!isOrderSelected);
            readyOrderButton.setDisable(!isOrderSelected);
        });
        
        communicationUtility = ControllerCommunicationUtility.getInstance();
    }

    /**
     * Generates a random time range between 15 and 30 minutes.
     *
     * @return A random integer between 15 and 30.
     */
    public int randomRangeTime() {
        Random random = new Random();
        int number = random.nextInt((30 - 15) + 1) + 15;
        return number;
    }

    /**
     * Sets the client controller for this OrderManagementController.
     *
     * @param clientController The client controller to be used.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
    }

    /**
     * Sets the restaurant ID for this OrderManagementController.
     *
     * @param restaurantId The ID of the restaurant.
     */
    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
        if (this.clientController != null) {
            loadOrders();
        }
    }
	
    /**
     * Retrieves an Order object by its ID from the current list of orders.
     *
     * @param orderId The ID of the order to retrieve.
     * @return The Order object if found, null otherwise.
     */
    private Order getOrderById(int orderId) {
        for (String orderString : ordersList) {
            int currentOrderId = extractOrderIdFromString(orderString);
            if (currentOrderId == orderId) {
                return createOrderFromString(orderString);
            }
        }
        return null;
    }

    /**
     * Creates an Order object from its string representation.
     *
     * @param orderString The string representation of the order.
     * @return The created Order object.
     */
    private Order createOrderFromString(String orderString) {
        Order order = new Order();
        String[] lines = orderString.split("\n");
        for (String line : lines) {
            if (line.startsWith("Order ID:")) {
                order.setOrderId(Integer.parseInt(line.split(":")[1].trim()));
            } else if (line.startsWith("Customer ID:")) {
                order.setCustomerId(line.split(":")[1].trim());
            } else if (line.startsWith("Restaurant ID:")) {
                order.setRestaurantId(line.split(":")[1].trim());
            } else if (line.startsWith("Status:")) {
                order.setStatus(Order.OrderStatus.valueOf(line.split(":")[1].trim()));
            } else if (line.startsWith("Delivery Type:")) {
                order.setDeliveryType(Order.DeliveryType.valueOf(line.split(":")[1].trim()));
            }
        }
        return order;
    }
    
    /**
     * Loads orders for the current restaurant from the server.
     */
    private void loadOrders() {
        System.out.println("Loading orders for restaurant ID: " + this.restaurantId);
        Message<?> response = this.clientController.handleGetRestaurantOrders(this.restaurantId);
        System.out.println("Response received: " + response);
        if (response != null && response.getType() instanceof List) {
            @SuppressWarnings("unchecked")
            List<Order> orders = (List<Order>) response.getType();
            System.out.println("Number of orders received: " + orders.size());
            Platform.runLater(() -> {
                ordersList.clear();
                ordersMap.clear();
                for (Order order : orders) {
                    ordersList.add(order.toString());
                    ordersMap.put(order.getOrderId(), order);
                }
                ordersListView.setItems(ordersList);
                ordersListView.refresh();
            });
        } else {
            System.out.println("No orders found or invalid response");
        }
    }

    /**
     * Handles the action when the Confirm Order button is clicked.
     * Updates the status of the selected order to "CONFIRMED" and sends a notification to the customer.
     * Also schedules a status change to "PREPARING" after 10 seconds.
     */
    private void handleAcceptOrder() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder != null) {
            try {
                Message<?> response = clientController.handleUpdateOrderStatus(selectedOrder.getOrderId(), "CONFIRMED");
                if (response != null && response.getType() instanceof String && 
                    ((String) response.getType()).startsWith("Order status updated successfully")) {
                    selectedOrder.setStatus(Order.OrderStatus.CONFIRMED);
                    selectedOrder.setOrderTime(LocalDateTime.now());
                    updateOrderInList(selectedOrder);
                    
                    showAlert("Order confirmed", (String) response.getType());
                    sendMessageToClient(selectedOrder.getCustomerId(), "Your order #" + selectedOrder.getOrderId() + " has been accepted and is being prepared.");
                    
                    // Schedule the status change to PREPARING after 10 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(10000); // Wait for 10 seconds
                            Platform.runLater(() -> {
                                try {
                                    Message<?> prepareResponse = clientController.handleUpdateOrderStatus(selectedOrder.getOrderId(), "PREPARING");
                                    if (prepareResponse != null && prepareResponse.getType() instanceof String && 
                                        ((String) prepareResponse.getType()).startsWith("Order status updated successfully")) {
                                        selectedOrder.setStatus(Order.OrderStatus.PREPARING);
                                        updateOrderInList(selectedOrder);
                                        showAlert("Order status updated", "Order #" + selectedOrder.getOrderId() + " is now being prepared.");
                                    } else {
                                        showAlert("Error", "Failed to update order to PREPARING status");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showAlert("Error", "An error occurred while updating order to PREPARING status: " + e.getMessage());
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    communicationUtility.sendMessage(new Message<>("ORDER_ACCEPTED", selectedOrder.getOrderId()));
                    refreshOrdersAfterDelay();
                } else {
                    showAlert("Error", "Failed to confirm the order: " + response.getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while confirming the order: " + e.getMessage());
            }
        } else {
            showAlert("No Order Selected", "Please select an order to confirm.");
        }
    }
    
    /**
     * Refreshes the list of orders after a short delay of 2 seconds.
     * This is useful to ensure the UI reflects any updates after performing an action, 
     * such as accepting, rejecting, or marking an order as ready.
     */
    private void refreshOrdersAfterDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(2000); // 2-second delay
                Platform.runLater(this::loadOrders);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Handles the action when the Reject Order button is clicked.
     */
    private void handleRejectOrder() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder != null) {
            try {
                clientController.handleUpdateOrderStatus(selectedOrder.getOrderId(), "CANCELLED");
                selectedOrder.setStatus(Order.OrderStatus.CANCELLED);
                updateOrderInList(selectedOrder);
                
                showAlert("Order rejected", "The order has been rejected by the restaurant.");
                sendMessageToClient(selectedOrder.getCustomerId(), "Your order #" + selectedOrder.getOrderId() + " has been rejected by the restaurant.");
                
                communicationUtility.sendMessage(new Message<>("ORDER_REJECTED", selectedOrder.getOrderId()));
                loadOrders(); // Refresh the orders
            } catch (Exception e) {
                showAlert("Error", "An error occurred while rejecting the order: " + e.getMessage());
            }
        } else {
            showAlert("No Order Selected", "Please select an order to reject.");
        }
    }

    /**
     * Handles the action when the Ready Order button is clicked.
     */
    private void handleReadyOrder() {
        Order selectedOrder = getSelectedOrder();
        if (selectedOrder != null) {
            try {
                Message<?> response = clientController.handleUpdateOrderStatus(selectedOrder.getOrderId(), "READY");
                if (response != null && response.getType() instanceof String && 
                    ((String) response.getType()).startsWith("Order status updated successfully")) {
                    selectedOrder.setStatus(Order.OrderStatus.READY);
                    updateOrderInList(selectedOrder);
                    
                    showAlert("Order ready", "The order has been marked as ready.");
                    
                    // Schedule the status change to IN_DELIVERY after 10 seconds
                    new Thread(() -> {
                        try {
                            Thread.sleep(10000); // Wait for 10 seconds
                            Platform.runLater(() -> {
                                try {
                                    Message<?> deliveryResponse = clientController.handleUpdateOrderStatus(selectedOrder.getOrderId(), "IN_DELIVERY");
                                    if (deliveryResponse != null && deliveryResponse.getType() instanceof String && 
                                        ((String) deliveryResponse.getType()).startsWith("Order status updated successfully")) {
                                        selectedOrder.setStatus(Order.OrderStatus.IN_DELIVERY);
                                        updateOrderInList(selectedOrder);
                                        showAlert("Order status updated", "Order #" + selectedOrder.getOrderId() + " is now in delivery.");
                                        
                                        // Send message to client after status change
                                        sendMessageToClient(selectedOrder.getCustomerId(), "Your order #" + selectedOrder.getOrderId() + " is ready and out for delivery. Please confirm when you've received it.");
                                    } else {
                                        showAlert("Error", "Failed to update order to IN_DELIVERY status");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showAlert("Error", "An error occurred while updating order to IN_DELIVERY status: " + e.getMessage());
                                }
                            });
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }).start();

                    communicationUtility.sendMessage(new Message<>("ORDER_READY", selectedOrder.getOrderId()));
                    refreshOrdersAfterDelay();
                } else {
                    showAlert("Error", "Failed to mark the order as ready: " + response.getType());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Error", "An error occurred while marking the order as ready: " + e.getMessage());
            }
        } else {
            showAlert("No Order Selected", "Please select an order to mark as ready.");
        }
    }
    
    /**
     * Confirms the delivery of an order and updates its status to "DELIVERED".
     * This method also records the actual arrival time of the order.
     *
     * @param orderId The ID of the order to confirm delivery for.
     */
    public void confirmOrderDelivery(int orderId) {
        Order order = getOrderById(orderId);
        if (order != null && (order.getStatus() == Order.OrderStatus.IN_DELIVERY)) {
            try {
                Message<?> response = clientController.handleUpdateOrderStatus(orderId, "DELIVERED");
                if (response.getMessage().startsWith("Order status updated successfully")) {
                    order.setStatus(Order.OrderStatus.DELIVERED);
                    order.setActualArrivalTime(LocalDateTime.now()); // Stop the timer
                    updateOrderInList(order);
                    
                    showAlert("Order Delivered", response.getMessage());
                } else {
                    showAlert("Error", "Failed to mark the order as delivered: " + response.getMessage());
                }
            } catch (Exception e) {
                showAlert("Error", "An error occurred while marking the order as delivered: " + e.getMessage());
            }
        } else {
            showAlert("Invalid Status", "The order cannot be marked as delivered at this time.");
        }
    }
    
    /**
     * Starts a timer that automatically refreshes the list of orders every 30 seconds.
     * This ensures that the UI stays up-to-date with any changes in order status or new orders.
     */
    private void startOrderRefreshTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            loadOrders();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    /**
     * Gets the currently selected order from the list view.
     *
     * @return The selected Order object, or null if no order is selected.
     */
    private Order getSelectedOrder() {
        String selectedOrderString = ordersListView.getSelectionModel().getSelectedItem();
        if (selectedOrderString != null) {
            int orderId = extractOrderIdFromString(selectedOrderString);
            return ordersMap.get(orderId);
        }
        return null;
    }
    
    /**
     * Updates an order in the list view.
     *
     * @param order The Order object to update in the list.
     */
    private void updateOrderInList(Order order) {
        Platform.runLater(() -> {
            int index = -1;
            for (int i = 0; i < ordersList.size(); i++) {
                if (extractOrderIdFromString(ordersList.get(i)) == order.getOrderId()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                ordersList.set(index, order.toString());
                ordersMap.put(order.getOrderId(), order);
                ordersListView.refresh();
            }
        });
    }
    
    /**
     * Extracts the order ID from a string representation of an order.
     *
     * @param orderString The string representation of an order.
     * @return The extracted order ID.
     * @throws IllegalArgumentException if the order ID is not found in the string.
     */
    private int extractOrderIdFromString(String orderString) {
        String[] lines = orderString.split("\n");
        for (String line : lines) {
            if (line.startsWith("Order ID:")) {
                return Integer.parseInt(line.split(":")[1].trim());
            }
        }
        throw new IllegalArgumentException("Order ID not found in the order string");
    }
    
    /**
     * Shows an alert dialog with the given title and content.
     *
     * @param title The title of the alert dialog.
     * @param content The content message of the alert dialog.
     */
    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
    
    /**
     * Sends a message to a specific client (customer).
     *
     * @param customerId The ID of the customer to send the message to.
     * @param messageContent The content of the message to send.
     */
    public void sendMessageToClient(String customerId, String messageContent) {
        Message<String> message = new Message<>("CLIENT_MESSAGE", messageContent);
        communicationUtility.sendMessage(new Message<>(customerId, message));
    }
}