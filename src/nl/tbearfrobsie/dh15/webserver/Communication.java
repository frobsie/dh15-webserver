package nl.tbearfrobsie.dh15.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import nl.tbearfrobsie.dh15.webserver.util.Constant;
import nl.tbearfrobsie.dh15.webserver.util.Logger;

public class Communication {

	/** Base socket on which this class operates */ 
	private Socket socket; 
	
	/** Is the socket initialized */
	private boolean isInitialized = false;
	
	/** Inputstream bound to the above socket */
	private InputStream inputStream;

	/** Converts bytestream to characterstream */
	private InputStreamReader inputStreamReader;

	/** Reader for receiving user input */
	private BufferedReader bufferedReader;

	/** Writer for sending text to the client */
	private PrintWriter printWriter;

	/**
	 * Constructor.
	 * 
	 * @param socket
	 * @throws IOException
	 */
	public Communication(Socket socket) throws IOException {
		this.socket = socket;
		initialize();
	}
	
	/**
	 * Retrieves the inputstream from the socket
	 * and sets up the various readers and writers.
	 * 
	 * @throws IOException
	 */
	public void initialize() throws IOException {
		if(!isInitialized){
			inputStream = socket.getInputStream();
			inputStreamReader = new InputStreamReader(inputStream);
			bufferedReader = new BufferedReader(inputStreamReader);
			printWriter = new PrintWriter(socket.getOutputStream(), true);
		}
	}
	
	/**
	 * Read socket till an blank line is found
	 * There is a blank line between the headers and the content
	 * 
	 * @return ArrayList<String>
	 * @throws IOException
	 */
	public ArrayList<String> readTillEmptyLine() throws IOException {
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
	 * Returns formatted socket information.
	 * 
	 * @return String
	 */
	public String getSocketInfo() {
		String retVal = socket.getInetAddress() + ":" + socket.getPort();
		return retVal.replace(Constant.URI_DELIMITER, Constant.EMPTY_STR);
	}

	/**
	 * Send line to client
	 * 
	 * @param String message
	 * @throws IOException
	 */
	public void sendLine(String message) throws IOException {
		sendLine(message, false);
	}

	/**
	 * Send line to client
	 * 
	 * @param String message
	 * @param boolean toOut
	 * @throws IOException
	 * @return void
	 */
	public void sendLine(String message, boolean toOut) throws IOException {
		printWriter.println(message);

		if (toOut) {
			Logger.printLine(message, 1);
		}
	}
	
	/**
	 * Sends a file to client using DataOutputStreams
	 * 
	 * @param FileResource fr
	 * @throws IOException
	 * @return void
	 */
	public void sendFile(FileResource fr) throws IOException {
		// setup
		FileInputStream fis = new FileInputStream(fr);
		BufferedInputStream bis = new BufferedInputStream(fis, Constant.BUFFERSIZE);
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
	 * Retrieves a POST request from the 
	 * buffer.
	 * 
	 * @param int contentLength
	 * @return HashMap<String, String>
	 * @throws IOException
	 */
	public HashMap<String, String> getPost(int contentLength) throws IOException {
		HashMap<String, String> post = new HashMap<String, String>();
		if(contentLength > 0) {
			char[] data = new char[contentLength];
			bufferedReader.read(data);
			String[] rawPost = (new String(data)).split(Constant.URI_SPLIT_DELIMITER);
			for(int i = 0; i < rawPost.length; i++) {
				String[] split = rawPost[i].split(Constant.URI_SPLIT_DELIMITER_VALUE);
				String value = null;
				// no value given
				if(split.length == 2) {
					value = split[1];
				}
				post.put(split[0], value);
			}
		}
		return post;
	}
	
	/**
	 * Closes the inputstreamreader, the bufferedreader
	 * and eventually the socket.
	 * 
	 * @return void
	 * @throws IOException
	 */
	public void close() throws IOException {
		// close when there is no more input
		inputStreamReader.close();
		bufferedReader.close();
		socket.close();
	}
	
	public String getSocketIP() {
		return socket.getInetAddress().toString();
	}
}
