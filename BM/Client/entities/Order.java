package entities;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * The Order class represents an order in the Bite Me system.
 * It stores details about the order including customer, restaurant, price, status, delivery information, and more.
 */
@SuppressWarnings("serial")
public class Order implements Serializable {

    /**
     * The unique identifier of the order.
     */
    private int orderId;

    /**
     * The ID of the customer who placed the order.
     */
    private String customerId;
    
    /**
     * List of the items in the order.
     */
    private List<OrderItem> orderItems;
    
    /**
     * The ID of the restaurant fulfilling the order.
     */
    private String restaurantId;

    /**
     * The total price of the order.
     */
    private BigDecimal totalPrice;

    /**
     * The current status of the order.
     */
    private OrderStatus status;

    /**
     * The type of delivery for this order.
     */
    private DeliveryType deliveryType;

    /**
     * Indicates whether the order has been paid.
     */
    private boolean isPayed;

    /**
     * The time when the order was placed.
     */
    private LocalDateTime orderTime;

    /**
     * The time when the order is required to be delivered or picked up.
     */
    private LocalDateTime requiredTime;
    
    /**
     * The time when the order is arrived to the customer.
     */
    private LocalDateTime actualArrivalTime;

    /**
     * The delivery address for the order.
     */
    private String deliveryAddress;

    /**
     * The name of the recipient for the order.
     */
    private String recipientName;

    /**
     * The phone number of the recipient.
     */
    private String recipientPhone;

    /**
     * Indicates whether a discount has been applied to this order.
     */
    private boolean discountApplied;

    /**
     * Enum representing possible statuses of an order.
     */
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY, IN_DELIVERY, DELIVERED, CANCELLED
    }

    /**
     * Enum representing possible types of delivery.
     */
    public enum DeliveryType {
        SELF_PICKUP, DELIVERY, EARLY_DELIVERY, SHARED_DELIVERY
    }
    
    /**
     * Indicates whether the client chose robot delivery.
     */
    private boolean robot;

    /**
     * Creates an empty Order object (no-argument constructor).
     */
    public Order() {}

    /**
     * Creates an Order object with the specified details.
     *
     * @param orderId The unique identifier of the order.
     * @param customerId The ID of the customer who placed the order.
     * @param restaurantId The ID of the restaurant fulfilling the order.
     * @param totalPrice The total price of the order.
     * @param status The current status of the order.
     * @param deliveryType The type of delivery for this order.
     * @param isPayed Whether the order has been paid.
     * @param orderTime The time when the order was placed.
     * @param requiredTime The time when the order is required.
     * @param actualArrivalTime The time when the order actually arrived.
     * @param deliveryAddress The delivery address for the order.
     * @param recipientName The name of the recipient for the order.
     * @param recipientPhone The phone number of the recipient.
     * @param discountApplied Whether a discount has been applied to this order.
     */
    public Order(int orderId, String customerId, String restaurantId, BigDecimal totalPrice,
                 OrderStatus status, DeliveryType deliveryType, boolean isPayed,
                 LocalDateTime orderTime, LocalDateTime requiredTime, LocalDateTime actualArrivalTime, String deliveryAddress,
                 String recipientName, String recipientPhone, boolean discountApplied) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.totalPrice = totalPrice;
        this.status = status;
        this.deliveryType = deliveryType;
        this.isPayed = isPayed;
        this.orderTime = orderTime;
        this.requiredTime = requiredTime;
        this.deliveryAddress = deliveryAddress;
        this.recipientName = recipientName;
        this.recipientPhone = recipientPhone;
        this.discountApplied = discountApplied;
        this.actualArrivalTime = actualArrivalTime;
    }

    // Getters and setters for all fields

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }
    
    public String getDeliveryAddress() {
    	return deliveryAddress;
    }
    
    public void setDeliveryAddress(String address) {
    	this.deliveryAddress = address;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public DeliveryType getDeliveryType() {
        return deliveryType;
    }

    public void setDeliveryType(DeliveryType deliveryType) {
        this.deliveryType = deliveryType;
    }

    public boolean isPayed() {
        return isPayed;
    }

    public void setPayed(boolean payed) {
        isPayed = payed;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public LocalDateTime getRequiredTime() {
        return requiredTime;
    }

    public void setRequiredTime(LocalDateTime requiredTime) {
        this.requiredTime = requiredTime;
    }
    
    public LocalDateTime getActualArrivalTime() {
    	return actualArrivalTime;
    }
    
    public void setActualArrivalTime(LocalDateTime actualArrivalTime) {
        this.actualArrivalTime = actualArrivalTime;
    }
    
    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    public boolean isDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(boolean discountApplied) {
        this.discountApplied = discountApplied;
    }
    
    public boolean getRobot() {
    	return robot;
    }
    
    public void setRobot(boolean robot) {
    	this.robot = robot;
    }
    
    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    /**
     * Returns a string representation of the Order.
     *
     * @return A string containing the order's details.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Order Details:\n");
        sb.append("---------------\n");
        sb.append("Order ID: ").append(orderId).append("\n");
        sb.append("Customer ID: ").append(customerId).append("\n");
        sb.append("Restaurant ID: ").append(restaurantId).append("\n");
        sb.append("Status: ").append(status).append("\n");
        sb.append("Delivery Type: ").append(deliveryType).append("\n");
        sb.append("Robot Delivery: ").append(robot ? "Yes" : "No").append("\n");
        sb.append("Order Time: ").append(orderTime).append("\n");
        sb.append("Required Time: ").append(requiredTime).append("\n");
        sb.append("Delivery Address: ").append(deliveryAddress).append("\n");
        sb.append("Recipient Name: ").append(recipientName).append("\n");
        sb.append("Recipient Phone: ").append(recipientPhone).append("\n");
        sb.append("Paid: ").append(isPayed ? "Yes" : "No").append("\n");
        sb.append("Discount Applied: ").append(discountApplied ? "Yes" : "No").append("\n");
        sb.append("\nTotal Price: $").append(totalPrice);
        return sb.toString();
    }
}