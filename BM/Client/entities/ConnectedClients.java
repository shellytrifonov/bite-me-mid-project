package entities;

import java.io.Serializable;

import entities.User.UserRole;

/**
 * Represents a connected client in the system.
 * This class stores information about a client's connection details and role.
 */
@SuppressWarnings("serial")
public class ConnectedClients implements Serializable {
	
	/**
	 * Ip address of the connected client. 
	 */
	private String ip;
	
	/**
	 * Host name of the connected client.
	 */
	private String hostName;
	
	/**
	 * Id of the connected client.
	 */
	private String id;
	
	/**
	 * Role of the connected client.
	 */
	private UserRole role;
	
    /**
     * Creates a ConnectedClients object with the specified details.
     *
     * @param ip The clients ip address.
     * @param hostName The clients host name.
     * @param id The clients id.
     * @param role The clients role.
     */
	public ConnectedClients(String ip, String hostName, String id, UserRole role) {
		this.ip = ip;
		this.hostName = hostName;
		this.id = id;
		this.role = role;
	}
	
    // Getters and setters for all fields

	public String getIp() {
		return ip;
	}

	public String getHostName() {
		return hostName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}
}
