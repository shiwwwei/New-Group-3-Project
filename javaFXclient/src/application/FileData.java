package application;
import java.io.Serializable;

public class FileData implements Serializable{
	private static final long serialVersionUID = 42L;
	
	public FileData() {}
	
	
	private String filename;
	private long size;
	private byte[] data;

	
	
	
	public String getFilename() {
		return filename;
	}
	public void setfilename(String name) {
		this.filename=name;
	}
	public long getFileSize() {
		return size;
	}
	public void setFileSize(long fileSize) {
		this.size= fileSize;
	}
	public byte[] getData() {
		return data;
	}
	public void setData(byte[] fData) {
		this.data=fData;
	}
	
}
