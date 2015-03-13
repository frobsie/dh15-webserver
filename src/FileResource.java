import java.io.File;

public class FileResource extends File {

	private static final long serialVersionUID = 1L;

	private byte[] bytes;

	public FileResource(String pathname) {
		super(pathname);
		this.bytes = new byte[(int) this.length()];
	}

	public byte[] getBytes() {
		return this.bytes;
	}

	public int getByteSize() {
		return this.bytes.length;
	}
}