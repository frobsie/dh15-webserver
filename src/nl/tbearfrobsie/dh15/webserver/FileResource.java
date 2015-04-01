package nl.tbearfrobsie.dh15.webserver;

import java.io.File;

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
}
