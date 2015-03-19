package nl.tbearfrobsie.dh15.webserver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.SSLException;

public class ClientThread implements Runnable {
	
	public static Integer bufferSize = 128;
	public static String SERVER_OPENEDSOCKET = "Socket opened: ";
	public static String SERVER_CLOSINGSOCKET = "Closing socket: ";

	public static String ERROR_CLIENT_CLOSED_CONNECTION = "Error : Client closed connection.";
	public static String ERROR_CLIENT_FILENOTEXISTS = "File does not exist.";

    public static String ADMIN_URI = "/adminerino";

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

	/**
	 * Setup worker thread with given socket.
	 * 
	 * @param socket
	 * @param fileServer
	 */
	public ClientThread(Socket socket, FileServer fileServer) {
		this.socket = socket;
        this.fileServer = fileServer;
        
        directoryBrowsingAllowed = Boolean.valueOf(ConfigPropertyValues.get("directorybrowsing"));
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
		} catch(SSLException e) {
            //TODO fix isn't working at all
            sendResponseHeader(301, 0);
            try {
                sendLine("Location: https://127.0.0.1:8021");
            } catch(IOException ex) {
                ex.printStackTrace();
            }
            return;
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
        if(method.equals("GET")) {
            handleGet(uri, protocol);
        } else if (method.equals("POST")) {
            Integer contentLength = 0;
            final String contentHeader = "Content-Length: ";
            for(int i = 0; i < lines.size(); i++) {
            	if (lines.get(i).startsWith(contentHeader)) {
                    contentLength = Integer.parseInt(lines.get(i).substring(contentHeader.length()));
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
    	String retVal = ""; 
    	
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
        if(uri.equals("/") && !directoryBrowsingAllowed) {
            uri = getDefaultpageUri();
        }

        String fullPath = ConfigPropertyValues.get("docroot") + uri;
       
        // check if fullpath contains ../ (can cause directory scanning issues)
        if(fullPath.contains("../")) {
        	plot403();
        	return;
        }

        printLine(fullPath, 0);
		File file = new File(fullPath);
		
		printLine(fullPath, 3);
		printLine(uri, 1);
		
		// Als we directories mogen listen
		// en de opgevraagde resource is een map
        if (directoryBrowsingAllowed && isDirectory(file)) {        	
        	String folderContent = listFolderContent(uri);
        	sendResponseHeader(200, folderContent.length());
        	
            sendLine(folderContent);
        	return;
        }

        // als het een file is
		if (fileExists(file)) {	        
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
            String form = getManageForm();
            sendResponseHeader(200, form.length());

            sendLine(form);
            return;
        }
        
		// Als de opgevraagde resource niet bestaat
		// dan 404 tonen
		plot404();
        printLine(ERROR_CLIENT_FILENOTEXISTS, 3);
	}

	/**
	 * Collect the default page from the config and check if one of the options exists.
	 * @return String 
	 */
    protected String getDefaultpageUri() {
        String[] lineSplit = ConfigPropertyValues.get("defaultpage").split(";");
        for(int i = 0; i < lineSplit.length; i++) {
            File file = new File(ConfigPropertyValues.get("docroot") + "/" + lineSplit[i]);
            if(file.exists()) {
                return "/" + lineSplit[i];
            }
        }
        return "/" + lineSplit[0];
    }
	
    /**
     * send 404 page to the user.
     * @throws IOException
     */
	protected void plot404() throws IOException {
		String fullPath =  ConfigPropertyValues.get("statusroot") + "/" + ConfigPropertyValues.get("errorpage");

        FileResource errorFile = new FileResource(fullPath);
        sendResponseHeader(404, errorFile.getByteSize(), getContentType(errorFile));
		sendFile(errorFile);
	}
	
	/**
     * send 403 page to the user.
     * @throws IOException
     */
	protected void plot403() throws IOException {
		String fullPath =  ConfigPropertyValues.get("statusroot") + "/" + ConfigPropertyValues.get("forbiddenpage");

        FileResource errorFile = new FileResource(fullPath);
        sendResponseHeader(403, errorFile.getByteSize(), getContentType(errorFile));
		sendFile(errorFile);
	}
	
	/**
     * send 400 page to the user.
     * @throws IOException
     */
	protected void plot400() throws IOException {
		String fullPath =  ConfigPropertyValues.get("statusroot") + "/" + ConfigPropertyValues.get("badrequestpage");

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
            String[] rawPost = (new String(data)).split("&");
            for(int i = 0; i < rawPost.length; i++) {
                String[] split = rawPost[i].split("=");
                String value = null;
                // no value given
                if(split.length == 2) {
                    value = split[1];
                }
                post.put(split[0], value);
            }
        }

        if(uri.equals(ADMIN_URI)) {
            printLine(post.toString(),2);
            // UGLY :D
            if(post.containsKey("port")) {
                ConfigPropertyValues.set("port", new Integer(post.get("port")).toString());
            }
            if(post.containsKey("docroot")) {
                ConfigPropertyValues.set("docroot", post.get("docroot"));
            }
            if(post.containsKey("defaultpage")) {
                ConfigPropertyValues.set("defaultpage", post.get("defaultpage"));
            }
            if(post.containsKey("directorybrowsing")) {
                ConfigPropertyValues.set("directorybrowsing", "true");
            } else {
                ConfigPropertyValues.set("directorybrowsing", "false");
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
        sendResponseHeader(statusCode, contentLength, "text/html");
        
    }

    /**
     * send response headers to the client
     * @param statusCode
     * @param contentLength
     * @param contentType
     */
    private void sendResponseHeader(int statusCode, int contentLength, String contentType) {
        String status = null;
        // TODO
        // Deze als static constant definen ergens
        switch(statusCode) {
            case 200:
                status = "200 Ok";
                break;
            case 203:
                status = "203 No Content";
                break;
            case 301:
                status = "301 Moved Permanently";
                break;
            case 400:
            	status = "400 Bad Request";
            	break;
            case 403:
                status = "403 Forbidden";
                break;    
            case 404:
                status = "404 Not Found";
                break;
        }
        try {
            sendLine("HTTP/1.0 "+status);
            sendLine("Content-Type: "+contentType);
            sendLine("Content-Length: "+contentLength);
            
            // Om "MIME-Sniffing" te voorkomen
            sendLine("X-Content-Type-Options: nosniff");
            
            // Om "Clickjacking" te voorkomen
            sendLine("X-Frame-Options: deny");
            
            sendLine("");
            
            lastSentStatusCode = statusCode;
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if file exists and is not a directory
     * @param file
     * @return
     */
	private Boolean fileExists(File file) {
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Check if directory exists
	 * @param file
	 * @return
	 */
	private Boolean isDirectory(File file) {
		if (file.exists() && file.isDirectory()) {
			return true;
		}
		return false;
	}

	/**
	 * get the socket information formatted
	 * @return
	 */
	protected String getSocketInfo() {
		String retVal = socket.getInetAddress() + ":" + socket.getPort();
		return retVal.replace("/", "");
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
		BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
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
	 * Build and send directory structure to client for the given uri
	 * @param uri
	 * @return
	 */
	protected String listFolderContent(String uri) {
		String path = ConfigPropertyValues.get("docroot") + "/" + uri;
		File f = new File(path);
		List<File> list = Arrays.asList(f.listFiles());
		Iterator<File> fileIterator = list.iterator();
		
		String fileListingHtml = "<h1>Index of " + uri + "</h1>";
		fileListingHtml += "<table>";
		fileListingHtml += "<thead style=\"text-align:left;\">";
		fileListingHtml += "<tr>";
		fileListingHtml += "<th style=\"padding:10px\">filename</th>";
		fileListingHtml += "<th style=\"padding:10px\">lastmodified</th>";
		fileListingHtml += "<th style=\"padding:10px\">size</th>";
		fileListingHtml += "</tr>";
		fileListingHtml += "</thead>";
		fileListingHtml += "<tbody>";
		
		if (!uri.equals("/")) {
			// Parent uri opbouwen
			ArrayList<String> uriParts = new ArrayList<String>(Arrays.asList(uri.split("/")));
			
			if ((uriParts.size() -1) > 0) {
				uriParts.remove(uriParts.size()-1);
			}
			
			Iterator<String> uriIterator = uriParts.iterator();
			String newUri = "/";
			
			while(uriIterator.hasNext()) {
				String uriPart = uriIterator.next();
				
				if (!uriPart.equals("")) {
					newUri += uriPart + "/";	
				}
			}			
					
			fileListingHtml += "<tr>";
			fileListingHtml += "<td style=\"padding:10px\" colspan=\"3\"><a href=\""+newUri+"\">Parent Directory</a></td>";
			fileListingHtml += "</tr>";
		}
		
		while(fileIterator.hasNext()) {
			File file = fileIterator.next();
			String filePath = uri;
			
			if (uri.equals("/")) {
				filePath = uri + file.getName();
			} else {
				filePath = uri + "/" +file.getName();
			}
					
			// Date formatter voor de lastmodified date van de file
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			
			fileListingHtml += "<tr>";
			fileListingHtml += "<td style=\"padding:10px\"><a href=\""+ filePath +"\">"+file.getName()+"</a></td>";
			fileListingHtml += "<td style=\"padding:10px\">"+sdf.format(file.lastModified())+"</td>";
			fileListingHtml += "<td style=\"padding:10px\">"+file.length()+"</td>";
			fileListingHtml += "</tr>";
		}
		
		fileListingHtml += "</tbody>";
		fileListingHtml += "</table>";
		
		return fileListingHtml;
	}

	/**
	 * Print a formatted debug line to the console
	 * @param message
	 * @param sendType
	 */
    protected void printLine(String message, Integer sendType) {
        String fm = "";

        switch (sendType) {
            default:
            case 0:
                fm += "[SERVER] ";
                break;
            case 1:
                fm += "[SEND]   ";
                break;
            case 2:
                fm += "[RECV]   ";
                break;
            case 3:
                fm += "[ERROR]  ";
                break;
        }

        fm += "[" + getSocketInfo() + "] ";
        fm += message;

        System.out.println(fm);
    }

    /**
     * Get the form the manage the server configuration
     * @return
     */
    protected String getManageForm(){
        String admin =  "<form method=\"post\" action=\"" +
                ADMIN_URI +
                "\">\n" +
                "<table>\n" +
                "<thead>\n" +
                "    <tr><th>SuperServer</th><th class=\"right\">Control Panel</th></tr>\n" +
                "</thead>\n" +
                "<tbody>\n" +
                "    <tr><td>Web port:</td><td><input name=\"port\" value=\"" +
                ""+ ConfigPropertyValues.get("port")+"" +
                "\" type=\"text\"></td></tr>\n" +
                "    <tr><td>Webroot:</td><td><input name=\"docroot\" value=\"" +
                ""+ ConfigPropertyValues.get("docroot")+"" +
                "\" type=\"text\"></td></tr>\n" +
                "    <tr><td>Default page:</td><td><input name=\"defaultpage\" value=\"" +
                ""+ ConfigPropertyValues.get("defaultpage")+"" +
                "\" type=\"text\"></td></tr>\n" +
                "    <tr><td>Directory browsing</td><td><input name=\"directorybrowsing\" type=\"checkbox\"";
                if(ConfigPropertyValues.get("directorybrowsing").equals("true")) {
                    admin += " checked=\"checked\"";
                }

                admin += "></td></tr>\n" +
                "    <tr><td><input value=\"Show Log\" type=\"button\"></td>\n" +
                "        <td class=\"right\"><input value=\"OK\" type=\"submit\"></td>\n" +
                "    </tr>\n" +
                "</tbody>\n" +
                "</table>\n" +
                "</form>";
        return admin;
    }

    /**
     * Get content type for given file resource.
     * @param file
     * @return
     */
    private String getContentType(FileResource file) {
        return URLConnection.guessContentTypeFromName(file.getName());
    }
}
