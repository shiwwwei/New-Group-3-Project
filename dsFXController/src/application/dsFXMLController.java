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
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
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
	public Canvas canvas;
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
	public GraphicsContext gc ;
	public List<processLine> procLine = new ArrayList();
	public int arrowsDrawn =0;
	public double firstArrowYOffset =0;
	

	
	

	public void initialize() {
		gc =canvas.getGraphicsContext2D();
		System.out.println("graphics context obtained");
		renderBackground();
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
			drawProcess(taskDiagObj);
		}
		//Rectangle rect = new Rectangle(canvas.getLayoutX(), canvas.getLayoutY(), canvas.getLayoutX()+canvas.getWidth(), canvas.getHeight());
		
		
	}
	public void drawProcess(JSONTaskDiag taskd)
	{
		double width = gc.getCanvas().getWidth()/(2*taskd.processes.length);
		double height = width *3/4;
		double YOffset = height/2;
		for(int i=1;i<=taskd.processes.length;i++) {
			gc.setLineWidth(2);
			double Xoffset= (((gc.getCanvas().getWidth()/taskd.processes.length)*i )- (gc.getCanvas().getWidth()/(2*taskd.processes.length))) - (width/2);
			gc.strokeRect(Xoffset, YOffset, width, height);
			gc.setLineWidth(0.5);
			gc.strokeLine(Xoffset+(width/2), YOffset+height, Xoffset+(width/2), canvas.getHeight());
			gc.setFill(Color.BLACK);
			gc.fillText(taskd.processes[i-1].name+ ":" + taskd.processes[i-1].classes, Xoffset+width/5, YOffset+(height/2));
			gc.setFill(Color.MOCCASIN);
			processLine tempProcLine= new processLine();
			tempProcLine.lineXLocation =Xoffset+(width/2);
			tempProcLine.processName = 	taskd.processes[i-1].name;	
			procLine.add(tempProcLine);
		}
		//Font font = new Font(12);
		//System.out.println(Font.getFontNames());
		firstArrowYOffset= (YOffset+height)+gc.getCanvas().getHeight()/8;
		
	}
	public void renderBackground() {
		gc.setFill(Color.MOCCASIN);
		gc.setStroke(Color.DARKRED);
		gc.setLineWidth(2);
		gc.strokeRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
		gc.setFill(Color.BLACK);
		gc.fillText("Distributed System Messaging", 10, 20,200);
		gc.setFill(Color.MOCCASIN);
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
		System.out.println("Draw Arrow for :");
		for(int i=0;i<procLine.size();i++) {
			if(procLine.get(i).processName.equals(tmpTask.from)) 
			{
				fromX = procLine.get(i).lineXLocation;
				System.out.println(tmpTask.from );
			}
			if(procLine.get(i).processName.equals(tmpTask.to))
			{
				toX=procLine.get(i).lineXLocation;
				System.out.println(tmpTask.to );
			}
		}
		
		double Y = firstArrowYOffset + (arrowsDrawn*((gc.getCanvas().getHeight()-firstArrowYOffset)/totalSendTasks()));
		arrowsDrawn++;
		gc.setStroke(Color.DARKBLUE);
		gc.setLineWidth(0.85);
		gc.strokeLine(fromX, Y, toX, Y);
		gc.setFont(Font.font(10));
		gc.strokeText(tmpTask.msg, (fromX+(toX-fromX)/4), Y-3);
		gc.strokeOval(toX, Y,6,4);
	}
}

