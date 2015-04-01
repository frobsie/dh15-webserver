package nl.tbearfrobsie.dh15.webserver;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;

public class FileServer {

	public static String SERVER_STARTED = "Server started on port: ";
	public static String SERVER_CLIENT_ACCEPTED = "Accepted connection : ";
	public static String SERVER_CLIENT_DISCONNECTED = "Client disconnected : ";
	public static String SERVER_ERROR_COULDNOTSTART = "Could not start server.";
	public static String SERVER_ERROR_COULDNOTREADCONFIG = "Could not read config.";
	public static String SERVER_ERROR_EXCEPTION = "EXCEPTION THROWN IN THREAD";

	public static String CERTIFICATE_TYPE = "SunX509";
	public static String CERTIFICATE_CONTEXT = "TLS";

	/** SSLServerSocket */
	private SSLServerSocket serverSocket;

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
				int port = new Integer(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_PORT));
				System.out.println(SERVER_STARTED + port);

				// SSL even uitgezet @ testen
				createSSLServerSocket();
				//serverSocket = new ServerSocket(port);

				while (true) {
					// accept connections
					SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
					//Socket clientSocket = serverSocket.accept();

					// start new thread for incoming connection
					Thread t = new Thread(new ClientThread(new Communication(clientSocket), this));
					t.start();
				}
			} catch (Exception e) {
				// restart server
				e.printStackTrace();
			}
		}
	}

	/**
	 * Tries to create a new SSLServerSocket
	 * by processing the given keystore.
	 * 
	 * @return void
	 */
	private void createSSLServerSocket() {
		try{
			int port = new Integer(ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_PORT));
			String KEYSTORE 		= (String) ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_CERT_KEYSTORE);
			String STOREPASSWORD 	= (String) ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_CERT_STOREPASSWORD);
			String KEYPASSWORD 		= (String) ConfigPropertyValues.get(ConfigPropertyValues.CONFIG_KEY_CERT_KEYPASSWORD);

			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(new FileInputStream(KEYSTORE), KEYPASSWORD.toCharArray());

			KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			kmf.init(ks, STOREPASSWORD.toCharArray());

			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(kmf.getKeyManagers(), null, null);

			SSLServerSocketFactory ssf = sc.getServerSocketFactory();

			//Start de socket
			try {
				this.serverSocket = (SSLServerSocket) ssf.createServerSocket(port);
			} catch (Exception e) {
				throw new Exception("Kan de webserver niet starten op poort " + port + ".");
			}

		} catch( Exception e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Closes the server.
	 * 
	 * @return void
	 */
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
	 * @param String[] args
	 * @return void
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
