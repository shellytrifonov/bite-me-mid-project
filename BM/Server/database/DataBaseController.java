package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.sql.*;
import entities.Order;
import entities.OrderItem;
import entities.Restaurant;
import entities.MenuItem;
import entities.Message;

import entities.User;
import entities.User.UserRole;

/**
 * DatabaseController is responsible for handling database operations.
 * It uses the Singleton pattern to ensure only one instance exists.
 */
public class DataBaseController {

    /**
     * The single instance of DataBaseController (Singleton pattern).
     */
    private static DataBaseController instance;

    /**
     * The database connection object.
     */
    private Connection connection;
    
    /**
     * Private constructor to prevent instantiation.
     * Establishes a database connection using the JDBC utility class.
     */
    private DataBaseController() {
    	try {
    		this.connection = JDBC.getConnection();
    	} catch (SQLException e) {
    		e.printStackTrace();
    	}
    }
    
    /**
     * Returns the single instance of DatabaseController, creating it if necessary.
     *
     * @return The singleton instance of DatabaseController
     */
    public static DataBaseController getInstance() {
    	if (instance == null) {
    		instance = new DataBaseController();
    	}
    	return instance;
    }
    
    /**
     * Checks user login credentials against the database and updates connection status.
     *
     * @param username The username of the user trying to log in
     * @param password The password of the user trying to log in
     * @return A Message object containing either the logged-in User object (if successful)
     *         or an error message (if login fails)
     */
    public Message<User> checkUserLogin(String userId, String password) {
        String query = "SELECT * FROM Users WHERE userId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedPassword = rs.getString("password");
                    if (password.equals(storedPassword)) {
                        User user = new User(
                            rs.getString("userId"),
                            rs.getString("firstName"),
                            rs.getString("lastName"),
                            rs.getString("email"),
                            rs.getString("phoneNumber"),
                            rs.getString("password"),
                            UserRole.valueOf(rs.getString("role")),
                            rs.getBigDecimal("credit")
                        );
                        
                        boolean isAlreadyConnected = rs.getBoolean("connected");
                        if (isAlreadyConnected) {
                            return new Message<>("User is already logged in", null);
                        }
                        
                        if (updateUserConnectionStatus(userId, true)) {
                            return new Message<>("LOGIN_SUCCESS", user);
                        } else {
                            return new Message<>("LOGIN_SUCCESS, but failed to update connection status", user);
                        }
                    } else {
                        return new Message<>("Invalid password", null);
                    }
                } else {
                    return new Message<>("User not found", null);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Database error occurred: " + e.getMessage(), null);
        }
    }
    
    /**
     * Updates the user's connection status in the database.
     *
     * @param username The username of the user
     * @param isConnected The connection status to set (true for connected, false for disconnected)
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserConnectionStatus(String userId, boolean isConnected) {
        String query = "UPDATE Users SET connected = ? WHERE userId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBoolean(1, isConnected);
            pstmt.setString(2, userId);
            
            int rowAffected = pstmt.executeUpdate();
            return rowAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Logs out a user by setting their connection status to false.
     *
     * @param username The username of the user to log out
     * @return A Message indicating success or failure of the logout operation
     */
    public Message<Void> logoutUser(String userId) {
        if (updateUserConnectionStatus(userId, false)) {
            return new Message<>("User logged out successfully");
        } else {
            return new Message<>("Failed to log out user");
        }
    }
    
    /**
     * Handles a report management request.
     * @param reportType The type of report to be managed
     * @return A Message object containing the result of the report management operation
     */
    public Message<String> handleReportManagement(String reportType) {
        return new Message<>("Report management request received for: " + reportType);
    }
    
    /**
     * Handles the registration of a new customer.
     * @param newUser The User object containing the details of the new customer
     * @return A Message object containing the result of the registration operation
     */
    public Message<String> handleNewCustomerRegistration(User newUser) {
        String query = "INSERT INTO Users (userId, firstName, lastName, email, phoneNumber, password, role, creditCard, credit) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, newUser.getUserId());
            pstmt.setString(2, newUser.getFirstName());
            pstmt.setString(3, newUser.getLastName());
            pstmt.setString(4, newUser.getEmail());
            pstmt.setString(5, newUser.getPhoneNumber());
            pstmt.setString(6, newUser.getPassword());
            pstmt.setString(7, newUser.getRole().toString());
            pstmt.setString(8, newUser.getCreditCard());
            pstmt.setBigDecimal(9, newUser.getCredit());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return new Message<>("NEW_CUSTOMER_REGISTRATION_SUCCESS");
            } else {
                return new Message<>("NEW_CUSTOMER_REGISTRATION_FAILED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error registering new customer: " + e.getMessage());
        }
    }
    
    /**
     * Inserts a new order into the database.
     * 
     * @param newOrder The Order object containing the details of the new order.
     * @return A Message object containing the result of the operation.
     */
    public Message<String> newOrder(Order newOrder) {
        String query = "INSERT INTO Orders (customerId, restaurantId, totalPrice, status, deliveryType, isPayed, orderTime, requiredTime, actualArrivalTime, deliveryAddress, recipientName, recipientPhone, discountApplied, robot) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, newOrder.getCustomerId());
            pstmt.setString(2, newOrder.getRestaurantId());
            pstmt.setBigDecimal(3, newOrder.getTotalPrice());
            pstmt.setString(4, newOrder.getStatus().name());
            pstmt.setString(5, newOrder.getDeliveryType().name());
            pstmt.setBoolean(6, newOrder.isPayed());
            pstmt.setTimestamp(7, Timestamp.valueOf(newOrder.getOrderTime()));
            pstmt.setTimestamp(8, Timestamp.valueOf(newOrder.getRequiredTime()));
            pstmt.setTimestamp(9, newOrder.getActualArrivalTime() != null ? Timestamp.valueOf(newOrder.getActualArrivalTime()) : null);
            pstmt.setString(10, newOrder.getDeliveryAddress());
            pstmt.setString(11, newOrder.getRecipientName());
            pstmt.setString(12, newOrder.getRecipientPhone());
            pstmt.setBoolean(13, newOrder.isDiscountApplied());
            pstmt.setBoolean(14, newOrder.getRobot());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        newOrder.setOrderId(orderId);
                        return insertOrderItems(newOrder);
                    }
                }
                return new Message<>("New order registered successfully");
            } else {
                return new Message<>("Failed to register new order");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error registering new order: " + e.getMessage());
        }
    }
    
    /**
     * Inserts order items associated with an order into the database.
     * 
     * @param order The Order object containing the items to be inserted.
     * @return A Message object containing the result of the operation.
     */
    private Message<String> insertOrderItems(Order order) {
        String query = "INSERT INTO OrderItems (orderId, itemId, quantity, specialInstructions) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            for (OrderItem item : order.getOrderItems()) {
                pstmt.setInt(1, order.getOrderId());
                pstmt.setInt(2, item.getItemId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setString(4, item.getSpecialInstructions());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            return new Message<>("New order registered successfully with items");
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error inserting order items: " + e.getMessage());
        }
    }
    
    /**
     * Loads all restaurants from the database.
     * 
     * @return A Message object containing a list of Restaurant objects if successful, or an error message if not.
     */
    public Message<List<Restaurant>> loadRestaurants() {
        List<Restaurant> restaurants = new ArrayList<>();
        String query = "SELECT r.restaurantId, r.name, r.location, r.branch, u.phoneNumber " +
                       "FROM Restaurants r " +
                       "JOIN Users u ON r.restaurantId = u.userId";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Restaurant restaurant = new Restaurant(
                    rs.getString("restaurantId"),
                    null,
                    rs.getString("name"),
                    rs.getString("phoneNumber"),
                    rs.getString("location"),
                    Restaurant.branch.valueOf(rs.getString("branch"))
                );
                restaurants.add(restaurant);
            }
            
            System.out.println("Total restaurants loaded: " + restaurants.size());
            return new Message<>("Restaurants loaded successfully", restaurants);
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error loading restaurants: " + e.getMessage(), null);
        }
    }
    
    /**
     * Loads menu items for a specific restaurant from the database.
     * 
     * @param restaurant The Restaurant object for which to load menu items.
     * @return A Message object containing a list of MenuItem objects if successful, or an error message if not.
     */
    public Message<List<MenuItem>> loadItems(Restaurant restaurant) {
        List<MenuItem> menuItems = new ArrayList<>();
        String query = "SELECT * FROM MenuItems WHERE restaurantId = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, restaurant.getRestaurantId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MenuItem item = new MenuItem(
                        rs.getInt("itemId"),
                        rs.getString("restaurantId"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getBoolean("isInStock"),
                        MenuItem.ItemType.valueOf(rs.getString("type").toUpperCase()),
                        rs.getInt("quantity")
                    );
                    menuItems.add(item);
                }
            }
            
            return new Message<>("Menu items loaded successfully", menuItems);
        } catch (SQLException e) {
            System.out.println("DataBaseController: Error loading menu items: " + e.getMessage());
            e.printStackTrace();
            return new Message<>("Error loading menu items: " + e.getMessage(), null);
        } catch (Exception e) {
            System.out.println("DataBaseController: Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return new Message<>("Unexpected error: " + e.getMessage(), null);
        }
    }

    /**
     * Retrieves a list of orders for the specified customer from the database.
     * 
     * @param customerId the ID of the customer whose orders are to be retrieved.
     * @return a `Message` object containing a list of `Order` objects and a status message.
     */
    public Message<List<Order>> getCustomerOrders(String customerId) {
        List<Order> orders = new ArrayList<>();
        String orderQuery = "SELECT * FROM Orders WHERE customerId = ?";
        String itemQuery = "SELECT * FROM OrderItems WHERE orderId = ?";
        
        try (PreparedStatement orderStmt = connection.prepareStatement(orderQuery);
             PreparedStatement itemStmt = connection.prepareStatement(itemQuery)) {
            
            orderStmt.setString(1, customerId);
            try (ResultSet orderRs = orderStmt.executeQuery()) {
                while (orderRs.next()) {
                    Order order = new Order();
                    order.setOrderId(orderRs.getInt("orderId"));
                    order.setCustomerId(orderRs.getString("customerId"));
                    order.setRestaurantId(orderRs.getString("restaurantId"));
                    order.setTotalPrice(orderRs.getBigDecimal("totalPrice"));
                    order.setStatus(Order.OrderStatus.valueOf(orderRs.getString("status")));
                    order.setDeliveryType(Order.DeliveryType.valueOf(orderRs.getString("deliveryType")));
                    order.setPayed(orderRs.getBoolean("isPayed"));
                    order.setOrderTime(orderRs.getTimestamp("orderTime").toLocalDateTime());
                    order.setRequiredTime(orderRs.getTimestamp("requiredTime").toLocalDateTime());
                    order.setDeliveryAddress(orderRs.getString("deliveryAddress"));
                    order.setRecipientName(orderRs.getString("recipientName"));
                    order.setRecipientPhone(orderRs.getString("recipientPhone"));
                    order.setDiscountApplied(orderRs.getBoolean("discountApplied"));
                    order.setRobot(orderRs.getBoolean("robot"));

                    // Load order items
                    List<OrderItem> orderItems = new ArrayList<>();
                    itemStmt.setInt(1, order.getOrderId());
                    try (ResultSet itemRs = itemStmt.executeQuery()) {
                        while (itemRs.next()) {
                            OrderItem item = new OrderItem();
                            item.setOrderItemId(itemRs.getInt("orderItemId"));
                            item.setOrderId(itemRs.getInt("orderId"));
                            item.setItemId(itemRs.getInt("itemId"));
                            item.setQuantity(itemRs.getInt("quantity"));
                            item.setSpecialInstructions(itemRs.getString("specialInstructions"));
                            orderItems.add(item);
                        }
                    }
                    order.setOrderItems(orderItems);
                    orders.add(order);
                }
            }
            
            if (orders.isEmpty()) {
                return new Message<>("No orders found for this customer", orders);
            } else {
                return new Message<>("Customer orders loaded successfully", orders);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error loading customer orders: " + e.getMessage(), null);
        }
    }
    
    /**
     * Retrieves orders for a specific restaurant that are in PENDING status.
     *
     * @param restaurant The Restaurant object for which to retrieve orders.
     * @return A Message object containing a list of Order objects if successful, or an error message if not.
     */
    public Message<List<Order>> handleGetRestaurantOrders(Restaurant restaurant) {
        List<Order> orderList = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE restaurantId = ? AND status IN (?, ?)";
        try {
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setString(1, restaurant.getRestaurantId());
            pstmt.setString(2, "PENDING");
            pstmt.setString(3, "PREPARING");
            try {
                ResultSet rs = pstmt.executeQuery();
                System.out.println(rs.toString());
                while (rs.next()) {
                    Order order = new Order(
                        rs.getInt("orderId"),
                        rs.getString("customerId"),
                        rs.getString("restaurantId"),
                        rs.getBigDecimal("totalPrice"),
                        Order.OrderStatus.valueOf(rs.getString("status").toUpperCase()),
                        Order.DeliveryType.valueOf(rs.getString("deliveryType").toUpperCase()),
                        rs.getBoolean("isPayed"),
                        rs.getTimestamp("orderTime").toLocalDateTime(),
                        rs.getTimestamp("requiredTime").toLocalDateTime(),
                        rs.getTimestamp("actualArrivalTime") != null ? rs.getTimestamp("actualArrivalTime").toLocalDateTime() : null,
                        rs.getString("deliveryAddress"),
                        rs.getString("recipientName"),
                        rs.getString("recipientPhone"),
                        rs.getBoolean("discountApplied")
                    );
                    order.setRobot(rs.getBoolean("robot"));
                    orderList.add(order);
                    System.out.println(order.toString());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return new Message<>("Error loading orders: " + e.getMessage(), null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("Error loading orders: " + e.getMessage(), null);
        }
        if (orderList.isEmpty()) {
            return new Message<>("No orders found for this restaurant", null);
        } else {
            return new Message<>("Orders loaded successfully", orderList);
        }
    }
	
	/**
	 * Updates the status of an order in the database.
	 *
	 * @param orderId The ID of the order to update.
	 * @param status The new status to set for the order.
	 * @return A Message object containing the result of the update operation.
	 */
    /**
     * Updates the status of an order in the database.
     *
     * @param orderId The ID of the order to update.
     * @param status The new status to set for the order.
     * @return A Message object containing the result of the update operation.
     */
    public Message<String> handleUpdateOrderStatus(int orderId, String status) {
        String query;
        LocalDateTime now = LocalDateTime.now();

        if ("DELIVERED".equals(status)) {
            query = "UPDATE orders SET status = ?, actualArrivalTime = ? WHERE orderId = ?";
        } else {
            query = "UPDATE orders SET status = ? WHERE orderId = ?";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, status);
            if ("DELIVERED".equals(status)) {
                pstmt.setTimestamp(2, Timestamp.valueOf(now));
                pstmt.setInt(3, orderId);
            } else {
                pstmt.setInt(2, orderId);
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Order ID " + orderId + " updated to status: " + status);
                return new Message<>("Order status updated successfully");
            } else {
                System.out.println("Failed to update order ID " + orderId + ". No rows affected.");
                return new Message<>("Failed to update order status: No rows affected");
            }
        } catch (SQLException e) {
            System.out.println("SQL Error updating order status for order ID " + orderId + ": " + e.getMessage());
            return new Message<>("Error updating order status: " + e.getMessage());
        }
    }

	
    /**
     * Generates an income report for the specified date range.
     * This method queries the database to retrieve income data and compile it into a report.
     *
     * @param startDate The start date of the report period.
     * @param endDate The end date of the report period.
     * @return A Map containing the income report data, including total income,
     *         income by restaurant, and daily income breakdown.
     * @throws SQLException If there's an error executing the database queries.
     */
    public Map<String, Object> generateIncomeReport(LocalDate startDate, LocalDate endDate,String managerId) {
        Map<String, Object> reportData = new HashMap<>();
     
        String query = "SELECT " +
        	    "r.name AS restaurant_name, " +
        	    "DATE(o.orderTime) AS order_date, " +
        	    "SUM(o.totalPrice) AS daily_income " +
        	"FROM Orders o " +
        	"JOIN Restaurants r ON o.restaurantId = r.restaurantId " +
        	"JOIN Managers m ON r.restaurantId = m.restaurantId " +
        	"WHERE m.managerId = ? " +
        	    "AND o.orderTime BETWEEN ? AND ? " +
        	    "AND o.status = 'DELIVERED' " +
        	    "AND o.isPayed = 1 " +
        	"GROUP BY r.restaurantId,r.name, DATE(o.orderTime) " +
        	"ORDER BY r.name, order_date";
        
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        	pstmt.setString(1, managerId);
        	pstmt.setTimestamp(2, Timestamp.valueOf(startDate.atStartOfDay()));
        	pstmt.setTimestamp(3, Timestamp.valueOf(endDate.atTime(LocalTime.MAX)));
            System.out.println("Executing query with parameters: " +
                    "startDate=" + startDate + ", endDate=" + endDate + ", managerId=" + managerId);

            
            double totalIncome = 0;
            Map<String, Double> incomeByRestaurant = new HashMap<>();
            Map<LocalDate, Double> dailyIncome = new TreeMap<>();

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String restaurantName = rs.getString("restaurant_name");
                    LocalDate orderDate = rs.getDate("order_date").toLocalDate();
                    double income = rs.getDouble("daily_income");

                    totalIncome += income;

                    incomeByRestaurant.merge(restaurantName, income, Double::sum);
                    dailyIncome.merge(orderDate, income, Double::sum);
                }
            }

            reportData.put("totalIncome", totalIncome);
            reportData.put("incomeByRestaurant", incomeByRestaurant);
            reportData.put("dailyIncome", dailyIncome);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reportData;
    }
    
    /**
     * Generates a detailed orders report for a specific manager within a given date range.
     * This report provides insights into order patterns, popular items, and revenue distribution.
     *
     * @param startDate The start date of the report period (inclusive).
     * @param endDate The end date of the report period (inclusive).
     * @param managerId The ID of the manager for whom the report is being generated.
     * @return A Map containing the following report data:
     *         - "totalOrders": (Integer) Total number of orders in the period.
     *         - "ordersByType": (Map<String, Integer>) Number of orders for each item type.
     *         - "topItems": (List<Map<String, Object>>) Top 5 most ordered items, each containing:
     *           - "itemName": (String) Name of the item.
     *           - "orderCount": (Integer) Number of times the item was ordered.
     * @throws SQLException if there's an error executing the database query.
     */
    public Map<String, Object> generateOrdersReport(LocalDate startDate, LocalDate endDate, String managerId) throws SQLException {
        Map<String, Object> reportData = new HashMap<>();

        String query = "SELECT " +
                "    o.orderId, " +
                "    i.type, " +
                "    i.name, " +
                "    oi.quantity, " +
                "    COUNT(oi.orderItemId) AS itemCount " +
                "FROM " +
                "    biteme.Orders o " +
                "JOIN " +
                "    biteme.Restaurants r ON o.restaurantId = r.restaurantId " +
                "JOIN " +
                "    biteme.Managers m ON r.restaurantId = m.restaurantId " +
                "JOIN " +
                "    biteme.OrderItems oi ON o.orderId = oi.orderId " +
                "JOIN " +
                "    biteme.MenuItems i ON oi.itemId = i.itemId " +
                "WHERE " +
                "    m.managerId = ? " +
                "    AND o.orderTime BETWEEN ? AND ? " +
                "    AND o.status = 'DELIVERED' AND o.isPayed = 1 " +
                "GROUP BY " +
                "    o.orderId, i.type, i.name, oi.quantity " +
                "ORDER BY " +
                "    o.orderId, i.type, itemCount DESC";


        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, managerId);
            pstmt.setObject(2, startDate.atStartOfDay());
            pstmt.setObject(3, endDate.atTime(LocalTime.MAX));

            try (ResultSet rs = pstmt.executeQuery()) {
            	 Map<String, Integer> ordersByType = new HashMap<>();
                 Map<String, Integer> itemTotalCounts = new HashMap<>();
                 List<Map<String, Object>> allItems = new ArrayList<>();
                 Set<Integer> uniqueOrderIds = new HashSet<>();
                 int totalOrders = 0;

                 while (rs.next()) {
                     int orderId = rs.getInt("orderId");
                     String type = rs.getString("type");
                     String name = rs.getString("name");
                     int quantity = rs.getInt("quantity");
                     int itemCount = rs.getInt("itemCount");

                     uniqueOrderIds.add(orderId);
                     ordersByType.merge(type, quantity, Integer::sum);
                     itemTotalCounts.merge(name, quantity, Integer::sum);
                     totalOrders += quantity;

                     Map<String, Object> itemData = new HashMap<>();
                     itemData.put("orderId", orderId);
                     itemData.put("itemName", name);
                     itemData.put("quantity", quantity);
                     itemData.put("itemCount", itemCount);
                     allItems.add(itemData);
                 }

                 reportData.put("totalOrders", totalOrders);
                 reportData.put("uniqueOrders", uniqueOrderIds.size());
                 reportData.put("ordersByType", ordersByType);
                 reportData.put("allItems", allItems);

                 List<Map.Entry<String, Integer>> topItems = itemTotalCounts.entrySet().stream()
                     .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                     .limit(5)
                     .collect(Collectors.toList());

                 List<Map<String, Object>> formattedTopItems = topItems.stream()
                     .map(entry -> {
                         Map<String, Object> item = new HashMap<>();
                         item.put("itemName", entry.getKey());
                         item.put("orderCount", entry.getValue());
                         return item;
                     })
                     .collect(Collectors.toList());

                 reportData.put("topItems", formattedTopItems);
             }
        } catch (SQLException e) {
            System.err.println("Error generating orders report: " + e.getMessage());
            throw e;
        }
        
        return reportData;
    }
  
    /*
     * Generates a performance report for deliveries within a specified date range for a specific manager.
     *
     * @param startDate The start date of the report period (inclusive).
     * @param endDate The end date of the report period (inclusive).
     * @param managerId The ID of the manager for whom the report is being generated.
     * @return A Map containing the performance report data.
     * @throws SQLException if there's an error executing the database query.
     */
    public Map<String, Object> generatePerformanceReport(LocalDate startDate, LocalDate endDate, String managerId) {
        Map<String, Object> reportData = new HashMap<>();
        System.out.println("Got to generatePerformanceReport");

        String query = "SELECT " +
        	    "    DATE(o.orderTime) AS deliveryDate, " +
        	    "    DAYNAME(o.orderTime) AS dayOfWeek, " +
        	    "    COUNT(*) AS totalDeliveries, " +
        	    "    SUM(CASE WHEN o.actualArrivalTime <= o.requiredTime THEN 1 ELSE 0 END) AS onTimeDeliveries, " +
        	    "    AVG(TIMESTAMPDIFF(MINUTE, o.orderTime, o.actualArrivalTime)) AS avgDeliveryTime, " +
        	    "    SUM(o.totalPrice) AS totalRevenue, " +
        	    "    (SUM(CASE WHEN o.actualArrivalTime <= o.requiredTime THEN 1 ELSE 0 END) / COUNT(*) * 100) AS onTimePercentage " +
        	    "FROM " +
        	    "    biteme.Orders o " +
        	    "JOIN " +
        	    "    biteme.Restaurants r ON o.restaurantId = r.restaurantId " +
        	    "JOIN " +
        	    "    biteme.Managers m ON r.restaurantId = m.restaurantId " +
        	    "WHERE " +
        	    "    o.status = 'DELIVERED' " +
        	    "    AND o.deliveryType IN ('DELIVERY', 'EARLY_DELIVERY', 'ROBOT', 'SHARED_DELIVERY') " +
        	    "    AND o.orderTime BETWEEN ? AND ? " +
        	    "    AND m.managerId = ? " +
        	    "    AND o.isPayed = 1 " +
        	    "GROUP BY " +
        	    "    DATE(o.orderTime), DAYNAME(o.orderTime) " +
        	    "ORDER BY " +
        	    "    deliveryDate;";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        	pstmt.setObject(1, startDate.atStartOfDay());
            pstmt.setObject(2, endDate.atTime(LocalTime.MAX));
            pstmt.setString(3, managerId);
            System.out.println("Got to resultset");

            try (ResultSet rs = pstmt.executeQuery()) {
            	long totalDeliveries = 0;
            	boolean isEmpty=true;
                BigDecimal totalOnTimeDeliveries = BigDecimal.ZERO;
                BigDecimal totalDeliveryTime = BigDecimal.ZERO;
                BigDecimal totalRevenue = BigDecimal.ZERO;
                Map<String, BigDecimal> performanceByDay = new HashMap<>();
                List<Map<String, Object>> dailyData = new ArrayList<>();
                while (rs.next()) {
                	isEmpty=false;
                    long dailyDeliveries = rs.getLong("totalDeliveries");
                    totalDeliveries += dailyDeliveries;
                    System.out.println("Pass field 1");

                    BigDecimal dailyOnTimeDeliveries = rs.getBigDecimal("onTimeDeliveries");
                    totalOnTimeDeliveries = totalOnTimeDeliveries.add(dailyOnTimeDeliveries);                    
                    System.out.println("Pass field 2");

                    BigDecimal avgDeliveryTime = rs.getBigDecimal("avgDeliveryTime");
                    totalDeliveryTime = totalDeliveryTime.add(avgDeliveryTime.multiply(BigDecimal.valueOf(dailyDeliveries)));
                    System.out.println("Pass field 3");

                    BigDecimal dailyRevenue = rs.getBigDecimal("totalRevenue");
                    totalRevenue = totalRevenue.add(dailyRevenue);
                    System.out.println("Pass field 4");

                    String dayOfWeek = rs.getString("dayOfWeek");

                    BigDecimal onTimePercentage = rs.getBigDecimal("onTimePercentage");
                    performanceByDay.put(dayOfWeek, onTimePercentage);
                    System.out.println("Pass field 5");

                    Map<String, Object> dayData = new HashMap<>(); 
                    dayData.put("date", rs.getDate("deliveryDate").toLocalDate()); 
                    dayData.put("dayOfWeek", dayOfWeek);
                    dayData.put("totalDeliveries", dailyDeliveries);
                    dayData.put("onTimeDeliveries", dailyOnTimeDeliveries);
                    dayData.put("avgDeliveryTime", rs.getDouble("avgDeliveryTime"));
                    dayData.put("totalRevenue", dailyRevenue);
                    dayData.put("onTimePercentage", onTimePercentage);
                    dailyData.add(dayData);
                }
                if(!isEmpty) {
                long tempTotalDeliveryTime =totalDeliveryTime.longValue();
                reportData.put("totalDeliveries", totalDeliveries);
                reportData.put("totalOnTimeDeliveries", totalOnTimeDeliveries);
                reportData.put("avgDeliveryTime", tempTotalDeliveryTime/totalDeliveries);
                reportData.put("totalRevenue", totalRevenue);
                reportData.put("performanceByDay", performanceByDay);
                reportData.put("dailyData", dailyData);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //pie chart
        @SuppressWarnings("unchecked")
		Map<String, Double>  s=(Map<String, Double>) reportData.get("onTimeDeliveries");
        System.out.println(reportData.get("totalOnTimeDeliveries"));

        
        
        System.out.println(reportData.get("performanceByDay"));

        System.out.println(reportData.get("dailyData"));
        System.out.println(reportData.get("totalRevenue"));
        return reportData;
    }
    
    /**
     * Retrieves quarterly report data for a specific branch, quarter, and year.
     * 
     * This method queries the database to fetch order and revenue statistics for restaurants
     * in a given branch during a specific quarter and year. It categorizes the data based on
     * order count ranges and aggregates revenue information.
     *
     * @param quarter The quarter for which to retrieve data (1-4)
     * @param year The year for which to retrieve data
     * @param branch The branch name for which to retrieve data
     * @return A Map containing the following keys:
     *         - "branchData": Map<String, Map<String, Integer>> where the outer key is the restaurant name,
     *                         the inner key is the order range, and the value is the number of days in that range
     *         - "revenueData": Map<String, Map<String, Double>> where the outer key is the restaurant name,
     *                          the inner key is the order range, and the value is the total revenue for that range
     *         - "branch": String representing the branch name
     *         - "quarter": Integer representing the quarter
     *         - "year": Integer representing the year
     * @throws SQLException if a database access error occurs or this method is called on a closed connection
     */
    public Map<String, Object> getQuarterlyReportData(int quarter, int year, String branch) {
        Map<String, Object> reportData = new HashMap<>();
        
        String query = "SELECT restaurant_name, order_range, " +
                "COUNT(DISTINCT order_date) AS days_count, " +
                "SUM(daily_revenue) AS total_revenue " +
                "FROM (" +
                "    SELECT r.name AS restaurant_name, " +
                "           DATE(o.orderTime) AS order_date, " +
                "           SUM(o.totalPrice) AS daily_revenue, " +
                "           CASE " +
                "               WHEN COUNT(o.orderId) BETWEEN 0 AND 20 THEN '0-20' " +
                "               WHEN COUNT(o.orderId) BETWEEN 21 AND 40 THEN '21-40' " +
                "               WHEN COUNT(o.orderId) BETWEEN 41 AND 60 THEN '41-60' " +
                "               WHEN COUNT(o.orderId) BETWEEN 61 AND 80 THEN '61-80' " +
                "               ELSE '81+' " +
                "           END AS order_range " +
                "    FROM restaurants r " +
                "    LEFT JOIN orders o ON r.restaurantId = o.restaurantId " +
                "    WHERE YEAR(o.orderTime) = ? AND QUARTER(o.orderTime) = ? AND r.branch = ? AND o.status = 'DELIVERED' AND  o.isPayed=1" +
                "    GROUP BY r.restaurantId, r.name, DATE(o.orderTime)" +
                ") AS daily_orders " +
                "GROUP BY restaurant_name, order_range " +
                "ORDER BY restaurant_name, order_range";
        try (PreparedStatement pstmt = connection.prepareStatement(query)){
            
            pstmt.setInt(1, year);
            pstmt.setInt(2, quarter);
            pstmt.setString(3, branch);
            
            ResultSet rs = pstmt.executeQuery();
            
            Map<String, Map<String, Integer>> branchData = new HashMap<>();
            Map<String, Map<String, Double>> revenueData = new HashMap<>();
            
            while (rs.next()) {
                String restaurantName = rs.getString("restaurant_name");
                String orderRange = rs.getString("order_range");
                int daysCount = rs.getInt("days_count");
                double revenue = rs.getDouble("total_revenue");
                
                branchData.computeIfAbsent(restaurantName, k -> new HashMap<>()).put(orderRange, daysCount);
                revenueData.computeIfAbsent(restaurantName, k -> new HashMap<>()).put(orderRange, revenue);
            }
            
            reportData.put("branchData", branchData);
            reportData.put("revenueData", revenueData);
            reportData.put("branch", branch);
            reportData.put("quarter", quarter);
            reportData.put("year", year);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return reportData;
    }
    
    /**
     * Updates a menu item in the database.
     *
     * @param item The MenuItem object containing the updated information.
     * @return A Message object containing the result of the update operation.
     */
    public Message<String> updateMenuItem(MenuItem item) {
        String query = "UPDATE MenuItems SET price = ?, quantity = ? WHERE itemId = ? AND restaurantId = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setBigDecimal(1, item.getPrice());
            pstmt.setInt(2, item.getQuantity());
            pstmt.setInt(3, item.getItemId());
            pstmt.setString(4, item.getRestaurantId());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                return new Message<>("ITEM_UPDATED", "Menu item updated successfully");
            } else {
                return new Message<>("ITEM_NOT_FOUND", "No matching item found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message<>("UPDATE_FAILED", "Error updating menu item: " + e.getMessage());
        }
    }
}