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
import java.sql.Timestamp;
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
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import java.nio.channels.SocketChannel;

final class imageRef {
	public String processName;
	public double lineXLocation;
	public double x;
	public double y;
	public double width;
	public double height;
	public double currentYLoc =0;
}

public class dsFXMLController {
	@FXML
	Button startButton;
	@FXML
	Button startSim;
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
	
	//public classdiag cldiagwindow = new classdiag();

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

	public static boolean allProcessUp = false;
	public static boolean beginSimulation = false;
	public boolean simulationComplete = false;
	
	public List<imageRef> procLine = new ArrayList();
	public List<imageRef> nodeImageBox = new ArrayList();
	public List<imageRef> nodeImageProcBox = new ArrayList();
	public List<imageRef> classImageBox = new ArrayList();
	public int arrowsDrawn =0;
	public double firstArrowYOffset =0;
	
	public GraphicsContext classDiagGC;
	public Canvas classDiagCanvas = new Canvas(800,600);
	
	Stage classDiagStg = new Stage();
	public List<classTree> ClassTree= new ArrayList(); 
	
	public GraphicsContext depDiagGC;
	public Canvas depDiagCanvas = new Canvas(600,700);
	
	Stage depDiagStg = new Stage();
	
	public GraphicsContext taskDiagGC;
	public Canvas taskDiagCanvas = new Canvas(1024,600);
	
	Stage taskDiagStg = new Stage();
	GraphicsContext gc;
	
	public imageRef presentationLayer = new imageRef();
	public List<imageRef> paraLayer = new ArrayList();
	
	//Stage preLaunchStage = new Stage();

	
	

	public void initialize() {
	//	gc =canvas.getGraphicsContext2D();
		System.out.println("graphics context obtained");
	
	}
	
	@FXML
	protected void controllerStart() {
		
		
		port = 8008;
		updateListenPort = 8010;
		statusBox.setText(statusBox.getText() + "Controller Starting...\n");
		if (classDiagObj == null || depDiagObj == null || taskDiagObj == null) {
			statusBox.setText(statusBox.getText() + " JSON files not loaded\n");
		} else {
			startButton.setDisable(true);
			
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
		while (beginSimulation != true)
			Thread.sleep(100);
		System.out.println("Am not here");;
		for(int i=0;i<nodes.size();i++) {
			for(int j=0;j<nodes.get(i).procs.size();j++) {
				for(int k=0;k<nodes.get(i).procs.get(j).paraNum;k++) {
					try {
						
						Socket soc = new Socket(nodes.get(i).ipAddress,nodes.get(i).procs.get(j).recvPort.get(k).port); 
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
		while(!beginSimulation) 
			Thread.sleep(100);
		System.out.println("Why am i here\n");
		if(beginSimulation) {
		try {
			ServerSocket destResolvServer = new ServerSocket(updateListenPort);
			System.out.println("ready to resolv at" + updateListenPort);
			while(!simulationComplete) {
				Socket resolvSoc = destResolvServer.accept();
				DataInputStream request = new DataInputStream(resolvSoc.getInputStream());
				String req = request.readUTF();
				//System.out.println(req);
				taskDetail tmpTask = new taskDetail();
				Gson taskGson=new Gson();
				tmpTask=taskGson.fromJson(req, tmpTask.getClass());
				req = tmpTask.to;
				taskDetail finTask = tmpTask;
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
							int destPort = 0;
							for(int k=0;k<nodes.get(i).procs.get(j).recvPort.size();k++) {
								if(nodes.get(i).procs.get(j).recvPort.get(k).paraID == finTask.parID) {
									destPort = nodes.get(i).procs.get(j).recvPort.get(k).port;
									resp.writeUTF(nodes.get(i).ipAddress + "," +Integer.toString(destPort));
								}
									
							}
							
							
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
			for(int k=0;k<nodes.get(i).procs.size();k++)
			totalProcess = totalProcess + nodes.get(i).procs.get(k).paraNum;
		}
		int processUpdated = 0;
		System.out.println("Total process number :"+totalProcess);
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
				int paraID = Integer.valueOf(items.get(2).replaceAll("[^\\d.]", ""));
				for (int i = 0; i < nodes.size(); i++) {
					for (int j = 0; j < nodes.get(i).procs.size(); j++) {
						if (nodes.get(i).procs.get(j).name.equals(procName)) {
							portData temp = new portData();
							temp.paraID = paraID;
							temp.port = processPort;
							nodes.get(i).procs.get(j).recvPort.add(temp) ;
							
						}
					}
				}
				processUpdated++;
			}
			System.out.println("All process ports Updated");
			Gson tmp = new Gson();
			System.out.println(tmp.toJson(nodes));
			Platform.runLater(new Runnable() {
				public void run() {
				//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
					startSim.setDisable(false);
					}
		});
		
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
			System.out.println(procNames);
			for (int j = 0; j < procNames.size(); j++) {
				
				processes tempProcs = new processes();
				tempProcs.name = procNames.get(j);
				tempProcs.paraNum = tempProcs.paraNum +1;
				List< taskDetail> tempNodeContent = new ArrayList();
				tempNodeContent = taskDiagObj.getTaskForProcess(tempProcs.name);
				int k;
				for (k = 0; k < tempNodeContent.size(); k++) {
					task tempTask = new task();
					if (tempNodeContent.get(k).from.equals(tempProcs.name))
						tempTask.taskType = taskType.SEND;
					else
						tempTask.taskType = taskType.RECV;
					tempTask.from = tempNodeContent.get(k).from;
					tempTask.to = tempNodeContent.get(k).to;
					tempTask.msg = String.join(" ", tempNodeContent.get(k).msg);
					tempTask.paraID=tempNodeContent.get(k).parID;
					tempProcs.tasks.add(tempTask);
				}
				tempProcs.paraNum = tempProcs.tasks.get(k-1).paraID;
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
						   updateNodeInDepDiag(nodes.get(i).name);
							//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
								for(int k=0;k<nodes.get(i).procs.size();k++) {
									updateProcInTaskDiag(nodes.get(i).procs.get(k).name);
									updateProcInDepDiag(nodes.get(i).procs.get(k).name);
								}
				                
								
				
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
			
			
			Scene scene = new Scene(root, 800,600);
			
			   
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
		
		//classDiagGC.setFill(Color.web("#FDEBDD"));
		classDiagGC.setFill(Color.web("#EDEDED"));
		classDiagGC.setStroke(Color.DARKRED);
		//classDiagGC.setStroke(Color.web("#C06014"));
		classDiagGC.setLineWidth(3);
		classDiagGC.fillRect(0, 0, classDiagGC.getCanvas().getWidth(), classDiagGC.getCanvas().getHeight());
		classDiagGC.setFill(Color.web("#000000"));
		//classDiagGC.setFill(Color.FIREBRICK);
		classDiagGC.setFont(new Font("Verdana",20.0));
		classDiagGC.fillText("Distributed System Messaging : Class Diagram", 10, 20);
		classDiagGC.setFont(new Font("Tahoma",12.0));
		int numberOfRootNodes = clsdia.size();
		double xOffset = 10;
		double yOffset = 40;
		double cellXSize = (classDiagGC.getCanvas().getWidth()/numberOfRootNodes)- xOffset -10;
		double cellYSize = classDiagGC.getCanvas().getHeight() - 50;
		for(int i=0;i<numberOfRootNodes;i++) {
			classDiagGC.strokeRect(xOffset, yOffset, cellXSize,cellYSize);
			classDiagGC.setFill(Color.MIDNIGHTBLUE);
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
			
			
			Scene scene = new Scene(root, 600,700);
			
			   
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
		depDiagGC.setFill(Color.web("#EDEDED"));
		
		depDiagGC.setStroke(Color.DARKRED);
		depDiagGC.setLineWidth(2);
		depDiagGC.fillRect(0, 0, depDiagGC.getCanvas().getWidth(), depDiagGC.getCanvas().getHeight());
		depDiagGC.setFill(Color.BLACK);
		depDiagGC.fillText("Distributed System Messaging : Deployment Diagram", 10, 20);
		depDiagGC.setFill(Color.web("#EDEDED"));
		int numberOfNodes = devices.size();
		double xOffset = 10;
		double yOffset = 40;
		double cellXSize = depDiagGC.getCanvas().getWidth() - 50;
		double cellYSize =(depDiagGC.getCanvas().getHeight()/numberOfNodes)- yOffset -10;
		for(int i=0;i<devices.size();i++) {
	
			//update node Box
			imageRef tempProcLine= new imageRef();
			tempProcLine.lineXLocation =0;
			tempProcLine.processName = 	devices.get(i);	
			tempProcLine.x = xOffset;
			tempProcLine.y= yOffset;
			tempProcLine.height=cellYSize;
			tempProcLine.width=cellXSize;
			nodeImageBox.add(tempProcLine);
			
			
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
				imageRef tempNodeProc= new imageRef();
				tempNodeProc.lineXLocation =0;
				tempNodeProc.processName = procsInNode.get(j);	
				tempNodeProc.x = nodeXoffset;
				tempNodeProc.y= nodeYoffset;
				tempNodeProc.height=nodeHeight;
				tempNodeProc.width=nodeWidth;
				nodeImageProcBox.add(tempNodeProc);
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
			
			
			Scene scene = new Scene(root, 1024,600);
			
			   
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
		System.out.println("Number of parallel simulations :"+taskd.diagram.content.length);
	
		int numParSim = taskd.diagram.content.length;
		presentationLayer.x=20;
		presentationLayer.y=height+YOffset + 10;
		presentationLayer.height=taskDiagGC.getCanvas().getHeight()-presentationLayer.y;
		presentationLayer.width= taskDiagGC.getCanvas().getWidth() - (2*presentationLayer.x);
		taskDiagGC.strokeRoundRect(presentationLayer.x, presentationLayer.y,presentationLayer.width,presentationLayer.height-20, 20, 20);
		paraLayer = parallelDispLayers(presentationLayer,numParSim);
		taskDiagGC.strokeRoundRect(presentationLayer.x,presentationLayer.y, 50, 20, 20, 10);
		taskDiagGC.setFill(Color.BLACK);
		taskDiagGC.fillText("Par",presentationLayer.x+10,presentationLayer.y+15);
	
			double dashes[] = {5.0,20.0};
			taskDiagGC.setLineDashes(dashes);
			taskDiagGC.setLineWidth(0.7);
		for(int i=0;i<paraLayer.size();i++) {
			System.out.println(paraLayer.get(i).x+":"+paraLayer.get(i).y);
			taskDiagGC.strokeRect(paraLayer.get(i).x, paraLayer.get(i).y, paraLayer.get(i).width,paraLayer.get(i).height);
		}
		
		taskDiagGC.setLineDashes(null);	
		for(int i=1;i<=taskd.processes.length;i++) {
			taskDiagGC.setLineWidth(2);
			double Xoffset= (((taskDiagGC.getCanvas().getWidth()/taskd.processes.length)*i )- (taskDiagGC.getCanvas().getWidth()/(2*taskd.processes.length))) - (width/2);
			taskDiagGC.strokeRect(Xoffset, YOffset, width, height);
			taskDiagGC.setLineWidth(0.5);
			taskDiagGC.strokeLine(Xoffset+(width/2), YOffset+height, Xoffset+(width/2), taskDiagCanvas.getHeight());
			taskDiagGC.setFill(Color.BLACK);
			taskDiagGC.fillText(taskd.processes[i-1].name+ ":" + taskd.processes[i-1].classes, Xoffset+width/5, YOffset+(height/2));
			classDiagGC.setFill(Color.web("#EDEDED"));
			imageRef tempProcLine= new imageRef();
			tempProcLine.lineXLocation =Xoffset+(width/2);
			tempProcLine.processName = 	taskd.processes[i-1].name;	
			tempProcLine.x = Xoffset;
			tempProcLine.y= YOffset;
			tempProcLine.height=height;
			tempProcLine.width=width;
			procLine.add(tempProcLine);
		}
		//Font font = new Font(12);
		//System.out.println(Font.getFontNames());
		firstArrowYOffset= (YOffset+height)+taskDiagGC.getCanvas().getHeight()/8;
		
	}
	public void renderBackground() {
		taskDiagGC.setFill(Color.web("#EDEDED"));
		taskDiagGC.setStroke(Color.DARKRED);
		taskDiagGC.setLineWidth(2);
		taskDiagGC.fillRect(0, 0, taskDiagGC.getCanvas().getWidth(), taskDiagGC.getCanvas().getHeight());
		taskDiagGC.setFill(Color.BLACK);
		taskDiagGC.fillText("Distributed System Messaging : Task Sequence Diagram", 10, 20,200);
		taskDiagGC.setFill(Color.web("#EDEDED"));
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
	public void drawArrow (taskDetail tmpTask) {
		double fromX=0;
		double toX=0;
		java.util.Date date= new java.util.Date();
		Timestamp T = new Timestamp(date.getTime());
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
		double Y=0;
		int totalTasksinSim = taskDiagObj.diagram.content[tmpTask.parID - 1].content.length;
				
	//	for(i=0;i<nodes.size())
		if(paraLayer.get(tmpTask.parID-1).currentYLoc==0) {
			Y = paraLayer.get(tmpTask.parID-1).y +20;
			paraLayer.get(tmpTask.parID-1).currentYLoc = Y;
		}
		else {
			Y= paraLayer.get(tmpTask.parID-1).currentYLoc + paraLayer.get(tmpTask.parID-1).height/totalTasksinSim;
			paraLayer.get(tmpTask.parID-1).currentYLoc =Y;
		}
		
		
		
		taskDiagGC.setStroke(Color.DARKBLUE);
		taskDiagGC.setLineWidth(0.85);
		taskDiagGC.strokeLine(fromX, Y, toX, Y);
		taskDiagGC.setFont(Font.font(10));
		String msg = new String(tmpTask.msg[0]);
	
		for(int i=1;i<tmpTask.msg.length;i++)
			msg = msg + tmpTask.msg[i];
		msg = msg +"  at : "+T.toString();
		
		if(toX>fromX) {
			taskDiagGC.strokeText(msg,fromX+ (toX-fromX)/4, Y-3);
			taskDiagGC.strokeLine(toX, Y,toX-6,Y-6);
			taskDiagGC.strokeLine(toX, Y,toX-6,Y+6);
		}
		else {
			taskDiagGC.strokeText(msg,toX+ (fromX-toX) /4, Y-3);
			taskDiagGC.strokeLine(toX, Y,toX+6,Y-6);
			taskDiagGC.strokeLine(toX, Y,toX+6,Y+6);
		}
		
	}

	public void updateProcInTaskDiag(String ProcName)
	{
		
		for(int i=0;i<procLine.size();i++) {
			if(procLine.get(i).processName.equals(ProcName)) 
			{
				System.out.println("getting process :"+ProcName);
				double x =procLine.get(i).x;
				double y = procLine.get(i).y;
				double width =procLine.get(i).width;
				double height = procLine.get(i).height;
				
				Platform.runLater(new Runnable() {
					public void run() {
					//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
						System.out.println("Ok am here :"+x+y+height+width);
						taskDiagGC.setStroke(Color.SEAGREEN);
						taskDiagGC.setLineWidth(5);
						taskDiagGC.strokeRect(x+1, y+1, width-1, height-1);
						}
			});
				
			}
		
		}
		
		
	}

	public List<imageRef> parallelDispLayers(imageRef source,int paraNum) {
		List<imageRef> parallelLayer = new ArrayList();
		
		double heightOfLayer = source.height/paraNum;
		for(int i=0;i<paraNum;i++) {
			imageRef tempParaLayer = new imageRef();
			tempParaLayer.x=source.x;
			tempParaLayer.y=i*heightOfLayer+source.y;
			tempParaLayer.height = heightOfLayer;
			tempParaLayer.width=source.width;
			parallelLayer.add(tempParaLayer);
		}
		return parallelLayer;
	}
	
	
	public void updateNodeInDepDiag(String nodeName) {
		for(int i=0;i<nodeImageBox.size();i++) {
			if(nodeImageBox.get(i).processName.equals(nodeName)) 
			{
				System.out.println("getting process :"+nodeName);
				double x =nodeImageBox.get(i).x;
				double y = nodeImageBox.get(i).y;
				double width =nodeImageBox.get(i).width;
				double height = nodeImageBox.get(i).height;
				
				Platform.runLater(new Runnable() {
					public void run() {
					//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
						System.out.println("Ok am here :"+x+y+height+width);
						depDiagGC.setStroke(Color.SEAGREEN);
						depDiagGC.setLineWidth(5);
						depDiagGC.strokeRect(x+1, y+1, width-1, height-1);
						}
			});
				
			}
	}
	}
	public void updateProcInDepDiag(String nodeName) {
		System.out.println("getting process :"+nodeName);
		for(int i=0;i<nodeImageProcBox.size();i++) {
			
			if(nodeImageProcBox.get(i).processName.equals(nodeName)) 
			{
				
				double x =nodeImageProcBox.get(i).x;
				double y = nodeImageProcBox.get(i).y;
				double width =nodeImageProcBox.get(i).width;
				double height = nodeImageProcBox.get(i).height;
				
				Platform.runLater(new Runnable() {
					public void run() {
					//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
						System.out.println("Ok am here :"+x+y+height+width);
						depDiagGC.setStroke(Color.SEAGREEN);
						depDiagGC.setLineWidth(5);
						depDiagGC.strokeRect(x+1, y+1, width-1, height-1);
						}
			});
				
			}
	}
	}
	public void startSimulation()
	{
		allProcessUp=true;
		beginSimulation=true;
		startSim.setDisable(true);
		
	}
}

