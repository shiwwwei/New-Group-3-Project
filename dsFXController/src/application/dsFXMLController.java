package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.google.gson.Gson;

import application.task.taskType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.nio.channels.SocketChannel;

final class processLine {
	public String processName;
	public double lineXLocation;
}

public class dsFXMLController {
	@FXML
	Button startButton;
	@FXML
	Button stopButton;
	@FXML
	TextArea statusBox;
	@FXML
	Button classFileSelectButton;
	@FXML
	Button depFileSelectButton;
	@FXML
	Button taskFileSelectButton;
	@FXML
	TextField classFileText;
	@FXML
	TextField depFileText;
	@FXML
	TextField taskFileText;
	
	Pane basePane;
	
	public classdiag cldiagwindow = new classdiag();

	private FileChooser classDiagfileChooser = new FileChooser();
	private FileChooser depDiagfileChooser = new FileChooser();
	private FileChooser taskDiagfileChooser = new FileChooser();
	private String classDiagText;
	private String depDiagText;
	private String taskDiagText;

	private broadcastIntent controllerBroadcast = new broadcastIntent();

	private JSONClassdiag classDiagObj = null;
	private JSONDepDiag depDiagObj = null;
	private JSONTaskDiag taskDiagObj = null;
	private List<String> devices;
	private int port;
	private int updateListenPort;
	private String broadcastMessage;

	Thread broadcastThread = null;
	Thread clientControlThread = null;
	public boolean clientRunUP;
	public List<nodeReference> nodes = new ArrayList();

	private Socket socket = null;
	private ServerSocket server = null;

	Socket updateProcessSocket = null;
	ServerSocket updateProcessserver = null;

	public boolean allProcessUp = false;
	public boolean simulationComplete = false;
	
	public List<processLine> procLine = new ArrayList();
	public int arrowsDrawn =0;
	public double firstArrowYOffset =0;
	
	public GraphicsContext classDiagGC;
	public Canvas classDiagCanvas = new Canvas(340,220);
	
	Stage classDiagStg = new Stage();
	public List<classTree> ClassTree= new ArrayList(); 
	
	public GraphicsContext depDiagGC;
	public Canvas depDiagCanvas = new Canvas(340,460);
	
	Stage depDiagStg = new Stage();
	
	public GraphicsContext taskDiagGC;
	public Canvas taskDiagCanvas = new Canvas(800,500);
	
	Stage taskDiagStg = new Stage();
	GraphicsContext gc;
	Stage preLaunchStage = new Stage();

	
	

	public void initialize() {
//	//	gc =canvas.getGraphicsContext2D();
//		System.out.println("graphics context obtained");
//		Canvas canvas = new Canvas(1000, 800);
//		gc = canvas.getGraphicsContext2D();
//		gc.setLineWidth(1);
//		new Thread(new Runnable()
//		{
//			public void run()
//			{
//				try
//				{
//					drawUser(50, 70, "user1");
//					Thread.sleep(1000);
//					drawArrowLine(50, 120, 300, 0);
//					gc.fillText("Keyboard/Monitor", 170, 115);
//					drawBox(350, 50, "Coordinator",
//							"Application for\nUpload and parse\nJSON File,connecting to\nother nodes");
//					Thread.sleep(1000);
//					drawArrowLine(300, 350, 210, 315);
//					gc.fillText("TCP & UDP Connection", 310, 270);
//					drawArrowLine(700, 350, 210, 225);
//					gc.fillText("TCP & UDP Connection", 550, 270);
//					Thread.sleep(1000);
//					drawBox(150, 350, "Node1",
//							"Application for\ndisplay the\nsimulation and send\nconfirmation to\ncoordinator");
//					drawBox(550, 350, "Node2",
//							"Application for\ndiaplay the\nsimulation and send\nconfirmation to\ncoordinator");
//					Thread.sleep(1000);
//
//					drawArrowLine(50, 600, 200, 330);
//					gc.fillText("Keyboard/Monitor", 100, 550);
//					drawArrowLine(950, 600, 200, 210);
//					gc.fillText("Keyboard/Monitor", 800, 550);
//					Thread.sleep(1000);
//					drawUser(50, 550, "user2");
//					drawUser(950, 550, "user3");
//				} catch (InterruptedException e)
//				{
//					e.printStackTrace();
//				}
//			}
//		}).start();
//		Pane root = new Pane();
//		root.getChildren().add(canvas);
//		Scene scene = new Scene(root, 1000, 700);
//		preLaunchStage.setScene(scene);
//		preLaunchStage.show();
		//renderBackground();
	}
	
	@FXML
	protected void controllerStart() {
		
		
		port = 8008;
		updateListenPort = 8010;
		statusBox.setText(statusBox.getText() + "Controller Starting...\n");
		if (classDiagObj == null || depDiagObj == null || taskDiagObj == null) {
			statusBox.setText(statusBox.getText() + " JSON files not loaded\n");
		} else {
			statusBox.setText(statusBox.getText() + "JSON files Loaded...\n");
			statusBox.setText(statusBox.getText() + "Physical nodes required :" + devices + "\n");
			populateNodes();
			statusBox.setText(statusBox.getText() + "Node Data populated\n");
			Gson tmp = new Gson();
			System.out.println(tmp.toJson(nodes));
			clientRunUP = true;
			clientControllerTask();

			controllerBroadcast.broadcastNow = true;
			broadcastTask();
			statusBox.setText(statusBox.getText() + "Started broadcasting...\n");
			updatePortTask();
			System.out.println(allProcessUp);
			destResolvTask();
			startSimulationTask();
			
			
		
			
			

		}
	}
	
	public void startSimulationTask() {
		Runnable startSimulationTask = new Runnable() {
			public void run() {
				try {
					startSimulationRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread startSimulationThread = new Thread(startSimulationTask);
		startSimulationThread.setDaemon(true);
		startSimulationThread.start();
	}
	public void startSimulationRun() throws InterruptedException {
		while (allProcessUp != true)
			Thread.sleep(100);
		for(int i=0;i<nodes.size();i++) {
			for(int j=0;j<nodes.get(i).procs.size();j++) {
				try {
					Socket soc = new Socket(nodes.get(i).ipAddress,nodes.get(i).procs.get(j).recvPort);
					DataOutputStream dis = new DataOutputStream(soc.getOutputStream());
					dis.writeUTF("start");
					soc.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}

	public void destResolvTask() {
		Runnable destResolvTask = new Runnable() {
			public void run() {
				try {
					destResolvRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread destResolvThread = new Thread(destResolvTask);
		destResolvThread.setDaemon(true);
		destResolvThread.start();
	}

	public void destResolvRun() throws InterruptedException {
		while(!allProcessUp) 
			Thread.sleep(100);
		if(allProcessUp) {
		try {
			ServerSocket destResolvServer = new ServerSocket(updateListenPort);
			System.out.println("ready to resolv at" + updateListenPort);
			while(!simulationComplete) {
				Socket resolvSoc = destResolvServer.accept();
				DataInputStream request = new DataInputStream(resolvSoc.getInputStream());
				String req = request.readUTF();
				task tmpTask = new task();
				Gson taskGson=new Gson();
				tmpTask=taskGson.fromJson(req, tmpTask.getClass());
				req = tmpTask.to;
				task finTask = tmpTask;
				double randSleep =Math.random()*2000;
				try {
					Thread.sleep(0+(int)randSleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Platform.runLater(new Runnable() {
						public void run() {
						//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
							
			                drawArrow(finTask);
							}
				});
				for (int i = 0; i < nodes.size(); i++) {
					for (int j = 0; j < nodes.get(i).procs.size(); j++) {
						if (nodes.get(i).procs.get(j).name.equals(req)) {
							DataOutputStream resp = new DataOutputStream(resolvSoc.getOutputStream());
							resp.writeUTF(nodes.get(i).ipAddress + "," +nodes.get(i).procs.get(j).recvPort);
							
						}
					}
				}
			}
			destResolvServer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		}
	}

	public void updatePortTask() {
		Runnable updatePortTask = new Runnable() {
			public void run() {
				try {
					updatePortRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		Thread updatePortThread = new Thread(updatePortTask);
		updatePortThread.setDaemon(true);
		updatePortThread.start();
	}

	public void updatePortRun() throws InterruptedException {
		int totalProcess = 0;
		for (int i = 0; i < nodes.size(); i++) {
			totalProcess = totalProcess + nodes.get(i).procs.size();
		}
		int processUpdated = 0;

		try {
			updateProcessserver = new ServerSocket(updateListenPort);
			while (processUpdated < totalProcess) {
				updateProcessSocket = updateProcessserver.accept();
				DataInputStream in = new DataInputStream(updateProcessSocket.getInputStream());
				String str = (String) in.readUTF();
				System.out.println("receiving from :" + str);
				List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
				int processPort = Integer.valueOf(items.get(1).replaceAll("[^\\d.]", ""));
				String procName = items.get(0);
				for (int i = 0; i < nodes.size(); i++) {
					for (int j = 0; j < nodes.get(i).procs.size(); j++) {
						if (nodes.get(i).procs.get(j).name.equals(procName)) {
							nodes.get(i).procs.get(j).recvPort = processPort;
						}
					}
				}
				processUpdated++;
			}
			System.out.println("All process ports Updated");
			Gson tmp = new Gson();
			System.out.println(tmp.toJson(nodes));
			allProcessUp = true;
			updateProcessSocket.close();
			updateProcessserver.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void populateNodes() {
		for (int i = 0; i < devices.size(); i++) {
			nodeReference tempNode = new nodeReference();
			tempNode.name = devices.get(i);

			List<String> procNames = depDiagObj.getProcessForNodes(tempNode.name);

			for (int j = 0; j < procNames.size(); j++) {

				processes tempProcs = new processes();
				tempProcs.name = procNames.get(j);

				List<nodeContent> tempNodeContent = new ArrayList();
				tempNodeContent = taskDiagObj.getTaskForProcess(tempProcs.name);
				for (int k = 0; k < tempNodeContent.size(); k++) {
					task tempTask = new task();
					if (tempNodeContent.get(k).from.equals(tempProcs.name))
						tempTask.taskType = taskType.SEND;
					else
						tempTask.taskType = taskType.RECV;
					tempTask.from = tempNodeContent.get(k).from;
					tempTask.to = tempNodeContent.get(k).to;
					tempTask.msg = String.join(" ", tempNodeContent.get(k).message);
					tempProcs.tasks.add(tempTask);
				}
				tempNode.procs.add(tempProcs);
			}

			nodes.add(tempNode);
		}

	}

	@FXML
	protected void controllerStop() {
		controllerBroadcast.broadcastNow = false;
		this.clientRunUP = false;
		if (broadcastThread != null) {
			try {
				broadcastThread.join(200);
				clientControlThread.join(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	public void clientControllerTask() {
		Runnable clientControlTask = new Runnable() {
			public void run() {
				try {
					clientControlRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		clientControlThread = new Thread(clientControlTask);
		clientControlThread.setDaemon(true);
		clientControlThread.start();
	}

	public void clientControlRun() throws InterruptedException {

		System.out.println(nodes.size() + " \n");
		int nodesConnected = 0;
		System.out.println("Listening for nodes");
		try {
			server = new ServerSocket(port);
			while (nodesConnected < nodes.size()) {
				socket = server.accept();
				DataInputStream in = new DataInputStream(socket.getInputStream());
				String str = (String) in.readUTF();
				System.out.println("Accepted Connection from :" + str);
				for (int i = 0; i < nodes.size(); i++) {
					if (nodes.get(i).name.equals(str)) {
						nodesConnected++;
						DataOutputStream out = new DataOutputStream(socket.getOutputStream());
						Gson gsonObj = new Gson();
						out.writeUTF(gsonObj.toJson(nodes.get(i)));
						str = (String) in.readUTF();
						System.out.println("client said :" + str + "\n");
						InetSocketAddress tempNodeIP = (InetSocketAddress) socket.getRemoteSocketAddress();
						System.out.println(tempNodeIP.getAddress().getHostAddress());
						nodes.get(i).ipAddress = tempNodeIP.getAddress().getHostAddress();
					}
				}
			}

			server.close();
		} catch (IOException i) {
			System.out.println(i);
		}

		System.out.println("Quitting listen for nodes \n");

	}

	public void broadcastTask() {
		Runnable servertask = new Runnable() {

			public void run() {
				try {
					broadcastRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		};

		broadcastThread = new Thread(servertask);
		// Terminate the running thread if the application exits
		broadcastThread.setDaemon(true);
		// Start the thread
		broadcastThread.start();
	}

	public void broadcastRun() throws InterruptedException {
		while (controllerBroadcast.broadcastNow) {
			controllerBroadcast.sendBroadcast(Integer.toString(port) + "," + Integer.toString(updateListenPort));
			Thread.sleep(200);
		}

	}
	

	@FXML
	protected void classFileSelect(ActionEvent event) {
		Node source = (Node) event.getSource();
		Window stage = source.getScene().getWindow();
		File file = classDiagfileChooser.showOpenDialog(stage);
		if (file != null) {

			classFileText.setText(file.getPath());
			try {
				classDiagText = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				statusBox.setText(statusBox.getText() + "\n" + "unable to process File");
			}

			classDiagObj = new JSONClassdiag();
			Gson gsonClassDiag = new Gson();

			classDiagObj = gsonClassDiag.fromJson(classDiagText, classDiagObj.getClass());
			String testString = gsonClassDiag.toJson(classDiagObj);
			System.out.println(testString);
			ClassTree = classDiagObj.parse();
			gsonClassDiag = new Gson();
			String test = gsonClassDiag.toJson(ClassTree);
			System.out.println(test);
			//root = FXMLLoader.load(getClass().getResource("classdiagFXML.fxml"));
			Pane root = new Pane();
			
		
			classDiagGC = classDiagCanvas.getGraphicsContext2D();
		//	classDiagGC.fillText("Distributed System Messaging", 10, 20);
			
			
			Scene scene = new Scene(root, 340,220);
			
			   
			classDiagStg.setTitle("classDiag");
      
			classDiagStg.setScene(scene);
			classDiagStg.setX(0.0);
			
			classDiagStg.setY(0.0);
			root.getChildren().add(classDiagCanvas);
			classDiagStg.show();
			drawClass(ClassTree);
			
		}

	}

	public void drawClass(List <classTree> clsdia) {
		
		classDiagGC.setFill(Color.MOCCASIN);
		
		classDiagGC.setStroke(Color.DARKRED);
		classDiagGC.setLineWidth(2);
		classDiagGC.strokeRect(0, 0, classDiagGC.getCanvas().getWidth(), classDiagGC.getCanvas().getHeight());
		classDiagGC.setFill(Color.BLACK);
		classDiagGC.fillText("Distributed System Messaging : Class Diagram", 10, 20);
		classDiagGC.setFill(Color.MOCCASIN);
		int numberOfRootNodes = clsdia.size();
		double xOffset = 10;
		double yOffset = 40;
		double cellXSize = (classDiagGC.getCanvas().getWidth()/numberOfRootNodes)- xOffset -10;
		double cellYSize = classDiagGC.getCanvas().getHeight() - 50;
		for(int i=0;i<numberOfRootNodes;i++) {
			classDiagGC.strokeRect(xOffset, yOffset, cellXSize,cellYSize);
			classDiagGC.setFill(Color.BLACK);
			classDiagGC.fillText("Class Name :" + clsdia.get(i).name, xOffset+5, yOffset+20);
			for(int j=0;j<clsdia.get(i).fields.size();j++) {
				classDiagGC.fillText(clsdia.get(i).fields.get(j).type+":"+clsdia.get(i).fields.get(j).name ,xOffset+5,yOffset+20*(j+2));
			}
			double nodeWidth = (cellXSize/clsdia.get(i).nodes.size()) -20;
			
			double nodeXoffset = xOffset+10;
			double nodeYoffset = yOffset +yOffset+20*(clsdia.get(i).fields.size());
			double nodeHeight = cellYSize-nodeYoffset;
			for(int j=0;j<clsdia.get(i).nodes.size();j++) {
				classDiagGC.strokeRect(nodeXoffset,nodeYoffset, nodeWidth,nodeHeight);
				classDiagGC.fillText("Class Name :" + clsdia.get(i).nodes.get(j).name, nodeXoffset+5, nodeYoffset+20);
				for(int k=0;k<clsdia.get(i).nodes.get(j).fields.size();k++) {
					classDiagGC.fillText(clsdia.get(i).nodes.get(j).fields.get(k).type+":"+clsdia.get(i).nodes.get(j).fields.get(k).name ,nodeXoffset+5, nodeYoffset+20*(k+2));
				}
				nodeXoffset+=nodeWidth+20;
			}
			xOffset = xOffset+cellXSize+10;
			
		}
		
		
	}
	
	@FXML

	protected void depFileSelect(ActionEvent event) {

		Node source = (Node) event.getSource();
		Window stage = source.getScene().getWindow();

		File file = depDiagfileChooser.showOpenDialog(stage);
		if (file != null) {

			depFileText.setText(file.getPath());

			try {
				depDiagText = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				statusBox.setText(statusBox.getText() + "\n" + "unable to process File");
			}

			depDiagObj = new JSONDepDiag();
			Gson gsonDepDiag = new Gson();
			depDiagObj = gsonDepDiag.fromJson(depDiagText, depDiagObj.getClass());
			// String testString = gsonDepDiag.toJson(depDiagObj);
			devices = depDiagObj.getPhysicalDevices();
			// portsRequired = devices.size();
			System.out.println("List of Physical nodes :\n" + devices);
			String testString = gsonDepDiag.toJson(depDiagObj);
			System.out.println(testString);
			
			Pane root = new Pane();
			
			
			depDiagGC = depDiagCanvas.getGraphicsContext2D();
		//	classDiagGC.fillText("Distributed System Messaging", 10, 20);
			
			
			Scene scene = new Scene(root, 340,460);
			
			   
			depDiagStg.setTitle("DepDiag");
      
			depDiagStg.setScene(scene);
			depDiagStg.setX(0.0);
			
			depDiagStg.setY(260);
			
			root.getChildren().add(depDiagCanvas);
			depDiagStg.show();
			drawNodes(depDiagObj);

		}

	}
	public void drawNodes(JSONDepDiag depDiagObj) {
		depDiagGC.setFill(Color.MOCCASIN);
		
		depDiagGC.setStroke(Color.DARKRED);
		depDiagGC.setLineWidth(2);
		depDiagGC.strokeRect(0, 0, depDiagGC.getCanvas().getWidth(), depDiagGC.getCanvas().getHeight());
		depDiagGC.setFill(Color.BLACK);
		depDiagGC.fillText("Distributed System Messaging : Deployment Diagram", 10, 20);
		depDiagGC.setFill(Color.MOCCASIN);
		int numberOfNodes = devices.size();
		double xOffset = 10;
		double yOffset = 40;
		double cellXSize = depDiagGC.getCanvas().getWidth() - 50;
		double cellYSize =(depDiagGC.getCanvas().getHeight()/numberOfNodes)- yOffset -10;
		for(int i=0;i<devices.size();i++) {
			depDiagGC.strokeRect(xOffset, yOffset, cellXSize,cellYSize);
			depDiagGC.setFill(Color.DARKRED);
			depDiagGC.fillText(devices.get(i), xOffset+10, yOffset+20);
			List<String> procsInNode = depDiagObj.getProcessForNodes(devices.get(i));
			double nodeWidth = (cellXSize/procsInNode.size()) -20;
			
			double nodeXoffset = xOffset+10;
			double nodeYoffset = yOffset+30;
			double nodeHeight = cellYSize - 40;
			for(int j=0;j<procsInNode.size();j++) {
				depDiagGC.setStroke(Color.BLACK);
				depDiagGC.strokeRect(nodeXoffset, nodeYoffset, nodeWidth,nodeHeight);
				depDiagGC.fillText(procsInNode.get(j), nodeXoffset+nodeWidth/2 , nodeYoffset+(nodeHeight)/2);
				nodeXoffset = nodeXoffset+nodeWidth+20;
				depDiagGC.setStroke(Color.DARKRED);
				System.out.println("rect at"  +nodeXoffset+ ":"+ nodeYoffset+":"+nodeWidth+":"+nodeHeight);
			}
			yOffset = yOffset+cellYSize+10;
		}
		
	}

	@FXML
	protected void taskFileSelect(ActionEvent event) {

		Node source = (Node) event.getSource();
		Window stage = source.getScene().getWindow();

		File file = taskDiagfileChooser.showOpenDialog(stage);
		if (file != null) {

			taskFileText.setText(file.getPath());

			try {
				taskDiagText = new String(Files.readAllBytes(file.toPath()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				statusBox.setText(statusBox.getText() + "\n" + "unable to process File");
			}
			taskDiagObj = new JSONTaskDiag();

			Gson gsonTaskDiag = new Gson();
			taskDiagObj = gsonTaskDiag.fromJson(taskDiagText, taskDiagObj.getClass());
			String testString = gsonTaskDiag.toJson(taskDiagObj);
			System.out.println(testString);
			
			Pane root = new Pane();
			
			
			taskDiagGC = taskDiagCanvas.getGraphicsContext2D();
		//	classDiagGC.fillText("Distributed System Messaging", 10, 20);
			
			
			Scene scene = new Scene(root, 800,460);
			
			   
			taskDiagStg.setTitle("Task Sequence Diagram");
      
			taskDiagStg.setScene(scene);
			taskDiagStg.setX(340);
			
			taskDiagStg.setY(220);
			
			root.getChildren().add(taskDiagCanvas);
			taskDiagStg.show();
			renderBackground();
			drawProcess(taskDiagObj);
		}
		
		//Rectangle rect = new Rectangle(canvas.getLayoutX(), canvas.getLayoutY(), canvas.getLayoutX()+canvas.getWidth(), canvas.getHeight());
		
		
	}
	public void drawProcess(JSONTaskDiag taskd)
	{
		double width = taskDiagGC.getCanvas().getWidth()/(2*taskd.processes.length);
		double height = width *3/4;
		double YOffset = height/2;
		for(int i=1;i<=taskd.processes.length;i++) {
			taskDiagGC.setLineWidth(2);
			double Xoffset= (((taskDiagGC.getCanvas().getWidth()/taskd.processes.length)*i )- (taskDiagGC.getCanvas().getWidth()/(2*taskd.processes.length))) - (width/2);
			taskDiagGC.strokeRect(Xoffset, YOffset, width, height);
			taskDiagGC.setLineWidth(0.5);
			taskDiagGC.strokeLine(Xoffset+(width/2), YOffset+height, Xoffset+(width/2), taskDiagCanvas.getHeight());
			taskDiagGC.setFill(Color.BLACK);
			taskDiagGC.fillText(taskd.processes[i-1].name+ ":" + taskd.processes[i-1].classes, Xoffset+width/5, YOffset+(height/2));
			taskDiagGC.setFill(Color.MOCCASIN);
			processLine tempProcLine= new processLine();
			tempProcLine.lineXLocation =Xoffset+(width/2);
			tempProcLine.processName = 	taskd.processes[i-1].name;	
			procLine.add(tempProcLine);
		}
		//Font font = new Font(12);
		//System.out.println(Font.getFontNames());
		firstArrowYOffset= (YOffset+height)+taskDiagGC.getCanvas().getHeight()/8;
		
	}
	public void renderBackground() {
		taskDiagGC.setFill(Color.MOCCASIN);
		taskDiagGC.setStroke(Color.DARKRED);
		taskDiagGC.setLineWidth(2);
		taskDiagGC.strokeRect(0, 0, taskDiagGC.getCanvas().getWidth(), taskDiagGC.getCanvas().getHeight());
		taskDiagGC.setFill(Color.BLACK);
		taskDiagGC.fillText("Distributed System Messaging : Task Sequence Diagram", 10, 20,200);
		taskDiagGC.setFill(Color.MOCCASIN);
	}
	public int totalSendTasks() {
		int totSendTask=0;
		for(int i=0;i<nodes.size();i++) {
			for(int j=0;j<nodes.get(i).procs.size();j++) {
				for(int k=0;k<nodes.get(i).procs.get(j).tasks.size();k++)
				if (nodes.get(i).procs.get(j).tasks.get(k).taskType == taskType.SEND)
					totSendTask++;
	
			}
		}
		return totSendTask;
	}
	public void drawArrow (task tmpTask) {
		double fromX=0;
		double toX=0;
		
		for(int i=0;i<procLine.size();i++) {
			if(procLine.get(i).processName.equals(tmpTask.from)) 
			{
				fromX = procLine.get(i).lineXLocation;
				
			}
			if(procLine.get(i).processName.equals(tmpTask.to))
			{
				toX=procLine.get(i).lineXLocation;
			}
		}
		
		double Y = firstArrowYOffset + (arrowsDrawn*((taskDiagGC.getCanvas().getHeight()-firstArrowYOffset)/totalSendTasks()));
		arrowsDrawn++;
		
		taskDiagGC.setStroke(Color.DARKBLUE);
		taskDiagGC.setLineWidth(0.85);
		taskDiagGC.strokeLine(fromX, Y, toX, Y);
		taskDiagGC.setFont(Font.font(10));
		taskDiagGC.strokeText(tmpTask.msg, (fromX+(toX-fromX)/4), Y-3);
		if(toX>fromX) {
			taskDiagGC.strokeLine(toX, Y,toX-6,Y-6);
			taskDiagGC.strokeLine(toX, Y,toX-6,Y+6);
		}
		else {
			taskDiagGC.strokeLine(toX, Y,toX+6,Y-6);
			taskDiagGC.strokeLine(toX, Y,toX+6,Y+6);
		}
		
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

}