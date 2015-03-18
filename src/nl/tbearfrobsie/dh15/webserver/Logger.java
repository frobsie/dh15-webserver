package nl.tbearfrobsie.dh15.webserver;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static String LOG_FILE_SERVER = "./log/server.log";
	private static String LOG_FILE_ACCESS = "./log/access.log";
	
	public static final Integer LOG_TYPE_SERVER = 1;
	public static final Integer LOG_TYPE_ACCESS = 2;
	
	private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static void append(Integer type, String message) {
	    String logFile = "";

	    switch(type) {
	        case 1:
	            logFile = LOG_FILE_SERVER;
	            break;
	        case 2:
	            logFile = LOG_FILE_ACCESS;
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
