package controller;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import entities.Message;

/**
 * The ControllerCommunicationUtility class provides a centralized mechanism for
 * inter-controller communication using the Observer pattern.
 * It is implemented as a singleton to ensure a single point of communication across the application.
 */
public class ControllerCommunicationUtility {
	
	/**
	 * The single instance of ControllerCommunicationUtility, following the Singleton pattern.
	 */
	private static ControllerCommunicationUtility instance;

	/**
	 * The PropertyChangeSupport object used to manage listeners and fire property change events.
	 * This is the core mechanism for the Observer pattern implementation in this utility.
	 */
	private PropertyChangeSupport support;
	
    /**
     * Private constructor to prevent instantiation.
     * Initializes the PropertyChangeSupport object.
     */
    private ControllerCommunicationUtility() {
        support = new PropertyChangeSupport(this);
    }

    /**
     * Returns the singleton instance of ControllerCommunicationUtility.
     *
     * @return The singleton instance of ControllerCommunicationUtility.
     */
    public static ControllerCommunicationUtility getInstance() {
        if (instance == null) {
            instance = new ControllerCommunicationUtility();
        }
        return instance;
    }

    /**
     * Adds a PropertyChangeListener to the listener list.
     *
     * @param pcl The PropertyChangeListener to be added.
     */
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        support.addPropertyChangeListener(pcl);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param pcl The PropertyChangeListener to be removed.
     */
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        support.removePropertyChangeListener(pcl);
    }

    /**
     * Reports a bound property update to any registered listeners.
     *
     * @param propertyName The name of the property that was changed.
     * @param oldValue The old value of the property.
     * @param newValue The new value of the property.
     */
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        support.firePropertyChange(propertyName, oldValue, newValue);
    }

    /**
     * Sends a Message object to all registered listeners.
     * This method is a convenience wrapper around firePropertyChange,
     * specifically for sending Message objects.
     *
     * @param message The Message object to be sent.
     */
    public void sendMessage(Message<?> message) {
        support.firePropertyChange("message", null, message);
    }
}