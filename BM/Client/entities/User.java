package entities;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The User class represents a user in the Bite Me system.
 * It stores the user's details including ID, name, contact information, role and account balance.
 */
@SuppressWarnings("serial")
public class User implements Serializable {

    /**
     * The unique identifier of the user.
     */
    private String userId;
    
    /**
     * Credit card number for private customers.
     */
    private String creditCard;

    /**
     * The first name of the user.
     */
    private String firstName;

    /**
     * The last name of the user.
     */
    private String lastName;

    /**
     * The email address of the user.
     */
    private String email;

    /**
     * The phone number of the user.
     */
    private String phoneNumber;

    /**
     * The password of the user.
     */
    private String password;
    
    /**
     * The credit of the user.
     */
    private BigDecimal credit;

    /**
     * The role of the user in the system.
     */
    private UserRole role;

    /**
     * Enum representing possible user roles in the system.
     */
    public enum UserRole {
        CUSTOMER_BUSINESS, CUSTOMER_PRIVATE, MANAGER, CEO, RESTAURANT
    }

    /**
     * Creates an empty User object (no-argument constructor).
     */
    public User() {}
    
    /**
     * Creates a User object with the specified details.
     * 
     * @param username The unique identifier of the user.
     * @param password The password of the user.
     */
    public User(String userId, String password) {
    	this.userId = userId;
    	this.password = password;
    }
    
    /**
     * Creates a User object with the specified details.
     *
     * @param userId The unique identifier of the user.
     * @param firstName The first name of the user.
     * @param lastName The last name of the user.
     * @param email The email address of the user.
     * @param phoneNumber The phone number of the user.
     * @param password The password of the user.
     * @param role The role of the user in the system.
     * @param balance The current account balance of the user.
     */
    public User(String userId, String firstName, String lastName, String email, String phoneNumber, String password, UserRole role, BigDecimal credit) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
        this.credit = credit;
    }
    
    /**
     * Creates a User that is private customer.
     * @param userId The unique identifier of the user.
     * @param phoneNumber The phone number of the user.
     * @param creditCard credit card number of the user.
     * @param role Private user.
     * @param password The password of the user.
     */
    public User(String userId, String phoneNumber, String creditCard, UserRole role, String password, BigDecimal credit) {
    	this.userId = userId;
    	this.phoneNumber = phoneNumber;
    	this.creditCard = creditCard;
    	this.role = UserRole.CUSTOMER_PRIVATE;
    	this.password = password;
    	this.credit = credit;
    }

    // Getters and setters for all fields

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getCreditCard() {
    	return creditCard;
    }
    
    public void setCreditCard(String creditCard) {
    	this.creditCard = creditCard;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public BigDecimal getCredit() {
    	return credit;
    }
    
    public void setCredit(BigDecimal credit) {
    	this.credit = credit;
    }
}