package entities;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The MenuItem class represents an item on a restaurant's menu in the Bite Me system.
 * It stores details about the item including its name, description, price, availability, and type.
 */
@SuppressWarnings("serial")
public class MenuItem implements Serializable {

    /**
     * The unique identifier of the menu item.
     */
    private int itemId;

    /**
     * The ID of the restaurant this menu item belongs to.
     */
    private String restaurantId;

    /**
     * The name of the menu item.
     */
    private String name;
    
    /**
     * The amount of the item.
     */
    private int quantity;

    /**
     * A description of the menu item.
     */
    private String description;

    /**
     * The price of the menu item.
     */
    private BigDecimal price;

    /**
     * Indicates whether the item is currently in stock.
     */
    private boolean isInStock;

    /**
     * The type of the menu item (e.g., drink, main course, etc.).
     */
    private ItemType type;

    /**
     * Enum representing possible types of menu items.
     */
    public enum ItemType {
        DRINK, MAIN, APPETIZER, SALAD, DESSERT
    }

    /**
     * Creates an empty MenuItem object (no-argument constructor).
     */
    public MenuItem() {}

    /**
     * Creates a MenuItem object with the specified details.
     *
     * @param itemId The unique identifier of the menu item.
     * @param restaurantId The ID of the restaurant this menu item belongs to.
     * @param name The name of the menu item.
     * @param description A description of the menu item.
     * @param price The price of the menu item.
     * @param isInStock Whether the item is currently in stock.
     * @param type The type of the menu item.
     */
    public MenuItem(int itemId, String restaurantId, String name, String description, 
                    BigDecimal price, boolean isInStock, ItemType type, int quantity) {
        this.itemId = itemId;
        this.restaurantId = restaurantId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.isInStock = isInStock;
        this.type = type;
        this.setQuantity(quantity);
    }

    // Getters and setters for all fields

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public boolean isInStock() {
        return isInStock;
    }

    public void setInStock(boolean inStock) {
        isInStock = inStock;
    }

    public ItemType getType() {
        return type;
    }

    public void setType(ItemType type) {
        this.type = type;
    }
    
    public int getQuantity() {
    	return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.isInStock = quantity > 0;
    }

    /**
     * Returns a string representation of the MenuItem name.
     *
     * @return A string containing the menu item's name.
     */
    @Override
    public String toString() {
        return this.getName();
    }
}