package nl.tbearfrobsie.dh15.webserver;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class HTTPHandler {

	private Communication comm;

	private User user;

	private Request request;
	
	private Response response;

	public HTTPHandler(Communication comm) {
		this.comm = comm;

		user = new User();
	}

	/**
	 * Handle request response
	 * @throws IOException
	 * @throws Exception
	 */
	public void handle() throws IOException, Exception {
		try{
			request = new Request(comm);
			response = new Response(this.comm, this.user, this);
			loginUser();

			handleRequestMethod();
			
		} catch(InvalidRequestException e) {
			response.plot400();
		} catch(ForbiddenRequestException e) {
			response.plot403();
		} catch(IOException e) {
			e.printStackTrace();
		}
		this.comm.close();
	}

	/**
	 * Handles user input
	 *
	 * @param lines
	 * @throws IOException
	 */
	protected void handleRequestMethod() throws IOException, Exception {
		switch(request.getMethod()) {
		case Constant.MSG_PROTOCOL_GET:
			handleGet();
			break;
		case Constant.MSG_PROTOCOL_POST:
			handlePost();
			break;
		default:
			response.plot400();
			break;
		}		

		Logger.append(Logger.LOG_TYPE_ACCESS, createAccessLogLine());
		Logger.append(Logger.LOG_TYPE_ACCESS_EXTENDED, createAccessLogExtendedLine());
	}

	/**
	 * Temporary GET handler
	 * 
	 * @param uri
	 * @param protocol
	 * @throws IOException
	 */
	private void handleGet() throws IOException {		

		Logger.printLine(request.getPath(), 0);
		File file = new File(request.getPath());

		// Als we directories mogen listen
		// en de opgevraagde resource is een map
		if (ConfigPropertyValues.isDirectoryBrowsingAllowed() && file.isDirectory()) {        	
			String folderContent = HtmlProvider.listFolderContent(request.getUri()); 
			response.sendResponseHeader(200, folderContent.length());

			comm.sendLine(folderContent);
			return;
		}

		// als het een file is
		if (file.isFile()) {	        
			// Print Headers
			FileResource fr = new FileResource(request.getPath());
			response.sendResponseHeader(200, fr.getByteSize(), response.getContentType(fr));

			comm.sendFile(fr);
			return;
		}

		switch(request.getUri()) {
		case Constant.ADMIN_URI:
			this.showAdmin();
			break;
		case Constant.SETTINGS_URI:
			this.showSettingsForm();
			break;
		case Constant.URI_LOGOUT:
			this.handleLogout();
			break;
		case Constant.URI_SHOWLOG:
			this.showLog();
			break;
		case Constant.URI_CLEARLOGS:
			this.clearLogs();
			break;
		case Constant.URI_USERS:
			this.showUsers();
			break;
		case Constant.URI_USER_NEW:
			this.showNewUserForm();
			break;
		case Constant.URI_USER_DELETE:
			this.deleteUser();
			break;
		default:
			// Als de opgevraagde resource niet bestaat
			// dan 404 tonen
			response.plot404();
			Logger.printLine(Constant.ERROR_CLIENT_FILENOTEXISTS, 3);
			break;
		}
	}

	/**
	 * Handle POST requests 
	 * @param uri
	 * @param protocol
	 * @param contentLength
	 * @throws IOException
	 * @throws Exception
	 */
	protected void handlePost() throws IOException, Exception, UpdatedConfigException {
		Logger.printLine(request.getPost().toString(),2);
		switch(request.getUri()) {
		case Constant.ADMIN_URI:
			this.processLoginUser();
			break;
		case Constant.SETTINGS_URI:
			this.processConfigUpdate();
			break;
		case Constant.URI_USER_NEW:
			this.processNewUser();
			break;
		default:
			response.plot404();
			break;
		}
	}
	
	/**
	 * Show admin form 
	 * @throws IOException
	 */
	private void showAdmin() throws IOException {
		if(user.isLoggedIn()) {
			response.redirectUrl(Constant.SETTINGS_URI);
			return;
		}
		String form = HtmlProvider.getLoginForm();
		response.sendResponseHeader(200, form.length());

		comm.sendLine(form);
	}
	
	private void showSettingsForm() throws IOException {
		if(!user.isLoggedIn()) {
			response.plot403();
			return;
		}
		
		String form = HtmlProvider.getManageForm(user);
		response.sendResponseHeader(200, form.length());

		comm.sendLine(form);
		
	}
	
	/**
	 * Commit the logout action
	 */
	private void handleLogout() {
		user.destroyCookieId();

		MySQLAccess msa = new MySQLAccess();
		msa.storeUser(user);
		msa.close();
		user = new User();

		response.redirectUrl(Constant.ADMIN_URI);
	}
	
	/**
	 * Show log file
	 * @throws IOException
	 */
	private void showLog() throws IOException {
		if(!user.isLoggedIn()) {
			response.plot403();
			return;
		}
		String log = HtmlProvider.getLog();
		response.sendResponseHeader(200, log.length());
		comm.sendLine(log);
	}
	
	/**
	 * Clear logs
	 * @throws IOException 
	 */
	private void clearLogs() throws IOException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		}
		String cleared = Constant.MSG_LOGS_CLEARED;
		Logger.clearLogFiles();
		response.sendResponseHeader(200, cleared.length());
		comm.sendLine(cleared);
	}
	
	/**
	 * Show list of users
	 * @throws IOException 
	 */
	private void showUsers() throws IOException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		}
		String users = HtmlProvider.listUserContent();
		response.sendResponseHeader(200, users.length());
		comm.sendLine(users);
	}
	
	/**
	 * Show new user form
	 * @throws IOException
	 */
	private void showNewUserForm() throws IOException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		}
		String userForm = HtmlProvider.getNewUserForm();
		response.sendResponseHeader(200, userForm.length());
		comm.sendLine(userForm);
	}
	
	/**
	 * Delete user 
	 * @throws IOException
	 */
	private void deleteUser() throws IOException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		} else if (!request.getContains(Constant.GET_ID)) {
			response.plot400();
		}
		int id = Integer.parseInt(request.getGetValue(Constant.GET_ID));

		MySQLAccess msa1 = new MySQLAccess();
		msa1.deleteUser(id);
		msa1.close();
		response.redirectUrl(Constant.URI_USERS);
	}

	private void processConfigUpdate() throws IOException, UpdatedConfigException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		}
		
		boolean isPortChanged = false;
		
		if(request.postContains(ConfigPropertyValues.CONFIG_KEY_PORT)) {
			if(!ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_PORT).equals(request.getPostValue(ConfigPropertyValues.CONFIG_KEY_PORT))) {
				isPortChanged = true;
			}
			ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_PORT, 
					new Integer(request.getPostValue(ConfigPropertyValues.CONFIG_KEY_PORT)).toString());
		}
		if(request.postContains(ConfigPropertyValues.CONFIG_KEY_DOCROOT)) {
			ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DOCROOT, 
					request.getPostValue(ConfigPropertyValues.CONFIG_KEY_DOCROOT));
		}
		if(request.postContains(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE)) {
			ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE, 
					request.getPostValue(ConfigPropertyValues.CONFIG_KEY_DEFAULTPAGE));
		}
		if(request.postContains(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING)) {
			ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING, 
					ConfigPropertyValues.CONFIG_VALUE_STR_TRUE);
		} else {
			ConfigPropertyValues.set(ConfigPropertyValues.CONFIG_KEY_DIRECTORYBROWSING, 
					ConfigPropertyValues.CONFIG_VALUE_STR_FALSE);
		}
		ConfigPropertyValues.write();
		
		
		if(isPortChanged) {
			response.redirectUrl(Constant.LOCALHOST+Constant.URI_SPLIT_PORT+ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_PORT)+Constant.SETTINGS_URI);
			throw new UpdatedConfigException();
		} else {
			response.redirectUrl(Constant.SETTINGS_URI);
		}
	}

	private void processLoginUser() throws IOException {
		if(request.postContains(Constant.POST_USERNAME) && request.postContains(Constant.POST_PASSWORD)) {
			MySQLAccess msa = new MySQLAccess();
			User user = msa.readUser(request.getPostValue(Constant.POST_USERNAME));
			if(null == user) {
				response.plot403();
				return;
			} 

			if(!user.checkPassword(request.getPostValue(Constant.POST_PASSWORD))) {
				response.plot403();
				return;
			}
			user.setLoggedIn(true);
			user.generateCookieId();

			msa.storeUser(user);
			setUser(user);
			msa.close();
			response.redirectUrl(Constant.SETTINGS_URI);
		}
	}

	private void processNewUser() throws IOException {
		if(!user.isLoggedIn() || !user.hasRole(User.ROLE_BEHEERDERS)) {
			response.plot403();
			return;
		}
		
		if(request.postContains(Constant.POST_USERNAME) && request.postContains(Constant.POST_PASSWORD) && request.postContains(Constant.POST_ROLE)) {
			User user = new User(request.getPostValue(Constant.POST_USERNAME), request.getPostValue(Constant.POST_PASSWORD), request.getPostValue(Constant.POST_ROLE));

			MySQLAccess msa = new MySQLAccess();
			msa.createUser(user);
			msa.close();
			response.redirectUrl(Constant.URI_USERS);
			return;
		} else {
			response.redirectUrl(Constant.URI_USER_NEW);
			return;
		}
	}

	private void loginUser() {
		if(request.getUserCookieId().equals("")) {
			return;
		}

		MySQLAccess msa = new MySQLAccess();
		User user = msa.readUserByCookie(request.getUserCookieId());
		if(null == user) {
			return;
		} 
		user.setLoggedIn(true);

		//Regenerate token
		//user.generateCookieId();

		//msa.storeUser(user);
		setUser(user);
		msa.close();
	}


	protected String createAccessLogLine() {
		String retVal = String.format("%s " + "\"" + request.getLogLine() + "\"" + " %s", comm.getSocketInfo(), response.lastSentStatusCode);
		return retVal;
	}

	protected String createAccessLogExtendedLine() {
		String retVal = Constant.EMPTY_STR; 

		Iterator<String> it = request.getRawHeaders().iterator();
		while(it.hasNext()) {
			String line = it.next();
			retVal += line + System.lineSeparator();
		}

		retVal = String.format("%s %s " + "\"" + retVal + "\"", comm.getSocketInfo(), response.lastSentStatusCode);
		retVal += System.lineSeparator();

		return retVal;
	}
	
	protected void setUser(User user) {
		this.user = user;
		this.response.setUser(user);
	}
}
