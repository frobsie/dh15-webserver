package nl.tbearfrobsie.dh15.webserver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileResource extends File {

	/** Serializable */
	private static final long serialVersionUID = 1L;

	/** Bytearray which stores the file content */
	private byte[] bytes;

	/**
	 * Constructor.
	 * Creates a regular java File object
	 * based on given pathname and setup up
	 * the resource's bytearray.
	 * 
	 * @param String pathname
	 */
	public FileResource(String pathname) {
		super(pathname);
		this.bytes = new byte[(int) this.length()];
	}

	/**
	 * Returns the content
	 * in this FileResource in bytes
	 * 
	 * @return byte[]
	 */
	public byte[] getBytes() {
		return this.bytes;
	}

	/**
	 * Returns number of bytes
	 * in this fileresource.
	 * 
	 * @return int
	 */
	public int getByteSize() {
		return this.bytes.length;
	}
	
	/**
	 * Get content type for given file resource.
	 * Zie : http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html#probeContentType(java.nio.file.Path)
	 * 
	 * @param FileResource file
	 * @return String
	 * @throws IOException 
	 */
	public String getContentType() throws IOException {
		String mimeType = Files.probeContentType(this.toPath());
		return mimeType;
	}
}
