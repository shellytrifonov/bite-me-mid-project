package entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a generic message for system communication.
 * This class encapsulates a string message and a payload of any type.
 *
 * @param <T> The type of the object
 */
public class Message<T> implements Serializable {
    private static final long serialVersionUID = 1L; // Explicit serialization ID

    private final String message;
    private final T obj;

    /**
     * Constructs a Message with both a string message and a payload.
     *
     * @param message The string message (cannot be null)
     * @param obj The object (can be null)
     * @throws IllegalArgumentException if the message is null
     */
    public Message(String message, T obj) {
        this.message = Objects.requireNonNull(message, "Message cannot be null");
        this.obj = obj;
    }

    /**
     * Constructs a Message with only a string message.
     *
     * @param message The string message (cannot be null)
     * @throws IllegalArgumentException if the message is null
     */
    public Message(String message) {
        this(message, null);
    }

    /**
     * Gets the string message.
     *
     * @return The string message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the message object.
     *
     * @return The message object, can be null
     */
    public T getType() {
        return obj;
    }

    /**
     * Checks if the message has object.
     *
     * @return true if the obj is not null, false otherwise
     */
    public boolean hasObj() {
        return obj != null;
    }

    @Override
    public String toString() {
        return "Message{" + "message='" + message + '\'' + ", obj=" + obj + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message<?> message1 = (Message<?>) o;
        return Objects.equals(message, message1.message) && Objects.equals(obj, message1.obj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, obj);
    }
}