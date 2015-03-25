package nl.tbearfrobsie.dh15.webserver;

import java.io.UnsupportedEncodingException;
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
	
	private SecureRandom random = new SecureRandom();
	
	public User(int id, String username, String password, String role, String cookieId) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.role = role;
		this.cookieId = cookieId;
	}
	
	public User() {
		this.loggedIn = false;
		this.anonymous = true;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getUsername() {
		return this.username;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getRole() {
		return this.role;
	}
	
	public boolean hasRole(String role) {
		if(this.role.equals(role)) {
			return true;
		}
		return false;
	}
	
	public String getCookieId() {
		return this.cookieId;
	}
	
	public boolean checkPassword(String passwordToCheck) {
		String saltstring = this.username+passwordToCheck+ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_SALT);
		String passwd = hash256(saltstring);
		if(password.equals(passwd)) {
			return true;
		}
		return false;
	}
	
	public boolean isLoggedIn() {
		if(this.loggedIn && !this.anonymous) {
			return true;
		} 
		return false;
	}
	
	public void setLoggedIn(boolean loggedin) {
		this.loggedIn = loggedin;
	}
	
	public String generateCookieId() {
		this.cookieId = new BigInteger(130, random).toString(32);
		return this.getCookieId();
	}
	
	public void destroyCookieId() {
		this.cookieId = null;
	}
	
	private String hash256(String passwdString)
	{
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
