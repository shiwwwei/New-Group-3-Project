package application;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class clientSocketTask {
	
	public Socket soc = null;
	private FileData fileDataObject = null;
	private ObjectOutputStream ostream = null;
	private boolean isConnected = false;
	private File sendFile = null;
	public clientSocketTask() {}
	
	public String connect(String ip,String port)  {
		while (!isConnected) {
			try {
				soc = new Socket(ip, Integer.parseInt(port));
				ostream = new ObjectOutputStream(soc.getOutputStream());
				isConnected = true;
				System.out.println("Connected");
				
			}catch (IOException e) {
				e.printStackTrace();
			//	return "Error in connecting";
			}
		}
		return "Connection Successful";
			
	}
	public void sendFile(String file,String fileName) {
		fileDataObject = new FileData();
		
		fileDataObject.setfilename(fileName);
		
		sendFile = new File(file);
		if (sendFile.isFile()) {
			try {
				DataInputStream diStream = new DataInputStream(new FileInputStream(file));
				int len = (int) sendFile.length();
				byte[] fileBytes = new byte[ len];
				int read = 0;
				int numRead = 0;
				while (read < fileBytes.length && (numRead = diStream.read(fileBytes, read, fileBytes.length - read)) >= 0) {
					read = read + numRead;
				}
				fileDataObject.setFileSize(len);
				fileDataObject.setData(fileBytes);
				diStream.close();
			} catch (Exception e) {	e.printStackTrace();}
		} 
		else {
			System.out.println("path specified is not pointing to a file");
		}
		//Now writing the FileEvent object to socket
		try {
			ostream.writeObject(fileDataObject);
			//System.out.println("Done...Going to exit");

	//	Thread.sleep(3000);
			//System.exit(0);
		} catch (IOException e) {e.printStackTrace();}
	}
	
	
	
	
	
	
	
	
	
	
}
