package nl.frobsie.dh14.networking.les3.fileserver;

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

public class ClientThread implements Runnable {
	
	public static Integer bufferSize = 128;
	public static String SERVER_OPENEDSOCKET = "Socket opened: ";
	public static String SERVER_CLOSINGSOCKET = "Closing socket: ";
	public static String SERVER_DOCUMENTROOT = "/home/johan/Desktop/dh14";

	public static String ERROR_CLIENT_CLOSED_CONNECTION = "Error : Client closed connection.";
	public static String ERROR_CLIENT_FILENOTEXISTS = "File does not exist.";

	/** The socket on which the client is connected */
	private Socket socket;
	
	private InputStream inputStream;

	/** Converts bytestream to characterstream */
	private InputStreamReader inputStreamReader;

	/** Reader for receiving user input */
	private BufferedReader bufferedReader;

	/** Writer for sending text to the client */
	private PrintWriter printWriter;

	/**
	 * Setup worker thread with given socket.
	 * 
	 * @param socket
	 */
	public ClientThread(Socket socket) {
		this.socket = socket;
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

			String line;

			// process client input
			while ((line = bufferedReader.readLine()) != null) {
				processInput(line);
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
	 * Temporary GET handler
	 * 
	 * @param input
	 * @throws IOException
	 */
	private void handleGet(String filePath) throws IOException {
		String fullPath = SERVER_DOCUMENTROOT + filePath;
		File file = new File(fullPath);

		if (fileExists(file)) {
			sendLine("OK");
			sendFile(fullPath);
		} else {
			printLine(ERROR_CLIENT_FILENOTEXISTS, 3);
		}
	}
	
	protected void handlePost(String fileName) throws IOException, Exception {
		sendLine("OK");
		int totalSize = waitForFileSize();
		//sendLine("Yes");
		receiveFile(totalSize, fileName);
		sendLine("Thx");
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

	/**
	 * Handles user input
	 * 
	 * @param input
	 * @throws IOException
	 */
	protected void processInput(String line) throws IOException, Exception {

		String command, parameter;

		// split the input string by whitespace
		String[] lineSplit = line.split(" ");

		// should have 3 indexes
		if (lineSplit.length < 2) {
			return;
		}

		command = lineSplit[0];
		parameter = lineSplit[1];

		printLine(line, 2);

		switch (command) {
		case "GET":
			handleGet(parameter);
			break;

		case "POST":
			handlePost(parameter);
			break;

		case "Thx":
			// nutteloos
			break;
		}
	}

	protected void sendLine(String message) throws IOException {
		sendLine(message, false);
	}

	protected void sendLine(String message, boolean toOut) throws IOException {
		printWriter.println(message);

		if (!toOut) {
			printLine(message, 1);
		}
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

	/**
	 * Sends a file to client using DataOutputStreams
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	protected void sendFile(String filePath) throws IOException,
			FileNotFoundException, SocketException {

		// load file
		FileResource fr = new FileResource(filePath);

		// setup
		FileInputStream fis = new FileInputStream(fr);
		BufferedInputStream bis = new BufferedInputStream(fis, bufferSize);
		DataInputStream dis = new DataInputStream(bis);
		OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);

		// send filesize to client
		String byteSize = new Integer(fr.getByteSize()).toString();
		sendLine(byteSize, false);

		printLine(filePath, 1);

		// tries to read the complete file into the inputstream
		dis.readFully(fr.getBytes(), 0, fr.getByteSize());

		// tries to write the complete file into the outputstream
		dos.write(fr.getBytes(), 0, fr.getByteSize());

		// output the buffer to the socket
		dos.flush();

		// close the datainputstream
		dis.close();
	}
	
	protected void receiveFile(int totalSize, String fileName) throws IOException {
	    int currentTot = 0;
	    int bytesRead;
	    byte [] bytearray  = new byte [bufferSize];
	    
	    InputStream is = socket.getInputStream();
	    BufferedOutputStream bos;
		bos = getFileOutputStream(SERVER_DOCUMENTROOT+fileName);
    
	    int readLength = bufferSize;
	    while(currentTot < totalSize) {
	       bytesRead = is.read(bytearray, 0, readLength);
	       
	       bos.write(bytearray,  0, bytesRead);
	       bos.flush();
	       
	       bytearray = new byte [bufferSize];
	       currentTot = currentTot + bytesRead;

	       if((currentTot+bufferSize) > totalSize) {
	    	   readLength = totalSize - currentTot;
	       }
	    } 
	}
	
	private BufferedOutputStream getFileOutputStream(String fileName) throws FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(fileName);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    return bos;
	}
	
	private int waitForFileSize() throws Exception {
		String line = bufferedReader.readLine();
		return Integer.parseInt(line);
	}

	protected void listFolderContent() {
		// TODO
	}
}
