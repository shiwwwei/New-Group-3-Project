package application;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class serverSocketTask {
	
	private ServerSocket server = null;
	public Socket soc = null;
	public FileData fileDataObject = null;
	private ObjectInputStream istream = null;
	private File recvFile = null;
	private FileOutputStream fileStream =null;
	public String fileDir;
	
	public serverSocketTask() {}
	public String serverSocketCreate(String directory, int port) {
		//start Socket  
		try {
			fileDir = directory;
			server = new ServerSocket(port,10);
			server.setReuseAddress(true);
			System.out.println("starting Server at :" + port);
			
		}catch(IOException e) {
			e.printStackTrace();
			return "Socket Creation Error :(Maybe already in use)";
		}
		return "Successful Socket Creation at port"+ port;
	}
	public String getFileFromClient()
	{
		try {
			soc = server.accept();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("Accepted a connection");
		try {
			istream = new ObjectInputStream(soc.getInputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("receieved a stream");
			try {
				fileDataObject = (FileData) ( istream).readObject();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return "Error";
			}
			System.out.println(fileDataObject.getFilename());
			String outputFile = fileDir + "\\"+fileDataObject.getFilename();
			
			recvFile = new File(outputFile);
			try {
				fileStream = new FileOutputStream(recvFile);
				fileStream.write(fileDataObject.getData());
				fileStream.flush();
				fileStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				return "Error";
			}
			System.out.println("Output file : " + outputFile + " is successfully saved ");
		return "Server successfully received a file" +fileDataObject.getFilename() + "from :" + soc.getInetAddress().toString();
	}
	
	public String stopServer()  {
		try {
			System.out.println("Stopping Server...");
			soc.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "Error";
		}
		return "Success";
	}
	
	
	
	
	
	
	
	
	
}
