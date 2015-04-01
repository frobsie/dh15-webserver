package nl.tbearfrobsie.dh15.webserver;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class User {
	private int id;
	private String username;
	private String password;
	private String role;
	private String cookieId;
	
	private boolean loggedIn;
	private boolean anonymous;
	
	public static String ROLE_BEHEERDERS = "beheerders";
	public static String ROLE_ONDERSTEUNERS = "ondersteuners";
	
	/** Random needed for cookie id generation */
	private SecureRandom random = new SecureRandom();
	
	/**
	 * Existing user constructor
	 * 
	 * @param int id
	 * @param String username
	 * @param String password
	 * @param String role
	 * @param String cookieId
	 */
	public User(int id, String username, String password, String role, String cookieId) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.cookieId = cookieId;
	}
	
	/**
	 * New User constructor 
	 * 
	 * @param String username
	 * @param String password
	 * @param String role
	 */
	public User(String username, String password, String role) {
		this.username = username;
		setPassword(password);
		this.role = role;
	}
	
	/**
	 * Default constructor.
	 */
	public User() {
		this.loggedIn = false;
		this.anonymous = true;
	}
	
	/**
	 * Returns the id of the user.
	 * 
	 * @return int
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Returns the username.
	 * 
	 * @return String
	 */
	public String getUsername() {
		return this.username;
	}
	
	/**
	 * Returns the password.
	 * 
	 * @return String
	 */
	public String getPassword() {
		return this.password;
	}
	
	/**
	 * Returns the role.
	 * 
	 * @return String
	 */
	public String getRole() {
		return this.role;
	}
	
	/**
	 * Checks if this user has
	 * a given role.
	 * 
	 * @param String role
	 * @return boolean
	 */
	public boolean hasRole(String role) {
		if(this.role.equals(role)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Returns the cookie id.
	 * 
	 * @return String
	 */
	public String getCookieId() {
		return this.cookieId;
	}
	
	/**
	 * Checks if a given password
	 * matches the password of the
	 * current user object.
	 * 
	 * @param String passwordToCheck
	 * @return boolean
	 */
	public boolean checkPassword(String passwordToCheck) {
		String passwd = cryptPassword(passwordToCheck);
		if(password.equals(passwd)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Sets a new password.
	 * 
	 * @param password
	 * @return void
	 */
	private void setPassword(String password) {
		this.password = cryptPassword(password);
	}
	
	/**
	 * Checks if the current user is 
	 * logged in.
	 * 
	 * @return boolean
	 */
	public boolean isLoggedIn() {
		if(this.loggedIn && !this.anonymous) {
			return true;
		} 
		return false;
	}
	
	/**
	 * Sets the current user loggedin (or not).
	 * 
	 * @param boolean loggedin
	 * @return void
	 */
	public void setLoggedIn(boolean loggedin) {
		this.loggedIn = loggedin;
	}
	
	/**
	 * Generates a new cookie id.
	 * 
	 * @return String
	 */
	public String generateCookieId() {
		this.cookieId = new BigInteger(130, random).toString(32);
		return this.getCookieId();
	}
	
	/**
	 * Unsets the curent user's cookie id.
	 * 
	 * @return void
	 */
	public void destroyCookieId() {
		this.cookieId = null;
	}
	
	// TODO move cryptPassword & hash256 to util
	/**
	 * Hashes a given password string
	 * with sha256 and an added salt.
	 * 
	 * @param String password
	 * @return String
	 */
	private String cryptPassword(String password) {
		String saltstring = this.username+password+ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_SALT);
		return hash256(saltstring);
	}
	
	/**
	 * Hashes a string with sha256.
	 * 
	 * @param String passwdString
	 * @return String
	 */
	private String hash256(String passwdString) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(passwdString.getBytes());
	        
	        byte[] mdbytes = md.digest();
	 
	        //convert the byte to hex format method 1
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < mdbytes.length; i++) {
	          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        return null;
	}
}
