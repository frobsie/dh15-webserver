package nl.tbearfrobsie.dh15.webserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class ClientThread implements Runnable {

	public static Integer BUFFERSIZE = 128;
	public static String SERVER_OPENEDSOCKET = "Socket opened: ";
	public static String SERVER_CLOSINGSOCKET = "Closing socket: ";
	public static String SERVER_STDOUT_SERVER = "[SERVER] ";
	public static String SERVER_STDOUT_SEND = "[SEND]   ";
	public static String SERVER_STDOUT_RECV = "[RECV]   ";
	public static String SERVER_STDOUT_ERROR = "[ERROR]  ";

	public static String DEFAULT_DATE_FORMAT = "MM-dd-yyyy HH:mm:ss";

	public static String ERROR_CLIENT_CLOSED_CONNECTION = "Error : Client closed connection.";
	public static String ERROR_CLIENT_FILENOTEXISTS = "File does not exist.";

	public static String ADMIN_URI = "/adminerino";
	public static String URI_SHOWLOG = "/showlog";
	public static String URI_CLEARLOGS = "/clearlogs";
	public static String URI_LOGOUT = "/logout";
	public static String URI_USERS = "/users";
	public static String URI_USER_NEW = "/users/new";
	public static String URI_USER_DELETE = "/users/delete/";

	public static String EMPTY_STR = "";

	public static String MSG_PROTOCOL_GET = "GET";
	public static String MSG_PROTOCOL_POST = "POST";

	public static String MSG_PROTOCOL_HEADER_HTTP = "HTTP/1.0 ";
	public static String MSG_PROTOCOL_HEADER_CONTENTTYPE = "Content-Type: ";
	public static String MSG_PROTOCOL_HEADER_CONTENTLENGTH = "Content-Length: ";
	public static String MSG_PROTOCOL_HEADER_LOCATION = "Location: ";
	public static String MSG_PROTOCOL_HEADER_NOSNIFF = "X-Content-Type-Options: nosniff";
	public static String MSG_PROTOCOL_HEADER_CLICKJACK = "X-Frame-Options: deny";
	public static String COOKIENAME = "UserCookieId";
	public static String MSG_PROTOCOL_HEADER_COOKIE = "Set-Cookie: "+COOKIENAME+"=";
	public static String MSG_PROTOCOL_HEADER_COOKIE_TAIL = "; Path=/;";
	public static String MSG_PROTOCOL_REQUEST_HEADER_COOKIE = "Cookie: "+COOKIENAME+"=";


	public static String MSG_URI_ROOT = "/";
	public static String MSG_URI_PARENTFOLDER = "../";
	public static String MSG_PROTOCOL_DEFAULTMIMETYPE = "text/html";
	public static String MSG_SOCKINFO_DELIMITER_START = "[";
	public static String MSG_SOCKINFO_DELIMITER_END = "] ";
	public static String MSG_LOGS_CLEARED = "Logs cleared.";

	public static String URI_DELIMITER = "/";
	public static String URI_SPLIT_DELIMITER = "&";
	public static String URI_SPLIT_DELIMITER_VALUE = "=";

	public static String STATUSCODE_200_STR = "200 Ok";
	public static String STATUSCODE_203_STR = "203 No Content";
	public static String STATUSCODE_301_STR = "301 Moved Permanently";
	public static String STATUSCODE_307_STR = "307 Temporary Redirect";
	public static String STATUSCODE_400_STR = "400 Bad Request";
	public static String STATUSCODE_403_STR = "403 Forbidden";
	public static String STATUSCODE_404_STR = "404 Not Found";

	public static String POST_USERNAME = "username";
	public static String POST_PASSWORD = "password";
	public static String POST_ROLE = "role";

	/** The socket on which the client is connected */
	private Socket socket;

	private InputStream inputStream;

	/** Converts bytestream to characterstream */
	private InputStreamReader inputStreamReader;

	/** Reader for receiving user input */
	private BufferedReader bufferedReader;

	/** Writer for sending text to the client */
	private PrintWriter printWriter;

	public FileServer fileServer;

	private Boolean directoryBrowsingAllowed;

	private Integer lastSentStatusCode = -1;

	private User user;

	/**
	 * Setup worker thread with given socket.
	 * 
	 * @param socket
	 * @param fileServer
	 */
	public ClientThread(Socket socket, FileServer fileServer) {
		this.socket = socket;
		this.fileServer = fileServer;

		directoryBrowsingAllowed = Boolean.valueOf(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING));

		user = new User();
	}

	/**
	 * Runnable implementation
	 */
	@Override
	public void run() {
		try {
			printLine(SERVER_OPENEDSOCKET + getSocketInfo(), 0);

			// init
			inputStream = socket.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			printWriter = new PrintWriter(socket.getOutputStream(), true);

			ArrayList<String> lines = readTillEmptyLine();

			// only process when there are lines
			if(lines.size() > 0) {
				//read first line
				readRequestMethod(lines);
			}

			printLine(SERVER_CLOSINGSOCKET + getSocketInfo(), 0);

			// close when there is no more input
			inputStreamReader.close();
			bufferedReader.close();
			socket.close();

		} catch (SocketException e) {
			printLine(ERROR_CLIENT_CLOSED_CONNECTION, 3);
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	/**
	 * Handles user input
	 *
	 * @param lines
	 * @throws IOException
	 */
	protected void readRequestMethod(ArrayList<String> lines) throws IOException, Exception {
		String line = lines.get(0);
		printLine(line, 2);

		// split the input string by whitespace
		String[] lineSplit = line.split(" ");

		// should have 3 indexes (method uri protocol)
		if (lineSplit.length < 3) {
			plot400();
			return;
		}

		String method = lineSplit[0];
		String uri = lineSplit[1];
		String protocol = lineSplit[2];

		tryUserLogin(lines);

		if(method.equals(MSG_PROTOCOL_GET)) {
			handleGet(uri, protocol);
		} else if (method.equals(MSG_PROTOCOL_POST)) {
			Integer contentLength = 0;
			for(int i = 0; i < lines.size(); i++) {
				if (lines.get(i).startsWith(MSG_PROTOCOL_HEADER_CONTENTLENGTH)) {
					contentLength = Integer.parseInt(lines.get(i).substring(MSG_PROTOCOL_HEADER_CONTENTLENGTH.length()));
				}	
			}            
			handlePost(uri, protocol, contentLength);
		} else {
			// All other methods return 400 
			plot400();
		}

		Logger.append(Logger.LOG_TYPE_ACCESS, createAccessLogLine(line));
		Logger.append(Logger.LOG_TYPE_ACCESS_EXTENDED, createAccessLogExtendedLine(lines));
	}

	protected String createAccessLogLine(String line) {
		String retVal = String.format("%s " + "\"" + line + "\"" + " %s", getSocketInfo(), lastSentStatusCode);
		return retVal;
	}

	protected String createAccessLogExtendedLine(List<String> lines) {
		String retVal = EMPTY_STR; 

		Iterator<String> it = lines.iterator();
		while(it.hasNext()) {
			String line = it.next();
			retVal += line + System.lineSeparator();
		}

		retVal = String.format("%s %s " + "\"" + retVal + "\"", getSocketInfo(), lastSentStatusCode);
		retVal += System.lineSeparator();

		return retVal;
	}

	/**
	 * Temporary GET handler
	 * 
	 * @param uri
	 * @param protocol
	 * @throws IOException
	 */
	private void handleGet(String uri, String protocol) throws IOException {		
		// if ask for root get default page (TODO accept multiple defaults)
		if(uri.equals(MSG_URI_ROOT) && !directoryBrowsingAllowed) {
			uri = getDefaultpageUri();
		}

		String fullPath = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + uri;

		// check if fullpath contains ../ (can cause directory scanning issues)
		if(fullPath.contains(MSG_URI_PARENTFOLDER)) {
			plot403();
			return;
		}

		printLine(fullPath, 0);
		File file = new File(fullPath);

		printLine(fullPath, 3);
		printLine(uri, 1);

		// Als we directories mogen listen
		// en de opgevraagde resource is een map
		if (directoryBrowsingAllowed && file.isDirectory()) {        	
			String folderContent = HtmlProvider.listFolderContent(uri); 
			sendResponseHeader(200, folderContent.length());

			sendLine(folderContent);
			return;
		}

		// als het een file is
		if (file.isFile()) {	        
			// Print Headers
			// TODO find better solution
			FileResource fr = new FileResource(fullPath);
			sendResponseHeader(200, fr.getByteSize(), getContentType(fr));

			sendFile(fr);
			return;
		}

		printLine(uri, 1);

		// TODO
		// Dit moet anders 
		if(uri.equals(ADMIN_URI)) {
			String form = HtmlProvider.getManageForm(user);
			sendResponseHeader(200, form.length());

			sendLine(form);
			return;
		}

		if(uri.equals(URI_LOGOUT)) {
			System.out.println("logout");
			user.destroyCookieId();

			MySQLAccess msa = new MySQLAccess();
			msa.storeUser(user);
			msa.close();
			user = new User();

			redirectUrl(ADMIN_URI);
			return;
		}

		if(uri.equals(URI_SHOWLOG)) {
			if(!user.isLoggedIn()) {
				plot403();
				return;
			}
			String log = HtmlProvider.getLog();
			sendResponseHeader(200, log.length());
			sendLine(log);
			return;
		}

		if(uri.equals(URI_CLEARLOGS)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String cleared = MSG_LOGS_CLEARED;
			clearLogFiles();
			sendResponseHeader(200, cleared.length());
			sendLine(cleared);
			return;
		}

		if(uri.equals(URI_USERS)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String users = HtmlProvider.listUserContent();
			sendResponseHeader(200, users.length());
			sendLine(users);
			return;
		}


		if(uri.equals(URI_USER_NEW)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String userForm = HtmlProvider.getNewUserForm();
			sendResponseHeader(200, userForm.length());
			sendLine(userForm);
			return;
		}

		if(uri.contains(URI_USER_DELETE)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			int id = Integer.parseInt(uri.substring(URI_USER_DELETE.length()));

			MySQLAccess msa = new MySQLAccess();
			msa.deleteUser(id);
			msa.close();
			redirectUrl(URI_USERS);
			return;
		}

		// Als de opgevraagde resource niet bestaat
		// dan 404 tonen
		plot404();
		printLine(ERROR_CLIENT_FILENOTEXISTS, 3);
	}

	protected void clearLogFiles() throws FileNotFoundException {
		PrintWriter a = new PrintWriter(Logger.LOG_FILE_ACCESS);
		PrintWriter ax = new PrintWriter(Logger.LOG_FILE_ACCESS_EXTENDED);

		a.print(EMPTY_STR);
		ax.print(EMPTY_STR);

		a.close();
		ax.close();
	}

	/**
	 * Collect the default page from the config and check if one of the options exists.
	 * @return String 
	 */
	protected String getDefaultpageUri() {
		String[] lineSplit = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE).split(";");
		for(int i = 0; i < lineSplit.length; i++) {
			File file = new File(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + URI_DELIMITER + lineSplit[i]);
			if(file.exists()) {
				return URI_DELIMITER + lineSplit[i];
			}
		}
		return URI_DELIMITER + lineSplit[0];
	}

	/**
	 * send 404 page to the user.
	 * @throws IOException
	 */
	protected void plot404() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_ERRORPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(404, errorFile.getByteSize(), getContentType(errorFile));
		sendFile(errorFile);
	}

	/**
	 * send 403 page to the user.
	 * @throws IOException
	 */
	protected void plot403() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_FORBIDDENPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(403, errorFile.getByteSize(), getContentType(errorFile));
		sendFile(errorFile);
	}

	/**
	 * send 400 page to the user.
	 * @throws IOException
	 */
	protected void plot400() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_BADREQUESTPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(400, errorFile.getByteSize(), getContentType(errorFile));
		sendFile(errorFile);
	}

	/**
	 * Handle POST requests 
	 * @param uri
	 * @param protocol
	 * @param contentLength
	 * @throws IOException
	 * @throws Exception
	 */
	protected void handlePost(String uri, String protocol, int contentLength) throws IOException, Exception {
		printLine(uri,2);

		HashMap<String, String> post = new HashMap<String, String>();
		if(contentLength > 0) {
			char[] data = new char[contentLength];
			bufferedReader.read(data);
			String[] rawPost = (new String(data)).split(URI_SPLIT_DELIMITER);
			for(int i = 0; i < rawPost.length; i++) {
				String[] split = rawPost[i].split(URI_SPLIT_DELIMITER_VALUE);
				String value = null;
				// no value given
				if(split.length == 2) {
					value = split[1];
				}
				post.put(split[0], value);
			}
		}

		if(uri.equals(ADMIN_URI)) {
			if(user.isLoggedIn()) { 	
				if(!user.hasRole(User.ROLE_BEHEERDERS)) {
					plot403();
					return;
				}
				printLine(post.toString(),2);
				// UGLY :D
				// - joh echt?
				if(post.containsKey(ConfigPropertyValues.CONFIG_KEY_PORT)) {
					ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_PORT, new Integer(post.get(ConfigPropertyValues.CONFIG_KEY_PORT)).toString());
				}
				if(post.containsKey(ConfigPropertyValues.CONFIG_KEY_DOCROOT)) {
					ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DOCROOT, post.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT));
				}
				if(post.containsKey(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE)) {
					ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE, post.get(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE));
				}
				if(post.containsKey(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING)) {
					ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING, ConfigPropertyValues.CONFIG_VALUE_STR_TRUE);
				} else {
					ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING, ConfigPropertyValues.CONFIG_VALUE_STR_FALSE);
				}
				ConfigPropertyValues.write();
				sendResponseHeader(203, 0);

				//Close socket so a new one will start on another port
				try {
					socket.close();
					fileServer.closeServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			} else {
				if(post.containsKey(POST_USERNAME) && post.containsKey(POST_PASSWORD)) {
					MySQLAccess msa = new MySQLAccess();
					User user = msa.readUser(post.get(POST_USERNAME));
					if(null == user) {
						System.out.println("No user");
						plot403();
						return;
					} 

					if(!user.checkPassword(post.get(POST_PASSWORD))) {
						plot403();
						return;
					}
					user.setLoggedIn(true);
					user.generateCookieId();

					msa.storeUser(user);
					this.user = user;
					handleGet(ADMIN_URI, MSG_PROTOCOL_HEADER_HTTP);
					msa.close();
				}
			}
		} else if(uri.equals(URI_USER_NEW)) {
			printLine(post.toString(),2);
			// UGLY :D
			// - joh echt?
			if(post.containsKey(POST_USERNAME) && post.containsKey(POST_PASSWORD) && post.containsKey(POST_ROLE)) {
				User user = new User(post.get(POST_USERNAME), post.get(POST_PASSWORD), post.get(POST_ROLE));

				MySQLAccess msa = new MySQLAccess();
				msa.createUser(user);
				msa.close();
				redirectUrl(URI_USERS);
				return;
			} else {
				redirectUrl(URI_USER_NEW);
				return;
			}
		}

	}

	/**
	 * Read socket till an blank line is found
	 * There is a blank line between the headers and the content
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> readTillEmptyLine() throws IOException {
		ArrayList<String> lines = new ArrayList<String>();
		String line = bufferedReader.readLine();

		// first read all header lines
		while (line != null && line.length() > 0) {
			lines.add(line);
			line = bufferedReader.readLine();
		}
		return lines;
	}

	/**
	 * send response headers to the client
	 * @param statusCode
	 * @param contentLength
	 */
	private void sendResponseHeader(int statusCode, int contentLength){
		sendResponseHeader(statusCode, contentLength, MSG_PROTOCOL_DEFAULTMIMETYPE);

	}

	/**
	 * send response headers to the client
	 * @param statusCode
	 * @param contentLength
	 * @param contentType
	 */
	private void sendResponseHeader(int statusCode, int contentLength, String contentType) {
		String status = null;

		switch(statusCode) {
		case 200:
			status = STATUSCODE_200_STR;
			break;
		case 203:
			status = STATUSCODE_203_STR;
			break;
		case 301:
			status = STATUSCODE_301_STR;
			break;
		case 400:
			status = STATUSCODE_400_STR;
			break;
		case 403:
			status = STATUSCODE_403_STR;
			break;    
		case 404:
			status = STATUSCODE_404_STR;
			break;
		}
		try {
			sendLine(MSG_PROTOCOL_HEADER_HTTP + status);
			if(user.isLoggedIn()) {
				sendLine(MSG_PROTOCOL_HEADER_COOKIE + user.getCookieId() + MSG_PROTOCOL_HEADER_COOKIE_TAIL);
			}
			sendLine(MSG_PROTOCOL_HEADER_CONTENTTYPE + contentType);
			sendLine(MSG_PROTOCOL_HEADER_CONTENTLENGTH + contentLength);

			// Om "MIME-Sniffing" te voorkomen
			sendLine(MSG_PROTOCOL_HEADER_NOSNIFF);

			// Om "Clickjacking" te voorkomen
			sendLine(MSG_PROTOCOL_HEADER_CLICKJACK);

			sendLine(EMPTY_STR);

			lastSentStatusCode = statusCode;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void redirectUrl(String uri) {
		try {
			sendLine(MSG_PROTOCOL_HEADER_HTTP + STATUSCODE_307_STR);
			sendLine(MSG_PROTOCOL_HEADER_LOCATION + uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * get the socket information formatted
	 * @return
	 */
	protected String getSocketInfo() {
		String retVal = socket.getInetAddress() + ":" + socket.getPort();
		return retVal.replace(URI_DELIMITER, EMPTY_STR);
	}

	/**
	 * Send line to client
	 * @param message
	 * @throws IOException
	 */
	protected void sendLine(String message) throws IOException {
		sendLine(message, false);
	}

	/**
	 * send line to client
	 * @param message
	 * @param toOut
	 * @throws IOException
	 */
	protected void sendLine(String message, boolean toOut) throws IOException {
		printWriter.println(message);

		if (toOut) {
			printLine(message, 1);
		}
	}

	/**
	 * Sends a file to client using DataOutputStreams
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	protected void sendFile(FileResource fr) throws IOException {
		// setup
		FileInputStream fis = new FileInputStream(fr);
		BufferedInputStream bis = new BufferedInputStream(fis, BUFFERSIZE);
		DataInputStream dis = new DataInputStream(bis);
		OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);

		// tries to read the complete file into the inputstream
		dis.readFully(fr.getBytes(), 0, fr.getByteSize());

		// tries to write the complete file into the outputstream
		dos.write(fr.getBytes(), 0, fr.getByteSize());

		// output the buffer to the socket
		dos.flush();

		// close the datainputstream
		dis.close();
	}

	/**
	 * Print a formatted debug line to the console
	 * @param message
	 * @param sendType
	 */
	protected void printLine(String message, Integer sendType) {
		String fm = EMPTY_STR;

		switch (sendType) {
		default:
		case 0:
			fm += SERVER_STDOUT_SERVER;
			break;
		case 1:
			fm += SERVER_STDOUT_SEND;
			break;
		case 2:
			fm += SERVER_STDOUT_RECV;
			break;
		case 3:
			fm += SERVER_STDOUT_ERROR;
			break;
		}

		fm += MSG_SOCKINFO_DELIMITER_START + getSocketInfo() + MSG_SOCKINFO_DELIMITER_END;
		fm += message;

		System.out.println(fm);
	}

	

	/**
	 * Get content type for given file resource.
	 * @param file
	 * @return
	 */
	private String getContentType(FileResource file) {
		return URLConnection.guessContentTypeFromName(file.getName());
	}

	private void tryUserLogin(ArrayList<String> lines) {
		String cookie = "";
		for(int i = 0; i < lines.size(); i++) {
			if (lines.get(i).startsWith(MSG_PROTOCOL_REQUEST_HEADER_COOKIE)) {
				cookie = lines.get(i).substring(MSG_PROTOCOL_REQUEST_HEADER_COOKIE.length());
			}	
		}            

		if(cookie.equals("")) {
			return;
		}

		MySQLAccess msa = new MySQLAccess();
		User user = msa.readUserByCookie(cookie);
		if(null == user) {
			return;
		} 
		user.setLoggedIn(true);

		//Regenerate token
		//user.generateCookieId();

		//msa.storeUser(user);
		this.user = user;
		msa.close();
	}
}
