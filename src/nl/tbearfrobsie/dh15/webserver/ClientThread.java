package nl.tbearfrobsie.dh15.webserver;

import java.io.IOException;
import java.net.SocketException;

import nl.tbearfrobsie.dh15.webserver.exception.UpdatedConfigException;
import nl.tbearfrobsie.dh15.webserver.util.Constant;
import nl.tbearfrobsie.dh15.webserver.util.Logger;

public class ClientThread implements Runnable {

	/** The socket on which the client is connected */
	private Communication comm;
	
	/** Reference to the base server class */
	public FileServer fileServer;

	/**
	 * Setup worker thread with given socket.
	 * 
	 * @param socket
	 * @param fileServer
	 */
	public ClientThread(Communication comm, FileServer fileServer) {
		this.comm = comm;
		this.fileServer = fileServer;
	}

	/**
	 * Runnable implementation.
	 * Creates a HTTPHandler with given Communication
	 * object and starts its handlers.
	 * 
	 * @return void
	 */
	@Override
	public void run() {
		try {
			Logger.printLine(Constant.SERVER_OPENEDSOCKET + comm.getSocketInfo(), 0);
		
			HTTPHandler http = new HTTPHandler(comm);
			http.handle();
			Logger.printLine(Constant.SERVER_CLOSINGSOCKET + comm.getSocketInfo(), 0);
	
			this.comm.close();
		} catch (SocketException e) {
			Logger.printLine(Constant.ERROR_CLIENT_CLOSED_CONNECTION, 3);
		} catch(UpdatedConfigException e){
			//Close socket so a new one will start on another port
			try {
				comm.close();
				fileServer.closeServer();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}
}
