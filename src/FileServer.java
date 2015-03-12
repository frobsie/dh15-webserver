package nl.frobsie.dh14.networking.les3.fileserver;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
	
	public static String SERVER_STARTED = "Server started on port: ";
	public static String SERVER_CLIENT_ACCEPTED = "Accepted connection : ";
	public static String SERVER_CLIENT_DISCONNECTED = "Client disconnected : ";
	public static String SERVER_ERROR_COULDNOTSTART = "Could not start server.";
	
	public static int PORT = 2000;
	
	/** ServerSocket */
	private ServerSocket serverSocket;
	
	/** Is the server running? */
	private boolean isRunning = true;
	
	/**
	 * Starts the fileserver
	 * 
	 * @throws IOException
	 */
	public void startServer() throws IOException
	{
		// setup serversocket with port
		serverSocket = new ServerSocket(PORT);
		
		while(isRunning) {
			// accept connections
			Socket clientSocket = serverSocket.accept();
		
			// start new thread for incoming connection
			Thread t = new Thread(new ClientThread(clientSocket));
            t.start();
		}
		
		serverSocket.close();
	}
	
	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
		// init file server
		FileServer fileServer = new FileServer();
		
		try {
			// start file server
			fileServer.startServer();
		} catch (Exception e) {
			System.out.println(SERVER_ERROR_COULDNOTSTART);
			System.exit(0);
		}
	}
}