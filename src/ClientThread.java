import sun.security.krb5.Config;

import javax.net.ssl.SSLException;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import java.util.ArrayList;
import java.util.HashMap;

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

	/**
	 * Setup worker thread with given socket.
	 * 
	 * @param socket
	 * @param fileServer
	 */
	public ClientThread(Socket socket, FileServer fileServer) {
		this.socket = socket;
        this.fileServer = fileServer;
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
        } catch(Exception e) {
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
        if (lineSplit.length < 2) {
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
            String contentLengthString = lines.get(lines.size()-1);
            if (contentLengthString.startsWith(contentHeader)) {
                contentLength = Integer.parseInt(contentLengthString.substring(contentHeader.length()));
            }
            handlePost(uri, protocol, contentLength);
        }
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
        if(uri == "/") {
            uri = ConfigPropertyValues.get("defaultpage");
        }

        String fullPath = ConfigPropertyValues.get("docroot") + uri;

        //TODO if fullPath is not permitted return 403
        //TODO implement content-length
        //TODO implement content-type
        //TODO implement http status code

        printLine(fullPath, 0);
        printLine(uri, 1);
		File file = new File(fullPath);

		if (fileExists(file)) {
            // Print Headers
            FileResource fr = new FileResource(fullPath);
            sendResponseHeader(200, fr.getByteSize());

            sendFile(fr);
            return;
		}

        // check if uri equals the admin url
        if(uri.equals(ADMIN_URI)) {
            String form = getManageForm();
            sendResponseHeader(200, form.length());

            //Print double linebreak to tell browser content is starting
            sendLine("");
            sendLine("");
            sendLine(form);
            return;
        }

        // No action to do with this request
        sendResponseHeader(404, 0);
        printLine(ERROR_CLIENT_FILENOTEXISTS, 3);
	}
	
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
                if(post.get("directorybrowsing").equals("on")) {
                    ConfigPropertyValues.set("directorybrowsing", "1");
                } else {
                    ConfigPropertyValues.set("directorybrowsing", "0");
                }
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


    private void sendResponseHeader(int statusCode, int contentLength) {
        String status = null;
        switch(statusCode) {
            case 200:
                status = "200 OK";
                break;
            case 203:
                status = "203 NO CONTENT";
                break;
            case 301:
                status = "301 Moved Permanently";
                break;
            case 404:
                status = "404 NO FOUND";
                break;
        }
        try {
            sendLine("HTTP/1.1 "+status);
            sendLine("Content-Type: text/html");
            sendLine("Content-Length: "+contentLength);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	private Boolean fileExists(File file) {
		if (file.exists() && !file.isDirectory()) {
			return true;
		}
		return false;
	}

	protected String getSocketInfo() {
		String retVal = socket.getInetAddress() + ":" + socket.getPort();
		return retVal.replace("/", "");
	}

	protected void sendLine(String message) throws IOException {
		sendLine(message, false);
	}

	protected void sendLine(String message, boolean toOut) throws IOException {
		printWriter.println(message);

		if (toOut) {
			printLine(message, 1);
		}
	}

	/**
	 * Sends a file to client using DataOutputStreams
	 * 
	 * @param fr
	 * @throws IOException
	 */
	protected void sendFile(FileResource fr) throws IOException {
        //Print double linebreak to tell browser content is starting
        sendLine("");
        sendLine("");

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

	protected void listFolderContent() {
		// TODO
	}

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
                if(ConfigPropertyValues.get("directorybrowsing").equals("1")) {
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
}
