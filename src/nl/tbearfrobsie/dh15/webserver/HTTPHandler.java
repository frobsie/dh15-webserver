package nl.tbearfrobsie.dh15.webserver;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HTTPHandler {
	
	private Communication comm;
	
	private Integer lastSentStatusCode = -1;
	
	private Boolean directoryBrowsingAllowed;
	
	private User user;
	
	public HTTPHandler(Communication comm) {
		this.comm = comm;
		
		directoryBrowsingAllowed = Boolean.valueOf(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING));
		user = new User();
	}
	 
	public void handle() throws IOException, Exception {
		ArrayList<String> lines = comm.readTillEmptyLine();

		// only process when there are lines
		if(lines.size() > 0) {
			//read first line
			readRequestMethod(lines);
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
		Logger.printLine(line, 2);

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

		if(method.equals(Constant.MSG_PROTOCOL_GET)) {
			handleGet(uri, protocol);
		} else if (method.equals(Constant.MSG_PROTOCOL_POST)) {
			Integer contentLength = 0;
			for(int i = 0; i < lines.size(); i++) {
				if (lines.get(i).startsWith(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH)) {
					contentLength = Integer.parseInt(lines.get(i).substring(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH.length()));
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
		String retVal = String.format("%s " + "\"" + line + "\"" + " %s", comm.getSocketInfo(), lastSentStatusCode);
		return retVal;
	}

	protected String createAccessLogExtendedLine(List<String> lines) {
		String retVal = Constant.EMPTY_STR; 

		Iterator<String> it = lines.iterator();
		while(it.hasNext()) {
			String line = it.next();
			retVal += line + System.lineSeparator();
		}

		retVal = String.format("%s %s " + "\"" + retVal + "\"", comm.getSocketInfo(), lastSentStatusCode);
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
		if(uri.equals(Constant.MSG_URI_ROOT) && !directoryBrowsingAllowed) {
			uri = getDefaultpageUri();
		}

		String fullPath = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + uri;

		// check if fullpath contains ../ (can cause directory scanning issues)
		if(fullPath.contains(Constant.MSG_URI_PARENTFOLDER)) {
			plot403();
			return;
		}

		Logger.printLine(fullPath, 0);
		File file = new File(fullPath);

		Logger.printLine(fullPath, 3);
		Logger.printLine(uri, 1);

		// Als we directories mogen listen
		// en de opgevraagde resource is een map
		if (directoryBrowsingAllowed && file.isDirectory()) {        	
			String folderContent = HtmlProvider.listFolderContent(uri); 
			sendResponseHeader(200, folderContent.length());

			comm.sendLine(folderContent);
			return;
		}

		// als het een file is
		if (file.isFile()) {	        
			// Print Headers
			// TODO find better solution
			FileResource fr = new FileResource(fullPath);
			sendResponseHeader(200, fr.getByteSize(), getContentType(fr));

			comm.sendFile(fr);
			return;
		}

		Logger.printLine(uri, 1);

		// TODO
		// Dit moet anders 
		if(uri.equals(Constant.ADMIN_URI)) {
			String form = HtmlProvider.getManageForm(user);
			sendResponseHeader(200, form.length());

			comm.sendLine(form);
			return;
		}

		if(uri.equals(Constant.URI_LOGOUT)) {
			user.destroyCookieId();

			MySQLAccess msa = new MySQLAccess();
			msa.storeUser(user);
			msa.close();
			user = new User();

			redirectUrl(Constant.ADMIN_URI);
			return;
		}

		if(uri.equals(Constant.URI_SHOWLOG)) {
			if(!user.isLoggedIn()) {
				plot403();
				return;
			}
			String log = HtmlProvider.getLog();
			sendResponseHeader(200, log.length());
			comm.sendLine(log);
			return;
		}

		if(uri.equals(Constant.URI_CLEARLOGS)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String cleared = Constant.MSG_LOGS_CLEARED;
			Logger.clearLogFiles();
			sendResponseHeader(200, cleared.length());
			comm.sendLine(cleared);
			return;
		}

		if(uri.equals(Constant.URI_USERS)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String users = HtmlProvider.listUserContent();
			sendResponseHeader(200, users.length());
			comm.sendLine(users);
			return;
		}


		if(uri.equals(Constant.URI_USER_NEW)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			String userForm = HtmlProvider.getNewUserForm();
			sendResponseHeader(200, userForm.length());
			comm.sendLine(userForm);
			return;
		}

		if(uri.contains(Constant.URI_USER_DELETE)) {
			if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
				plot403();
				return;
			}
			int id = Integer.parseInt(uri.substring(Constant.URI_USER_DELETE.length()));

			MySQLAccess msa = new MySQLAccess();
			msa.deleteUser(id);
			msa.close();
			redirectUrl(Constant.URI_USERS);
			return;
		}

		// Als de opgevraagde resource niet bestaat
		// dan 404 tonen
		plot404();
		Logger.printLine(Constant.ERROR_CLIENT_FILENOTEXISTS, 3);
	}

	/**
	 * Collect the default page from the config and check if one of the options exists.
	 * @return String 
	 */
	protected String getDefaultpageUri() {
		String[] lineSplit = ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE).split(";");
		for(int i = 0; i < lineSplit.length; i++) {
			File file = new File(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + Constant.URI_DELIMITER + lineSplit[i]);
			if(file.exists()) {
				return Constant.URI_DELIMITER + lineSplit[i];
			}
		}
		return Constant.URI_DELIMITER + lineSplit[0];
	}

	/**
	 * send 404 page to the user.
	 * @throws IOException
	 */
	protected void plot404() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_ERRORPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(404, errorFile.getByteSize(), getContentType(errorFile));
		comm.sendFile(errorFile);
	}

	/**
	 * send 403 page to the user.
	 * @throws IOException
	 */
	protected void plot403() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_FORBIDDENPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(403, errorFile.getByteSize(), getContentType(errorFile));
		comm.sendFile(errorFile);
	}

	/**
	 * send 400 page to the user.
	 * @throws IOException
	 */
	protected void plot400() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_BADREQUESTPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(400, errorFile.getByteSize(), getContentType(errorFile));
		comm.sendFile(errorFile);
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
		Logger.printLine(uri,2);

		HashMap<String, String> post = comm.getPost(contentLength);

		if(uri.equals(Constant.ADMIN_URI)) {
			if(user.isLoggedIn()) { 	
				if(!user.hasRole(User.ROLE_BEHEERDERS)) {
					plot403();
					return;
				}
				Logger.printLine(post.toString(),2);
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

				throw new UpdatedConfigException();
			} else {
				if(post.containsKey(Constant.POST_USERNAME) && post.containsKey(Constant.POST_PASSWORD)) {
					MySQLAccess msa = new MySQLAccess();
					User user = msa.readUser(post.get(Constant.POST_USERNAME));
					if(null == user) {
						System.out.println("No user");
						plot403();
						return;
					} 

					if(!user.checkPassword(post.get(Constant.POST_PASSWORD))) {
						plot403();
						return;
					}
					user.setLoggedIn(true);
					user.generateCookieId();

					msa.storeUser(user);
					this.user = user;
					handleGet(Constant.ADMIN_URI, Constant.MSG_PROTOCOL_HEADER_HTTP);
					msa.close();
				}
			}
		} else if(uri.equals(Constant.URI_USER_NEW)) {
			Logger.printLine(post.toString(),2);
			// UGLY :D
			// - joh echt?
			if(post.containsKey(Constant.POST_USERNAME) && post.containsKey(Constant.POST_PASSWORD) && post.containsKey(Constant.POST_ROLE)) {
				User user = new User(post.get(Constant.POST_USERNAME), post.get(Constant.POST_PASSWORD), post.get(Constant.POST_ROLE));

				MySQLAccess msa = new MySQLAccess();
				msa.createUser(user);
				msa.close();
				redirectUrl(Constant.URI_USERS);
				return;
			} else {
				redirectUrl(Constant.URI_USER_NEW);
				return;
			}
		}

	}

	

	/**
	 * send response headers to the client
	 * @param statusCode
	 * @param contentLength
	 */
	private void sendResponseHeader(int statusCode, int contentLength){
		sendResponseHeader(statusCode, contentLength, Constant.MSG_PROTOCOL_DEFAULTMIMETYPE);

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
			status = Constant.STATUSCODE_200_STR;
			break;
		case 203:
			status = Constant.STATUSCODE_203_STR;
			break;
		case 301:
			status = Constant.STATUSCODE_301_STR;
			break;
		case 400:
			status = Constant.STATUSCODE_400_STR;
			break;
		case 403:
			status = Constant.STATUSCODE_403_STR;
			break;    
		case 404:
			status = Constant.STATUSCODE_404_STR;
			break;
		}
		try {
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_HTTP + status);
			if(user.isLoggedIn()) {
				comm.sendLine(Constant.MSG_PROTOCOL_HEADER_COOKIE + user.getCookieId() + Constant.MSG_PROTOCOL_HEADER_COOKIE_TAIL);
			}
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CONTENTTYPE + contentType);
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH + contentLength);

			// Om "MIME-Sniffing" te voorkomen
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_NOSNIFF);

			// Om "Clickjacking" te voorkomen
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CLICKJACK);

			comm.sendLine(Constant.EMPTY_STR);

			lastSentStatusCode = statusCode;

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void redirectUrl(String uri) {
		try {
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_HTTP + Constant.STATUSCODE_307_STR);
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_LOCATION + uri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			if (lines.get(i).startsWith(Constant.MSG_PROTOCOL_REQUEST_HEADER_COOKIE)) {
				cookie = lines.get(i).substring(Constant.MSG_PROTOCOL_REQUEST_HEADER_COOKIE.length());
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
