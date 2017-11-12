import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Diagram extends Application
{
	GraphicsContext gc;

	@Override
	public void start(Stage primaryStage) throws Exception
	{
		Canvas canvas = new Canvas(1000, 800);
		gc = canvas.getGraphicsContext2D();
		gc.setLineWidth(1);
		new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					drawUser(50, 70, "user1");
					Thread.sleep(1000);
					drawArrowLine(50, 120, 300, 0);
					gc.fillText("Keyboard/Monitor", 170, 115);
					drawBox(350, 50, "Coordinator",
							"Application for\nUpload and parse\nJSON File,connecting to\nother nodes");
					Thread.sleep(1000);
					drawArrowLine(300, 350, 210, 315);
					gc.fillText("TCP & UDP Connection", 310, 270);
					drawArrowLine(700, 350, 210, 225);
					gc.fillText("TCP & UDP Connection", 550, 270);
					Thread.sleep(1000);
					drawBox(150, 350, "Node1",
							"Application for\ndisplay the\nsimulation and send\nconfirmation to\ncoordinator");
					drawBox(550, 350, "Node2",
							"Application for\ndiaplay the\nsimulation and send\nconfirmation to\ncoordinator");
					Thread.sleep(1000);

					drawArrowLine(50, 600, 200, 330);
					gc.fillText("Keyboard/Monitor", 100, 550);
					drawArrowLine(950, 600, 200, 210);
					gc.fillText("Keyboard/Monitor", 800, 550);
					Thread.sleep(1000);
					drawUser(50, 550, "user2");
					drawUser(950, 550, "user3");
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}).start();
		Group root = new Group();
		root.getChildren().add(canvas);
		Scene scene = new Scene(root, 1000, 800);
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	void drawUser(int x, int y, String name)
	{
		gc.save();
		gc.translate(x, y);
		gc.strokeOval(-15, 0, 30, 25);
		gc.strokeLine(-30, 35, 30, 35);
		gc.strokeLine(0, 25, 0, 65);
		gc.strokeLine(0, 65, -30, 100);
		gc.strokeLine(0, 65, 30, 100);
		gc.fillText(name, -15, 120);
		gc.restore();
	}

	void drawBox(int x, int y, String title, String content)
	{
		gc.save();
		gc.translate(x, y);
		gc.setFill(Color.BLACK);
		gc.strokeLine(0, 0, 10, -10);
		gc.strokeLine(10, -10, 310, -10);
		gc.strokeLine(310, -10, 300, 0);
		gc.strokeLine(300, 0, 0, 0);
		gc.strokeLine(0, 0, 0, 150);
		gc.strokeLine(0, 150, 300, 150);
		gc.strokeLine(300, 150, 310, 140);
		gc.strokeLine(310, 140, 310, -10);
		gc.strokeLine(310, -10, 300, 0);
		gc.strokeLine(300, 0, 300, 150);
		gc.setFont(Font.font(16));
		gc.strokeLine(5, 22, 15 + title.length() * 8, 22);
		gc.fillText(title, 5, 20);
		gc.strokeRect(70, 25, 215, 110);
		gc.setFill(Color.WHITE);
		gc.strokeRect(50, 45, 40, 20);
		gc.fillRect(51, 46, 38, 18);
		gc.strokeRect(50, 85, 40, 20);
		gc.fillRect(51, 86, 38, 18);
		gc.setFill(Color.BLACK);
		gc.fillText(content, 100, 50);
		gc.restore();
	}

	void drawArrowLine(int x, int y, double len, double angle)
	{
		// double len = Math.sqrt((x - ex) * (x - ex) + (y - ey) * (y - ey));
		// double angle = 180+Math.atan((y - ey) / (x - ex) * 1.0) * 180;
		gc.save();
		gc.translate(x, y);
		gc.rotate(angle);
		gc.strokeLine(0, 0, len, 0);
		gc.fillPolygon(new double[]
		{ 0, 10, 10 }, new double[]
		{ 0, -5, 5 }, 3);
		gc.fillPolygon(new double[]
		{ len, len - 10, len - 10 }, new double[]
		{ 0, -5, 5 }, 3);
		gc.restore();
	}

	public static void main(String[] args)
	{
		launch(args);
	}
}
