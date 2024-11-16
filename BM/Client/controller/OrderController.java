package controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.application.Platform;
import javafx.fxml.Initializable;

import client.ChatClient;
import client.ClientController;
import entities.Message;
import entities.Order;
import entities.Order.DeliveryType;
import entities.Order.OrderStatus;
import entities.OrderItem;
import entities.Restaurant;
import entities.MenuItem;
import entities.User;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller class for the order screen of the Bite Me application.
 * Present the restaurants and menu items for the customer.
 * @author Shelly Trifonov
 */
public class OrderController implements Initializable {
	
	/**
	 * ComboBox for selecting a restaurant.
	 */
	@FXML private ComboBox<Restaurant> restaurantComboBox;

	/**
	 * ComboBox for selecting the main dish.
	 */
	@FXML private ComboBox<MenuItem> mainComboBox;

	/**
	 * ComboBox for selecting the grill level of the main dish.
	 */
	@FXML private ComboBox<String> mainGrillLevelComboBox;

	/**
	 * ComboBox for selecting an appetizer.
	 */
	@FXML private ComboBox<MenuItem> appetizerComboBox;

	/**
	 * ComboBox for selecting a dessert.
	 */
	@FXML private ComboBox<MenuItem> dessertComboBox;

	/**
	 * ComboBox for selecting a salad.
	 */
	@FXML private ComboBox<MenuItem> saladComboBox;

	/**
	 * ComboBox for selecting the size of the salad.
	 */
	@FXML private ComboBox<String> saladSizeComboBox;

	/**
	 * ComboBox for selecting a drink.
	 */
	@FXML private ComboBox<MenuItem> drinkComboBox;

	/**
	 * ComboBox for selecting the size of the drink.
	 */
	@FXML private ComboBox<String> drinkSizeComboBox;

	/**
	 * ComboBox for selecting the quantity of items (deprecated, use specific quantity ComboBoxes).
	 */
	@FXML private ComboBox<Integer> quantityComboBox;

	/**
	 * ComboBox for selecting the quantity of the main dish.
	 */
	@FXML private ComboBox<Integer> mainQuantityComboBox;

	/**
	 * ComboBox for selecting the quantity of the appetizer.
	 */
	@FXML private ComboBox<Integer> appetizerQuantityComboBox;

	/**
	 * ComboBox for selecting the quantity of the dessert.
	 */
	@FXML private ComboBox<Integer> dessertQuantityComboBox;

	/**
	 * ComboBox for selecting the quantity of the salad.
	 */
	@FXML private ComboBox<Integer> saladQuantityComboBox;

	/**
	 * ComboBox for selecting the quantity of the drink.
	 */
	@FXML private ComboBox<Integer> drinkQuantityComboBox;

	/**
	 * ComboBox for selecting the delivery type.
	 */
	@FXML private ComboBox<DeliveryType> deliveryTypeComboBox;

	/**
	 * ComboBox for selecting the order time.
	 */
	@FXML private ComboBox<LocalTime> orderTimePicker;

	/**
	 * CheckBox for selecting robot delivery option.
	 */
	@FXML private CheckBox robotDeliveryCheckBox;

	/**
	 * TextArea for entering special instructions for the order.
	 */
	@FXML private TextArea specialInstructionsArea;

	/**
	 * Button to place the order.
	 */
	@FXML private Button placeOrderButton;

	/**
	 * TextField for entering the delivery address.
	 */
	@FXML private TextField addressField;

	/**
	 * TextField for entering the recipient's name.
	 */
	@FXML private TextField nameField;

	/**
	 * TextField for entering the recipient's phone number.
	 */
	@FXML private TextField phoneField;

	/**
	 * TextField for entering the number of orderers for shared delivery.
	 */
	@FXML private TextField numberOfOrderersField;

	/**
	 * Label for displaying the total price of the order.
	 */
	@FXML private Label totalPriceLabel;

	/**
	 * DatePicker for selecting the order date.
	 */
	@FXML private DatePicker orderDatePicker;

	/**
	 * Button to go back to the previous screen.
	 */
	@FXML private Button backButton;

	/**
	 * The ClientController instance used for communication with the server.
	 */
	private ClientController clientController;

	/**
	 * The Stage in which the customer's UI is displayed.
	 */
	private Stage customerStage;
    
	/**
	 * Initializes the controller class. This method is automatically called after the FXML file has been loaded.
	 * Sets up the UI components, their event handlers, and initializes the necessary data.
	 *
	 * @param location The location used to resolve relative paths for the root object, or null if unknown.
	 * @param resources The resources used to localize the root object, or null if not localized.
	 */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeComboBoxes();
        setupRestaurantComboBox();
        // Set initial prompt texts
        mainComboBox.setPromptText("Choose main");
        appetizerComboBox.setPromptText("Choose appetizer");
        dessertComboBox.setPromptText("Choose dessert");
        saladComboBox.setPromptText("Choose salad");
        drinkComboBox.setPromptText("Choose drink");
        initializeTimePicker();
        numberOfOrderersField.setVisible(false);
        
        restaurantComboBox.setOnAction(event -> loadMenuItems());
        placeOrderButton.setOnAction(event -> handlePlaceOrder());
        backButton.setOnAction(event -> handleBackButton());
        
        mainComboBox.setOnAction(e -> updateTotalPriceDisplay());
        appetizerComboBox.setOnAction(e -> updateTotalPriceDisplay());
        dessertComboBox.setOnAction(e -> updateTotalPriceDisplay());
        saladComboBox.setOnAction(e -> updateTotalPriceDisplay());
        drinkComboBox.setOnAction(e -> updateTotalPriceDisplay());

        mainQuantityComboBox.setOnAction(e -> updateTotalPriceDisplay());
        appetizerQuantityComboBox.setOnAction(e -> updateTotalPriceDisplay());
        dessertQuantityComboBox.setOnAction(e -> updateTotalPriceDisplay());
        saladQuantityComboBox.setOnAction(e -> updateTotalPriceDisplay());
        drinkQuantityComboBox.setOnAction(e -> updateTotalPriceDisplay());
        robotDeliveryCheckBox.setOnAction(e -> updateTotalPriceDisplay());
        deliveryTypeComboBox.setOnAction(e -> {
            handleDeliveryTypeChange();
            updateTotalPriceDisplay();
        });
        deliveryTypeComboBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == DeliveryType.SHARED_DELIVERY) {
                numberOfOrderersField.setVisible(true);
            } else {
                numberOfOrderersField.setVisible(false);
                numberOfOrderersField.clear();
            }
        });
    }
    
    /**
     * Sets the ClientController for this OrderController and initializes necessary data.
     *
     * @param clientController The ClientController to be used for server communication.
     */
    public void setClientController(ClientController clientController) {
        this.clientController = clientController;
        initializeAfterClientControllerSet();
    }
    
    /**
     * Initializes the controller after the ClientController has been set.
     * This method loads the list of restaurants available to the user.
     */
    public void initializeAfterClientControllerSet() {
        loadRestaurants();
    }

    /**
     * Sets the Stage for this OrderController.
     *
     * @param stage The Stage in which the customer's UI is displayed.
     */
    public void setCustomerStage(Stage stage) {
        this.customerStage = stage;
    }
	
    /**
     * Initializes the ComboBoxes with default values.
     * Populates the ComboBoxes with appropriate options for grill levels, sizes, quantities, and delivery types.
     */
    private void initializeComboBoxes() {
        mainGrillLevelComboBox.getItems().addAll("Rare", "Medium", "Well Done");
        saladSizeComboBox.getItems().addAll("Small", "Large");
        drinkSizeComboBox.getItems().addAll("Small", "Medium", "Large");
        for (int i = 1; i <= 10; i++) {
            mainQuantityComboBox.getItems().add(i);
            appetizerQuantityComboBox.getItems().add(i);
            dessertQuantityComboBox.getItems().add(i);
            saladQuantityComboBox.getItems().add(i);
            drinkQuantityComboBox.getItems().add(i);
        }
        mainQuantityComboBox.setValue(1);
        appetizerQuantityComboBox.setValue(1);
        dessertQuantityComboBox.setValue(1);
        saladQuantityComboBox.setValue(1);
        drinkQuantityComboBox.setValue(1);
        
        for (DeliveryType type : DeliveryType.values()) {
            deliveryTypeComboBox.getItems().add(type);
        }
    }
    
    /**
     * Initializes the time picker with available time slots.
     * Populates the orderTimePicker ComboBox with time slots from 8:00 AM to 10:00 PM in 30-minute increments.
     */
    private void initializeTimePicker() {
        LocalTime start = LocalTime.of(8, 0); // Start at 8:00 AM
        LocalTime end = LocalTime.of(22, 0);  // End at 10:00 PM
        while (start.isBefore(end.plusSeconds(1))) {
            orderTimePicker.getItems().add(start);
            start = start.plusMinutes(30); // Add times in 30-minute increments
        }
    }
    
    /**
     * Handles changes in the delivery type selection.
     * If SHARED_DELIVERY is selected, checks if the user is a business customer and shows a dialog to input the number of orderers.
     */
    private void handleDeliveryTypeChange() {
        if (deliveryTypeComboBox.getValue() == DeliveryType.SHARED_DELIVERY) {
            if (ChatClient.currentUser.getRole() == User.UserRole.CUSTOMER_BUSINESS) {
                showSharedDeliveryDialog();
            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Delivery Type", "Shared delivery is only available for business customers.");
                deliveryTypeComboBox.setValue(null);
            }
        }
    }

    /**
     * Shows a dialog for inputting the number of orderers for shared delivery.
     * Validates the input and updates the numberOfOrderersField.
     */
    private void showSharedDeliveryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Shared Delivery");
        dialog.setHeaderText("Enter the number of orderers");
        dialog.setContentText("Number of orderers:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(number -> {
            try {
                int numberOfOrderers = Integer.parseInt(number);
                if (numberOfOrderers < 1) {
                    showAlert(Alert.AlertType.WARNING, "Invalid Input", "Number of orderers must be at least 1.");
                    deliveryTypeComboBox.setValue(null);
                } else {
                    numberOfOrderersField.setText(number);
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input", "Please enter a valid number.");
                deliveryTypeComboBox.setValue(null);
            }
        });

        if (!result.isPresent()) {
            deliveryTypeComboBox.setValue(null);
        }
    }
    
    /**
     * Sets up the restaurant combo box with a custom cell factory.
     * Configures the restaurantComboBox to display restaurant names instead of their toString() representation.
     */
    private void setupRestaurantComboBox() {
        restaurantComboBox.setCellFactory(new Callback<ListView<Restaurant>, ListCell<Restaurant>>() {
            public ListCell<Restaurant> call(ListView<Restaurant> param) {
                return new ListCell<Restaurant>() {
                    @Override
                    protected void updateItem(Restaurant item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        restaurantComboBox.setButtonCell(new ListCell<Restaurant>() {
            @Override
            protected void updateItem(Restaurant item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    /**
     * Loads the list of restaurants from the server.
     * Sends a request to the server and populates the restaurantComboBox with the received data.
     */
    private void loadRestaurants() {
        if (clientController == null) {
            System.out.println("Error: ClientController is not set");
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Unable to load restaurants. Please try again later."));
            return;
        }
        
        Message<Void> getMessage = new Message<>("GET_RESTAURANTS", null);
        clientController.accept(getMessage);
        Message<?> response = clientController.getResponse("GET_RESTAURANTS");
        System.out.println("Received response: " + response);
        if (response != null) {
            if (response.getType() instanceof List) {
                @SuppressWarnings("unchecked")
				List<Restaurant> restaurants = (List<Restaurant>) response.getType();
                System.out.println("Received " + restaurants.size() + " restaurants");
                if (restaurants.isEmpty()) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "No Restaurants", "There are currently no restaurants available."));
                } else {
                    Platform.runLater(() -> {
                        restaurantComboBox.getItems().clear();
                        restaurantComboBox.getItems().addAll(restaurants);
                    });
                }
            } else {
                System.out.println("Error: Response type is not a List. Actual type: " + response.getType().getClass().getName());
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Unexpected response from server. Please try again later."));
            }
        } else {
            System.out.println("Error: Received null response from server");
            Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "No response from server. Please check your connection and try again."));
        }
    }

    /**
     * Loads menu items for the selected restaurant.
     * Sends a request to the server to get menu items for the selected restaurant and populates the respective ComboBoxes.
     */
    @SuppressWarnings("unchecked")
	private void loadMenuItems() {
        Restaurant selectedRestaurant = restaurantComboBox.getValue();
        if (selectedRestaurant != null) {
            System.out.println("Selected Restaurant: " + selectedRestaurant.getName() + " ID: " + selectedRestaurant.getRestaurantId());
            Message<String> getMessage = new Message<>("GET_MENU_ITEMS", selectedRestaurant.getRestaurantId());
            clientController.accept(getMessage);

            try {
                Message<?> response = clientController.getResponse("GET_MENU_ITEMS");
                System.out.println("Received response for menu items: " + response);

                if (response != null && response.getType() instanceof List) {
                    List<?> menuItems = (List<?>) response.getType();
                    System.out.println("Number of menu items received: " + menuItems.size());
                    for (Object item : menuItems) {
                        System.out.println("Item: " + item);
                    }
                    Platform.runLater(() -> {
                        populateMenuComboBoxes((List<MenuItem>) menuItems);
                        if (menuItems.isEmpty()) {
                            showAlert(Alert.AlertType.INFORMATION, "No Menu Items", "This restaurant has no menu items yet.");
                        }
                    });
                } else {
                    System.out.println("Response is not a List or is null");
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Error", "Failed to load menu items."));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Exception", "An error occurred: " + e.getMessage()));
            }
        } else {
            Platform.runLater(() -> showAlert(Alert.AlertType.WARNING, "Warning", "Please select a restaurant."));
        }
    }

    /**
     * Populates menu item combo boxes based on their types.
     * Clears existing items and adds new items to the appropriate ComboBoxes based on their menu item type.
     *
     * @param menuItems The list of menu items to populate the combo boxes with.
     */
    private void populateMenuComboBoxes(List<MenuItem> menuItems) {
        mainComboBox.getItems().clear();
        appetizerComboBox.getItems().clear();
        dessertComboBox.getItems().clear();
        saladComboBox.getItems().clear();
        drinkComboBox.getItems().clear();

        for (MenuItem item : menuItems) {
            if (item.isInStock()) {  // Only add the item if it's in stock
                switch (item.getType()) {
                    case MAIN:
                        mainComboBox.getItems().add(item);
                        break;
                    case APPETIZER:
                        appetizerComboBox.getItems().add(item);
                        break;
                    case DESSERT:
                        dessertComboBox.getItems().add(item);
                        break;
                    case SALAD:
                        saladComboBox.getItems().add(item);
                        break;
                    case DRINK:
                        drinkComboBox.getItems().add(item);
                        break;
                }
            }
        }

        // Set default values or prompts for empty combo boxes
        setPromptTextIfEmpty(mainComboBox, "No main dishes available");
        setPromptTextIfEmpty(appetizerComboBox, "No appetizers available");
        setPromptTextIfEmpty(dessertComboBox, "No desserts available");
        setPromptTextIfEmpty(saladComboBox, "No salads available");
        setPromptTextIfEmpty(drinkComboBox, "No drinks available");
    }

    /**
     * Sets the prompt text for a ComboBox if it has no items.
     *
     * @param comboBox The ComboBox to set the prompt text for.
     * @param promptText The prompt text to set if the ComboBox is empty.
     */
    private void setPromptTextIfEmpty(ComboBox<MenuItem> comboBox, String promptText) {
        if (comboBox.getItems().isEmpty()) {
            comboBox.setPromptText(promptText);
        } else {
            comboBox.setPromptText(null);  // Clear the prompt text if there are items
        }
    }
    
    /**
     * Handles the place order action.
     * Validates the order, creates it, sends it to the server, and processes the response.
     */
    @FXML
    private void handlePlaceOrder() {
        if (!validateOrder()) {
            return;
        }

        Order order = createOrder();
        if (showOrderConfirmation(order)) {
            order.setPayed(true); // Set isPayed to true after confirmation
            Message<?> response = clientController.placeOrder(order);

            if (response != null && response.getMessage().equals("ORDER_PLACED_SUCCESSFULLY")) {
                showAlert(Alert.AlertType.INFORMATION, "Order Placed", "Your order has been placed successfully!");
                
                // Close the current order stage
                Stage currentStage = (Stage) placeOrderButton.getScene().getWindow();
                currentStage.close();
                
                // Show the customer stage
                if (customerStage != null) {
                    Platform.runLater(() -> {
                        customerStage.show();
                    });
                } else {
                    showAlert(Alert.AlertType.ERROR, "Navigation Error", "Unable to return to the customer page.");
                }
            } else {
                showAlert(Alert.AlertType.ERROR, "Order Failed", "Failed to place order. Please try again.");
            }
        }
    }

    /**
     * Creates an Order object from the current selections in the UI.
     *
     * @return A new Order object with the details from the UI, or null if the order is invalid.
     */
    private Order createOrder() {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime requiredTime = LocalDateTime.of(orderDatePicker.getValue(), orderTimePicker.getValue());

        order.setRequiredTime(requiredTime);

        if (deliveryTypeComboBox.getValue() == DeliveryType.EARLY_DELIVERY) {
            // Check if it's at least 2 hours in advance
            if (requiredTime.isAfter(now.plusHours(2))) {
                order.setDeliveryType(DeliveryType.EARLY_DELIVERY);
                order.setDiscountApplied(true);
            } else {
                showAlert(Alert.AlertType.WARNING, "Invalid Early Delivery", "Early delivery must be scheduled at least 2 hours in advance.");
                return null;
            }
        } else {
            order.setDeliveryType(deliveryTypeComboBox.getValue());
            order.setDiscountApplied(false);
        }
        
        // Add order items
        if (mainComboBox.getValue() != null) {
            OrderItem mainItem = new OrderItem();
            mainItem.setItemId(mainComboBox.getValue().getItemId());
            mainItem.setQuantity(mainQuantityComboBox.getValue());
            orderItems.add(mainItem);
        }

        if (appetizerComboBox.getValue() != null) {
            OrderItem appetizerItem = new OrderItem();
            appetizerItem.setItemId(appetizerComboBox.getValue().getItemId());
            appetizerItem.setQuantity(appetizerQuantityComboBox.getValue());
            orderItems.add(appetizerItem);
        }

        if (dessertComboBox.getValue() != null) {
            OrderItem dessertItem = new OrderItem();
            dessertItem.setItemId(dessertComboBox.getValue().getItemId());
            dessertItem.setQuantity(dessertQuantityComboBox.getValue());
            orderItems.add(dessertItem);
        }

        if (saladComboBox.getValue() != null) {
            OrderItem saladItem = new OrderItem();
            saladItem.setItemId(saladComboBox.getValue().getItemId());
            saladItem.setQuantity(saladQuantityComboBox.getValue());
            orderItems.add(saladItem);
        }

        if (drinkComboBox.getValue() != null) {
            OrderItem drinkItem = new OrderItem();
            drinkItem.setItemId(drinkComboBox.getValue().getItemId());
            drinkItem.setQuantity(drinkQuantityComboBox.getValue());
            orderItems.add(drinkItem);
        }
        
        String specialInstructions = specialInstructionsArea.getText();
        for (OrderItem item : orderItems) {
            item.setSpecialInstructions(specialInstructions);
        }

        order.setCustomerId(ChatClient.currentUser.getUserId());
        order.setRestaurantId(restaurantComboBox.getValue().getRestaurantId());
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryType(deliveryTypeComboBox.getValue());
        if (deliveryTypeComboBox.getValue() == DeliveryType.EARLY_DELIVERY) {
            order.setDiscountApplied(true);
        } else {
            order.setDiscountApplied(false);
        }
        order.setPayed(false);
        order.setOrderTime(LocalDateTime.now());
        order.setDeliveryAddress(addressField.getText());
        order.setRecipientName(nameField.getText());
        order.setRecipientPhone(phoneField.getText());
        order.setOrderItems(orderItems);
        order.setTotalPrice(calculateTotalPrice());
        order.setRobot(robotDeliveryCheckBox.isSelected());
        
        return order;
    }
    
    /**
     * Calculates the total price of the order based on selected items, quantities, and delivery type.
     *
     * @return The total price as a BigDecimal.
     */
    private BigDecimal calculateTotalPrice() {
        BigDecimal total = BigDecimal.ZERO;
        
        if (mainComboBox.getValue() != null) {
            total = total.add(mainComboBox.getValue().getPrice().multiply(BigDecimal.valueOf(mainQuantityComboBox.getValue())));
        }
        if (appetizerComboBox.getValue() != null) {
            total = total.add(appetizerComboBox.getValue().getPrice().multiply(BigDecimal.valueOf(appetizerQuantityComboBox.getValue())));
        }
        if (dessertComboBox.getValue() != null) {
            total = total.add(dessertComboBox.getValue().getPrice().multiply(BigDecimal.valueOf(dessertQuantityComboBox.getValue())));
        }
        if (saladComboBox.getValue() != null) {
            total = total.add(saladComboBox.getValue().getPrice().multiply(BigDecimal.valueOf(saladQuantityComboBox.getValue())));
        }
        if (drinkComboBox.getValue() != null) {
            total = total.add(drinkComboBox.getValue().getPrice().multiply(BigDecimal.valueOf(drinkQuantityComboBox.getValue())));
        }
        if (deliveryTypeComboBox.getValue() == DeliveryType.DELIVERY) {
            total = total.add(new BigDecimal("25"));
        }
        if (deliveryTypeComboBox.getValue() == DeliveryType.EARLY_DELIVERY) {
            total = total.multiply(new BigDecimal("0.9")); // 10% discount
        }
        if (deliveryTypeComboBox.getValue() == DeliveryType.SHARED_DELIVERY && ChatClient.currentUser.getRole() == User.UserRole.CUSTOMER_BUSINESS & !numberOfOrderersField.getText().isEmpty()) {
                int numberOfOrderers = Integer.parseInt(numberOfOrderersField.getText());
                if (numberOfOrderers >= 2) {
                    total = total.add(new BigDecimal("15"));
                } else if (numberOfOrderers == 1) {
                    total = total.add(new BigDecimal("20"));
                } else {
                    total = total.add(new BigDecimal("25"));
                }
        }
        return total;
    }
    
    /**
     * Updates the display of the total price, including any applicable
     * discounts or additional costs based on the delivery type.
     */
    private void updateTotalPriceDisplay() {
        BigDecimal total = calculateTotalPrice();
        String priceText = "Total Price: $" + total.toString();
        if (deliveryTypeComboBox.getValue() == DeliveryType.EARLY_DELIVERY) {
            priceText += " (10% Early Delivery Discount Applied)";
        } else if (deliveryTypeComboBox.getValue() == DeliveryType.DELIVERY) {
            priceText += " (Delivery cost: $25.0)";
        } else if (deliveryTypeComboBox.getValue() == DeliveryType.SHARED_DELIVERY && 
                   !numberOfOrderersField.getText().isEmpty()) {
            int numberOfOrderers = Integer.parseInt(numberOfOrderersField.getText());
            BigDecimal shippingCost = numberOfOrderers >= 2 ? new BigDecimal("15") :
                                      numberOfOrderers == 1 ? new BigDecimal("20") :
                                      new BigDecimal("25");
            priceText += " (Shared Delivery cost: $" + shippingCost + ")";
        }
        totalPriceLabel.setText(priceText);
    }
    
    /**
     * Validates the current order to ensure all necessary fields are filled.
     *
     * @return true if the order is valid, false otherwise.
     */
    private boolean validateOrder() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate selectedDate = orderDatePicker.getValue();
        LocalTime selectedTime = orderTimePicker.getValue();

        if (selectedDate == null || selectedTime == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select both date and time for the order.");
            return false;
        }

        LocalDateTime selectedDateTime = LocalDateTime.of(selectedDate, selectedTime);

        if (selectedDateTime.isBefore(now)) {
            showAlert(Alert.AlertType.WARNING, "Invalid Time", "Selected time is in the past.");
            return false;
        }
        if (deliveryTypeComboBox.getValue() == DeliveryType.EARLY_DELIVERY) {
            if (!selectedDateTime.isAfter(now.plusHours(2))) {
                showAlert(Alert.AlertType.WARNING, "Invalid Early Delivery", "Early delivery must be scheduled at least 2 hours in advance.");
                return false;
            }
        }
        if (restaurantComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a restaurant.");
            return false;
        }
        if (mainComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a main dish.");
            return false;
        }
        if (deliveryTypeComboBox.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a delivery type.");
            return false;
        }
        if (deliveryTypeComboBox.getValue() != DeliveryType.SELF_PICKUP && 
            (addressField.getText().isEmpty() || nameField.getText().isEmpty() || phoneField.getText().isEmpty())) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please fill in all delivery details.");
            return false;
        }
        if (orderDatePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a date for the order.");
            return false;
        }
        if (orderTimePicker.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a time for the order.");
            return false;
        }
        if (mainComboBox.getValue() == null && appetizerComboBox.getValue() == null &&
        	    dessertComboBox.getValue() == null && saladComboBox.getValue() == null &&
        	    drinkComboBox.getValue() == null) {
        	    showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select at least one item.");
        	    return false;
        }
        if (mainGrillLevelComboBox.getValue() == null && mainComboBox.getValue() != null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a grill level for the main dish.");
    	    return false;
        }
        if (drinkSizeComboBox.getValue() == null && drinkComboBox.getValue() != null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a size for the drink.");
    	    return false;
        }
        if (saladSizeComboBox.getValue() == null && saladComboBox.getValue() != null) {
            showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please select a size for the salad.");
    	    return false;
        }
        if (deliveryTypeComboBox.getValue() == DeliveryType.SHARED_DELIVERY) {
            if (ChatClient.currentUser.getRole() != User.UserRole.CUSTOMER_BUSINESS) {
                showAlert(Alert.AlertType.WARNING, "Invalid Delivery Type", "Shared delivery is only available for business customers.");
                return false;
            }
            if (numberOfOrderersField.getText().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Incomplete Order", "Please enter the number of orderers for shared delivery.");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Shows an alert dialog with the given type, title, and content.
     *
     * @param alertType The type of the alert (e.g., WARNING, ERROR, INFORMATION).
     * @param title The title of the alert dialog.
     * @param content The content message of the alert dialog.
     */
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    
    /**
     * Displays a confirmation dialog with order details and waits for user confirmation.
     *
     * @param order The Order object containing all order details.
     * @return true if the user confirms the order, false otherwise.
     */
    private boolean showOrderConfirmation(Order order) {
        StringBuilder details = new StringBuilder();
        details.append("Order Details:\n\n");
        details.append("Restaurant: ").append(restaurantComboBox.getValue().getName()).append("\n");
        details.append("Delivery Type: ").append(order.getDeliveryType()).append("\n");
        details.append("Required Time: ").append(order.getRequiredTime()).append("\n\n");
        details.append("Items:\n");
        if (order.getRobot()) {
            details.append("Robot Delivery: Yes \n");
        }

        for (OrderItem item : order.getOrderItems()) {
            MenuItem menuItem = getMenuItemById(item.getItemId());
            details.append("- ").append(menuItem.getName())
                   .append(" (Quantity: ").append(item.getQuantity()).append(")")
                   .append(" $").append(menuItem.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                   .append("\n");
        }

        details.append("\nTotal Price: $").append(this.calculateTotalPrice());
        if (order.isDiscountApplied()) {
            details.append(" (10% Early Delivery Discount Applied)");
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Order Confirmation");
        alert.setHeaderText("Please confirm your order");
        alert.setContentText(details.toString());

        ButtonType payButton = new ButtonType("Pay");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(payButton, cancelButton);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == payButton;
    }
    
    /**
     * Retrieves a MenuItem object by its ID from the available menu items.
     *
     * @param itemId The ID of the menu item to retrieve.
     * @return The MenuItem object with the specified ID, or null if not found.
     */
    private MenuItem getMenuItemById(int itemId) {
        List<ComboBox<MenuItem>> comboBoxes = Arrays.asList(mainComboBox, appetizerComboBox, dessertComboBox, saladComboBox, drinkComboBox);
        for (ComboBox<MenuItem> comboBox : comboBoxes) {
            for (MenuItem item : comboBox.getItems()) {
                if (item.getItemId() == itemId) {
                    return item;
                }
            }
        }
        return null;
    }
    
    /**
     * Handles the back button action.
     * Closes the current window and shows the customer screen.
     */
    @FXML
    private void handleBackButton() {
        Stage currentStage = (Stage) placeOrderButton.getScene().getWindow();
        currentStage.close();
        if (customerStage != null) {
            customerStage.show();
        }
    }
}