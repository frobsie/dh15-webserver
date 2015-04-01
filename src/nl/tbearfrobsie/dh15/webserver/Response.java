package nl.tbearfrobsie.dh15.webserver;

import java.io.IOException;

import nl.tbearfrobsie.dh15.webserver.util.Constant;

public class Response {
	private Communication comm;
	private User user;
	
	/** The last statuscode sent to a client */
	public Integer lastSentStatusCode = -1;
	
	public Response(Communication comm, User user, HTTPHandler httpHandler) {
		this.comm = comm;
		this.user = user;
	}
	
	/**
	 * send 404 page to the user.
	 * 
	 * @throws IOException
	 */
	public void plot404() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_ERRORPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(404, errorFile.getByteSize(), errorFile.getContentType());
		comm.sendFile(errorFile);
	}

	/**
	 * send 403 page to the user.
	 * 
	 * @throws IOException
	 */
	public void plot403() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_FORBIDDENPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(403, errorFile.getByteSize(), errorFile.getContentType());
		comm.sendFile(errorFile);
	}

	/**
	 * send 400 page to the user.
	 * 
	 * @throws IOException
	 */
	public void plot400() throws IOException {
		String fullPath =  ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_STATUSROOT) + Constant.URI_DELIMITER + ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_BADREQUESTPAGE);

		FileResource errorFile = new FileResource(fullPath);
		sendResponseHeader(400, errorFile.getByteSize(), errorFile.getContentType());
		comm.sendFile(errorFile);
	}
	
	/**
	 * send response headers to the client
	 * 
	 * @param statusCode
	 * @param contentLength
	 * @return void
	 */
	public void sendResponseHeader(int statusCode, int contentLength){
		sendResponseHeader(statusCode, contentLength, Constant.MSG_PROTOCOL_DEFAULTMIMETYPE);

	}

	/**
	 * send response headers to the client
	 * @param statusCode
	 * @param contentLength
	 * @param contentType
	 * @return void
	 */
	public void sendResponseHeader(int statusCode, int contentLength, String contentType) {
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
			if(null != contentType) {
				comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CONTENTTYPE + contentType);
			}
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH + contentLength);

			// Om "MIME-Sniffing" te voorkomen
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_NOSNIFF);

			// Om "Clickjacking" te voorkomen
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_CLICKJACK);

			comm.sendLine(Constant.EMPTY_STR);

			this.lastSentStatusCode = statusCode;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Redirects a client
	 * 
	 * @param uri
	 * @return void
	 */
	public void redirectUrl(String uri) {
		try {
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_HTTP + Constant.STATUSCODE_302_STR);
			comm.sendLine(Constant.MSG_PROTOCOL_HEADER_LOCATION + uri);
			if(user.isLoggedIn()) {
				comm.sendLine(Constant.MSG_PROTOCOL_HEADER_COOKIE + user.getCookieId() + Constant.MSG_PROTOCOL_HEADER_COOKIE_TAIL);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
}
