package application;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

public class serverFXMLController {
	@FXML Button startServerButton;
	@FXML Button stopServerButton;
	@FXML Button selectFolder;
	@FXML Label uploadFolder;
	@FXML TextField port;
	@FXML TextArea connStat;
	private serverSocketTask servRef = new  serverSocketTask();
	private DirectoryChooser selectedDirectory = new DirectoryChooser();
	Thread backgroundThread;
	public static Runnable runnable;
	Gson gson = new Gson();
	String fileContent;
	
	@FXML protected void folderSelection(ActionEvent event) {
        
        Node source = (Node) event.getSource();
        Window stage = source.getScene().getWindow();
        
	                   File file = selectedDirectory.showDialog(stage);
	                   if (file != null) {
	                       System.out.println(file.getPath() );
	                       uploadFolder.setText(file.getPath());
	                   }
	                
	                   connStat.setText("Setting folder to : " + uploadFolder.getText());    
	                   
	}
	@FXML protected void startServer(ActionEvent event) {
		connStat.setText(connStat.getText() +"\n"+"Starting Server");
		stopServerButton.setDisable(false);
		startServerButton.setDisable(true);
		selectFolder.setDisable(true);
		port.setDisable(true);
		serverTask();
		
	}
	public void serverTask ()
	{
		Runnable servertask=new Runnable() {
			
			public void run() {
				 	serverRun();
			}
			
			
		};
		
		 backgroundThread = new Thread(servertask);
         // Terminate the running thread if the application exits
         backgroundThread.setDaemon(true);
         // Start the thread
         backgroundThread.start();

	}
	@FXML protected void stopServer(ActionEvent event) {
		connStat.setText(connStat.getText() +"\n"+"Stopping Servers");
		stopServerButton.setDisable(true);
		startServerButton.setDisable(false);
		selectFolder.setDisable(false);	
		
		Platform.exit();
		
	}
	public void serverRun() {
		//server code
		connStat.setText(connStat.getText()+"\n"+servRef.serverSocketCreate(uploadFolder.getText(),Integer.parseInt(port.getText())));		
		connStat.setText(connStat.getText()+"\n"+servRef.getFileFromClient());
		System.out.println(servRef.fileDir + "\\"+ servRef.fileDataObject.getFilename());
		try {
			fileContent = new String(Files.readAllBytes(Paths.get(servRef.fileDir + "\\"+ servRef.fileDataObject.getFilename())));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			connStat.setText(connStat.getText()+"\n"+"unable to process File");
		}
		System.out.println(fileContent);
		
		connStat.setText(connStat.getText()+"\n JSON parsed to :\n"+(gson.fromJson(fileContent, JSONClassdiag.class).toString()));
		
	}
}
