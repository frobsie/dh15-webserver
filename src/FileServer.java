import sun.security.krb5.Config;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FileServer {
	
	public static String SERVER_STARTED = "Server started on port: ";
	public static String SERVER_CLIENT_ACCEPTED = "Accepted connection : ";
	public static String SERVER_CLIENT_DISCONNECTED = "Client disconnected : ";
	public static String SERVER_ERROR_COULDNOTSTART = "Could not start server.";
	public static String SERVER_ERROR_COULDNOTREADCONFIG = "Could not read config.";
    public static String SERVER_ERROR_EXCEPTION = "EXCEPTION THROWN IN THREAD";
	
	/** ServerSocket */
	private ServerSocket serverSocket;
	
	/** Is the server running? */
	public boolean isRunning = true;
	
	/**
	 * Starts the fileserver
	 * 
	 * @throws IOException
	 */
	public void startServer() throws IOException
	{
        // Loop to restart server after exception
		while(true) {
            // setup serversocket with port
            try {
                int port = new Integer(ConfigPropertyValues.get("port"));
                System.out.println(SERVER_STARTED + port);

                serverSocket = new ServerSocket(port);
                while (isRunning) {
                    // accept connections
                    Socket clientSocket = serverSocket.accept();

                    // start new thread for incoming connection
                    Thread t = new Thread(new ClientThread(clientSocket, this));
                    t.start();
                }
                this.closeServer();
            } catch (Exception e) {
                // restart server
            }
        }
	}

    public void closeServer() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	/**
	 * Main
	 * 
	 * @param args
	 */
	public static void main(String[] args)
	{
        // init config
        try {
            ConfigPropertyValues.load();
        } catch (IOException e) {
            System.out.println(SERVER_ERROR_COULDNOTREADCONFIG);
            System.exit(0);
        }
		// init file server
		FileServer fileServer = new FileServer();
		
		try {
			// start file server
			fileServer.startServer();
		} catch (Exception e) {
            e.printStackTrace();
			System.out.println(SERVER_ERROR_COULDNOTSTART);
			System.exit(0);
		}
	}
}