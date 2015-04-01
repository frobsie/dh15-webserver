package nl.tbearfrobsie.dh15.webserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import nl.tbearfrobsie.dh15.webserver.util.Constant;
import nl.tbearfrobsie.dh15.webserver.util.Logger;

public class Request {
	private String uri;
	private String method = "";
	private String protocol;
	private int contentLength = 0;
	private String userCookieId = "";
	private HashMap<String, String> get = new HashMap<String, String>();
	private HashMap<String, String> post = new HashMap<String, String>();
	private boolean valid = false;
	private ArrayList<String> headers;
	
	private static final HashSet<String> METHODS = new HashSet<String>(Arrays.asList(
		     new String[] {Constant.MSG_PROTOCOL_GET, Constant.MSG_PROTOCOL_POST}
		));
	
	public Request(Communication comm) throws IOException, InvalidRequestException, ForbiddenRequestException {
		headers = comm.readTillEmptyLine();
		Iterator<String> headerItr = headers.iterator();
		if(!headerItr.hasNext()) {
			return;
		}
		String line;
		//Extract Method and Uri
		line = headerItr.next();
		Logger.printLine(line, 2);

		// split the input string by whitespace
		String[] lineSplit = line.split(" ");
		
		if(lineSplit.length < 3) {
			throw new InvalidRequestException();
		}
		
		setMethod(lineSplit[0]);
		setUri(lineSplit[1]);
		this.protocol = lineSplit[2];
		
		
		while(headerItr.hasNext()) {
			line = headerItr.next();
			if (line.startsWith(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH)) {
				this.contentLength = Integer.parseInt(line.substring(Constant.MSG_PROTOCOL_HEADER_CONTENTLENGTH.length()));
			}
			
			if (line.startsWith(Constant.MSG_PROTOCOL_REQUEST_HEADER_COOKIE)) {
				this.userCookieId = line.substring(Constant.MSG_PROTOCOL_REQUEST_HEADER_COOKIE.length());
			}
		}
		
		
		
		if(getMethod().equals(Constant.MSG_PROTOCOL_POST)) {
			post = comm.getPost(contentLength);
		}
		valid = true;
		
	}
	
	private void setMethod(String method) throws InvalidRequestException {
		if(METHODS.contains(method)) {
			this.method = method;
		} else {
			throw new InvalidRequestException();
		}
	}
	
	private void setUri(String uri) throws ForbiddenRequestException {
		// if ask for root get default page
		if(uri.equals(Constant.MSG_URI_ROOT) && !ConfigPropertyValues.isDirectoryBrowsingAllowed()) {
			uri = ConfigPropertyValues.getDefaultpageUri();
		}
		
		// split the input string by ?
		String[] uriSplit = uri.split("\\?");
		this.uri = uriSplit[0];
		
		if(uriSplit.length > 1) {
			this.processGetParams(uriSplit);
		}
		
		// check if path contains ../ (can cause directory scanning issues)
		if(getPath().contains(Constant.MSG_URI_PARENTFOLDER)) {
			throw new ForbiddenRequestException();
		}
	}
	
	private void processGetParams(String[] uriSplit) {
		String getParams = "";
		//make new string with all getparams
		for(int i = 1; i < uriSplit.length; i++) {
			getParams += uriSplit[i];
		}
		
		String[] paramsSplit = getParams.split("\\&");
		for(int i = 0; i< paramsSplit.length; i++) {
			String[] paramSplit = paramsSplit[i].split("="); 
			if(paramSplit.length == 1) {
				this.get.put(paramSplit[0], "");
			} else if (paramSplit.length == 2) {
				this.get.put(paramSplit[0], paramSplit[1]);
			}
		}
		
		Logger.printLine(get.toString(), 1);
	}
	
	public String getUri() {
		return this.uri;
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public String getProtocol() {
		return this.protocol;
	}
	
	public int getContentLenght() {
		return this.contentLength;
	}
	
	public boolean isValid() {
		return this.valid;
	}
	
	public String getUserCookieId() {
		return this.userCookieId;
	}
	
	public String getPath() {
		return ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_DOCROOT) + uri;
	}
	
	public String getPostValue(String key) {
		return post.get(key);
	}
	
	public HashMap<String, String> getPost() {
		return post;
	}
	
	public boolean postContains(String key) {
		return post.containsKey(key);
	}
	
	public String getGetValue(String key) {
		return get.get(key);
	}
	
	public HashMap<String, String> getGet() {
		return get;
	}
	
	public boolean getContains(String key) {
		return get.containsKey(key);
	}
	
	public String getLogLine() {
		return method + " " + uri + " " + protocol;
	}
	
	public ArrayList<String> getRawHeaders() {
		return headers;
	}	
}
