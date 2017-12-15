package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;


public class distributedSystemController extends Application {
	@Override
	public void start(Stage primaryStage) {
		Parent root;
		try {
			root = FXMLLoader.load(getClass().getResource("controllerUI.fxml"));
			
			Scene scene = new Scene(root, 600,180);
		   
	        primaryStage.setTitle("Controller");
	       
	        primaryStage.setScene(scene);
	        primaryStage.setX(380);
	        primaryStage.setY(20);
	        primaryStage.show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
