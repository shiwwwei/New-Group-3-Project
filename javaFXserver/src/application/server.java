package application;
	
import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;


public class server extends Application {
	@Override
	public void start(Stage primaryStage) throws IOException {
		Parent root = FXMLLoader.load(getClass().getResource("Server.fxml"));
		
        Scene scene = new Scene(root, 640,480);
    
        primaryStage.setTitle("Server");
        primaryStage.setScene(scene);
        primaryStage.show();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
