package application;

import java.io.File;
import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class clientFXMLController {
	@FXML private Label fileSelection;
	@FXML private Pane basePane;
	@FXML private TextArea connStat;
	@FXML private TextField ipAddress;
	@FXML private TextField port;
	@FXML private Button connect;
	@FXML private Button send;
	@FXML private Button fileSelect;
	private clientSocketTask clientref = new clientSocketTask(); ;
	private String filename ;
	
	Thread backgroundThread;
	
	//@FXML private Stage stage;
	private FileChooser fileChooser = new FileChooser();
	
	
	 @FXML protected void selectFile(ActionEvent event) {
	      //  connStat.setText("code to select file");
	        Node source = (Node) event.getSource();
	        Window stage = source.getScene().getWindow();
	        
		                   File file = fileChooser.showOpenDialog(stage);
		                   if (file != null) {
		                       System.out.println(file.getPath() );
		                       fileSelection.setText(file.getPath());
		                       filename=file.getName();
		                   }
		                
		            
		            
	 }
	 
	 @FXML protected void connectIntent(ActionEvent event) {
	        connStat.setText(connStat.getText()+"Connecting to " +ipAddress.getText()+":"+port.getText()+"\n");
	      //  ipAddress.setDisable(true);
	       // port.setDisable(true);
	        clientref.connect(ipAddress.getText(), port.getText());
	        connStat.setText(connStat.getText()+"Connected\n");
	        
	        
	 }
	 public void clientTask ()
		{
			Runnable clienttask=new Runnable() {
				public void run() {
					 	clientSend();
				}
				
			};
			
			 backgroundThread = new Thread(clienttask);
	         // Terminate the running thread if the application exits
	         backgroundThread.setDaemon(true);
	         // Start the thread
	         backgroundThread.start();

		}
	 @FXML protected void sendIntent(ActionEvent event) {
	        connStat.setText(connStat.getText()+"Sending "+fileSelection.getText()+" to " +ipAddress.getText()+":"+port.getText() + "\n");
	        clientTask();
	        
	 }
	 
	 public void clientSend() {
			//server code
			
		 	clientref.sendFile(fileSelection.getText(), filename);
		 	connStat.setText(connStat.getText() + "Sent " + filename +" to Server successfully\n");
			
		}

}
