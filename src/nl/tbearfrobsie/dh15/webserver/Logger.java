package nl.tbearfrobsie.dh15.webserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	/** Server log file */
	private static String LOG_FILE_SERVER = "./log/server.log";
	
	/** Access log file */
	private static String LOG_FILE_ACCESS = "./log/access.log";
	
	/** Access log extended file */
	private static String LOG_FILE_ACCESS_EXTENDED = "./log/access.extended.log";
	
	/** Log type SERVER */
	public static final Integer LOG_TYPE_SERVER = 1;
	
	/** Log type ACCESS */
	public static final Integer LOG_TYPE_ACCESS = 2;
	
	/** Log type ACCESS_EXTENDED */
	public static final Integer LOG_TYPE_ACCESS_EXTENDED = 3;
	
	/** Date format for logging */
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	/**
	 * Probeert om een opgegeven message
	 * toe te voegen aan een log file.
	 * 
	 * @param type
	 * @param message
	 */
	public static void append(Integer type, String message) {
	    String logFile = "";

	    switch(type) {
	        case 1:
	            logFile = LOG_FILE_SERVER;
	            break;
	        case 2:
	            logFile = LOG_FILE_ACCESS;
	            break;
	        case 3:
	            logFile = LOG_FILE_ACCESS_EXTENDED;
	            break;
	    }
	    
	    try ( PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(logFile, true))) ) {
	    	Date date = new Date();
	        out.print("[" + dateFormat.format(date) +  "] " + message + System.lineSeparator());
	        
	    } catch (IOException e) {
	        // TODO
	    	System.out.println(e.getMessage());
	    }
	}
}
