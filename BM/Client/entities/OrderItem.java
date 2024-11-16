package entities;

import java.io.Serializable;

/**
 * The OrderItem class represents an individual item within an order in the Bite Me system.
 * It links an order to a specific menu item and includes quantity and special instructions.
 */
@SuppressWarnings("serial")
public class OrderItem implements Serializable {

    /**
     * The unique identifier of the order item.
     */
    private int orderItemId;

    /**
     * The ID of the order this item belongs to.
     */
    private int orderId;

    /**
     * The ID of the menu item.
     */
    private int itemId;

    /**
     * The quantity of this item in the order.
     */
    private int quantity;

    /**
     * Any special instructions for this item.
     */
    private String specialInstructions;

    /**
     * Creates an empty OrderItem object (no-argument constructor).
     */
    public OrderItem() {}

    /**
     * Creates an OrderItem object with the specified details.
     *
     * @param orderItemId The unique identifier of the order item.
     * @param orderId The ID of the order this item belongs to.
     * @param itemId The ID of the menu item.
     * @param quantity The quantity of this item in the order.
     * @param specialInstructions Any special instructions for this item.
     */
    public OrderItem(int orderItemId, int orderId, int itemId, int quantity, String specialInstructions) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.itemId = itemId;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
    }

    // Getters and setters for all fields

    public int getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(int orderItemId) {
        this.orderItemId = orderItemId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    /**
     * Returns a string representation of the OrderItem.
     *
     * @return A string containing the order item's details.
     */
    @Override
    public String toString() {
        return "OrderItem{" +
                "orderItemId=" + orderItemId +
                ", orderId=" + orderId +
                ", itemId=" + itemId +
                ", quantity=" + quantity +
                ", specialInstructions='" + specialInstructions + '\'' +
                '}';
    }
}