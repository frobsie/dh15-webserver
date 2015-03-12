package nl.frobsie.dh14.networking.les3.fileserver;

import java.io.*;
import java.net.*;

public class FileClient {
	
	public static final String filePath = "/home/johan/Desktop/dh14";
	
	public final String hostname = "127.0.0.1";
	public final int port = 2000;
	public final int readBufferSize = 1000;
	
	private Socket socket;
	private InputStream is;
	private BufferedReader in;
	private PrintWriter out;
	
	public FileClient() throws UnknownHostException, IOException {
		openSocket();
	}
	
	public void getFile(String filePath, String newFileName) throws IOException {
	    try {
	    	sendLine("GET "+filePath);
			int totalSize = waitForFileSize();
			sendLine("Yes");
			readFileFromStream(totalSize, newFileName);
			sendLine("Thx");
		} catch (Exception e) {
			e.printStackTrace();
		}
	    
	    socket.close();
	}
	
	public void sendFile(String filePath, String fileName) throws IOException {
		try {
	    	sendLine("POST "+fileName);
			// wait for ok
	    	waitForOk();
	    	
			// send file to stream
	    	sendFileToStream(filePath);
			
			//wait for thx
			waitForThx();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		socket.close();
	}
	
	private void sendLine(String message) throws IOException {
	    out.println(message);
	}
	
	private void openSocket() throws UnknownHostException, IOException {
		socket = new Socket(hostname,port);
		is = socket.getInputStream();
	    in = new BufferedReader(
	            new InputStreamReader(is));
	    out = new PrintWriter(socket.getOutputStream(),	true);
	}
	
	private BufferedOutputStream getFileOutputStream(String fileName) throws FileNotFoundException {
		FileOutputStream fos = new FileOutputStream(fileName);
	    BufferedOutputStream bos = new BufferedOutputStream(fos);
	    return bos;
	}
	
	private int waitForFileSize() throws Exception {
		waitForOk();
		String line = in.readLine();
		return Integer.parseInt(line);
	}
	
	private void waitForOk() throws Exception {
		String line = in.readLine();
		if(!line.equals("OK")) {
			throw new Exception("Return message not ok");
		}
	}
	
	private void waitForThx() throws Exception {
		String line = in.readLine();
		if(!line.equals("Thx")) {
			throw new Exception("Return message not thx");
		}
	}
	
	private void readFileFromStream(int totalSize, String fileName) throws IOException {
	    int currentTot = 0;
	    int bytesRead;
	    byte [] bytearray  = new byte [readBufferSize];
	    
	    BufferedOutputStream bos;
		bos = getFileOutputStream(FileClient.filePath+fileName);
    
	    int readLength = readBufferSize;
	    while(currentTot < totalSize) {
	       bytesRead = is.read(bytearray, 0, readLength);
	       
	       bos.write(bytearray,  0, bytesRead);
	       bos.flush();
	       
	       bytearray = new byte [readBufferSize];
	       currentTot = currentTot + bytesRead;

	       if((currentTot+readBufferSize) > totalSize) {
	    	   readLength = totalSize - currentTot;
	       }
	    } 
	}
	
	/**
	 * Sends a file to client using DataOutputStreams
	 * 
	 * @param filePath
	 * @throws IOException
	 */
	private void sendFileToStream(String filePath) throws IOException {
		// load file
		FileResource fr = new FileResource(filePath);

		// setup
		FileInputStream fis = new FileInputStream(fr);
		BufferedInputStream bis = new BufferedInputStream(fis, 128);
		DataInputStream dis = new DataInputStream(bis);
		OutputStream os = socket.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);
		
		sendLine(new Integer(fr.getByteSize()).toString());

		// tries to read the complete file into the inputstream
		dis.readFully(fr.getBytes(), 0, fr.getByteSize());

		// tries to write the complete file into the outputstream
		dos.write(fr.getBytes(), 0, fr.getByteSize());

		// output the buffer to the socket
		dos.flush();

		// close the streams
		dis.close();
		dos.close();
	}
	
    public static void main(String[] args) {
    	FileClient fc;
		try {
			fc = new FileClient();
			//fc.getFile("/plaatje3.jpg", "platje.jpg");
			fc.sendFile(FileClient.filePath+"/100mb.bin", "/zut.jpg");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
    }       
}