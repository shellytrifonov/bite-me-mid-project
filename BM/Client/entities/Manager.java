package entities;

import java.io.Serializable;

/**
 * The Manager class represents a manager in the Bite Me system.
 * It links a user (acting as a manager) to a specific restaurant.
 */
@SuppressWarnings("serial")
public class Manager implements Serializable {

    /**
     * The ID of the user who is a manager.
     * This corresponds to a userId in the Users table.
     */
    private int managerId;

    /**
     * The ID of the restaurant this manager is associated with.
     * This corresponds to a restaurantId in the Restaurants table.
     */
    private int restaurantId;

    /**
     * Creates an empty Manager object (no-argument constructor).
     */
    public Manager() {}

    /**
     * Creates a Manager object with the specified details.
     *
     * @param managerId The ID of the user who is a manager.
     * @param restaurantId The ID of the restaurant this manager is associated with.
     */
    public Manager(int managerId, int restaurantId) {
        this.managerId = managerId;
        this.restaurantId = restaurantId;
    }

    // Getters and setters for all fields

    public int getManagerId() {
        return managerId;
    }

    public void setManagerId(int managerId) {
        this.managerId = managerId;
    }

    public int getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(int restaurantId) {
        this.restaurantId = restaurantId;
    }

    /**
     * Returns a string representation of the Manager.
     *
     * @return A string containing the manager's details.
     */
    @Override
    public String toString() {
        return "Manager{" +
                "managerId=" + managerId +
                ", restaurantId=" + restaurantId +
                '}';
    }
}