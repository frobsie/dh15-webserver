import sun.security.krb5.Config;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

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

                createSSLServerSocket();
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

    private void createSSLServerSocket() {
        try{
            int port = new Integer(ConfigPropertyValues.get("port"));
            String STORETYPE 		= (String) ConfigPropertyValues.get("cert.storetype");
            String KEYSTORE 		= (String) ConfigPropertyValues.get("cert.keystore");
            String STOREPASSWORD 	= (String) ConfigPropertyValues.get("cert.storepassword");
            String KEYPASSWORD 		= (String) ConfigPropertyValues.get("cert.keypassword");

            KeyStore ks = KeyStore.getInstance( STORETYPE );
            File kf = new File( KEYSTORE );
            ks.load( new FileInputStream( kf ), STOREPASSWORD.toCharArray() );

            KeyManagerFactory kmf = KeyManagerFactory.getInstance( "SunX509" );
            kmf.init( ks, KEYPASSWORD.toCharArray() );
            TrustManagerFactory tmf = TrustManagerFactory.getInstance( "SunX509" );
            tmf.init( ks );

            SSLContext sslContext = null;
            sslContext = SSLContext.getInstance( "TLS" );
            sslContext.init( kmf.getKeyManagers(), tmf.getTrustManagers(), null );
            SSLServerSocketFactory socketFactory = sslContext.getServerSocketFactory();

            serverSocket = socketFactory.createServerSocket(port);
        } catch( Exception e ) {
            e.printStackTrace();
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