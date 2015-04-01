package nl.tbearfrobsie.dh15.webserver.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Util {
	
	/**
	 * Hashes a string with sha256.
	 * 
	 * @param String stringToHash
	 * @return String
	 */
	public final static String hash256(String stringToHash) {
		MessageDigest md;
		String retVal = "";
		try {
			md = MessageDigest.getInstance("SHA-256");
			md.update(stringToHash.getBytes());
	        
	        byte[] mdbytes = md.digest();
	 
	        //convert the byte to hex format method 1
	        StringBuffer sb = new StringBuffer();
	        for (int i = 0; i < mdbytes.length; i++) {
	          sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        retVal =  sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
        return retVal;
	}
}
