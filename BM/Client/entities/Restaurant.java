package entities;

import java.io.Serializable;

/**
 * The Restaurant class represents a restaurant in the Bite Me system.
 * It stores the restaurant's details including ID, name, password, contact information, and location.
 */

@SuppressWarnings("serial")
public class Restaurant implements Serializable {

    /**
     * The unique identifier of the restaurant.
     */
    private String restaurantId;

    /**
     * The password for the restaurant's account (hashed).
     */
    private String password;

    /**
     * The name of the restaurant.
     */
    private String name;

    /**
     * The phone number of the restaurant.
     */
    private String phoneNumber;

    /**
     * The location or address of the restaurant.
     */
    private String location;
    
    /**
     * The branch which the restaurant belongs to.
     */
    private branch branch;
    
    /**
     * Represents the branches.
     */
    public enum branch {
    	NORTH, SOUTH, CENTER;
    }

    /**
     * Creates an empty Restaurant object (no-argument constructor).
     */
    public Restaurant() {}

    /**
     * Creates a Restaurant object with the specified details.
     *
     * @param restaurantId The unique identifier of the restaurant.
     * @param password The password for the restaurant's account (hashed).
     * @param name The name of the restaurant.
     * @param phoneNumber The phone number of the restaurant.
     * @param location The location or address of the restaurant.
     * @param branch The branch that the restaurant belongs to.
     */
    public Restaurant(String restaurantId, String password, String name, String phoneNumber, String location, branch branch) {
        this.restaurantId = restaurantId;
        this.password = password;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.location = location;
        this.branch = branch;
    }

    // Getters and setters for all fields

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
    
    public branch getBranch() {
    	return branch;
    }
    
    public void setBranch(branch branch) {
    	this.branch = branch;
    }

    /**
     * Returns a string name of the Restaurant.
     *
     * @return A string containing the restaurant's name.
     */
    @Override
    public String toString() {
        return this.getName();
    }
}