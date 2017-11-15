package node;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import node.task.taskType;
final class processLine {
	public String processName;
	public double lineXLocation;
}

public class nodeFXMLController {
	@FXML
	protected Button startButton;
	@FXML
	protected TextField nodeName;
	@FXML
	protected TextArea statusBox;
	Thread nodeControlThread;
	Thread sequenceControlThread;
	public int serverUpdatePort;
	public String controllerIP;
	public boolean startSimulation = false;
	List<processLine> processX = new ArrayList();
	
	public GraphicsContext nodeDiagGC;
	public Canvas nodeDiagCanvas = new Canvas(800,500);
	
	Stage nodeDiagStg = new Stage();

	public nodeReference node = new nodeReference();
	double firstArrowYOffset;
	public int arrowsDrawn =0;

	@FXML
	protected void startNode() {
		nodeName.setDisable(true);
		nodeControllerTask();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Gson gsonObj = new Gson();
		statusBox.setText("Connected to controller and receieved tasks..\n");
		String testString = gsonObj.toJson(node);
		statusBox.setText(statusBox.getText() + testString + "\n");
		
		Pane root = new Pane();
		
		
		nodeDiagGC = nodeDiagCanvas.getGraphicsContext2D();
	//	classDiagGC.fillText("Distributed System Messaging", 10, 20);
		
		
		Scene scene = new Scene(root, 800,460);
		
		   
		nodeDiagStg.setTitle("Task Sequence Diagram");
  
		nodeDiagStg.setScene(scene);
		nodeDiagStg.setX(340);
		
		nodeDiagStg.setY(220);
		
		root.getChildren().add(nodeDiagCanvas);
		nodeDiagStg.show();
		drawNode();
		
		sequenceControllerTask();

	}
	public void drawNode() {
		nodeDiagGC.strokeRect(10, 10, nodeDiagGC.getCanvas().getWidth()-10, nodeDiagGC.getCanvas().getHeight()-10);
		double width = nodeDiagGC.getCanvas().getWidth()/(2*node.procs.size());
		double height = width *3/4;
		double YOffset = height/2;
		for(int i=1;i<=node.procs.size();i++) {
			nodeDiagGC.setLineWidth(2);
			double Xoffset= (((nodeDiagGC.getCanvas().getWidth()/node.procs.size())*i )- (nodeDiagGC.getCanvas().getWidth()/(2*node.procs.size()))) - (width/2);
			nodeDiagGC.strokeRect(Xoffset, YOffset, width, height);
			nodeDiagGC.setLineWidth(0.5);
			nodeDiagGC.strokeLine(Xoffset+(width/2), YOffset+height, Xoffset+(width/2), nodeDiagGC.getCanvas().getHeight());
			nodeDiagGC.setFill(Color.BLACK);
			nodeDiagGC.fillText(node.procs.get(i-1).name, Xoffset+width/5, YOffset+(height/2));
			nodeDiagGC.setFill(Color.MOCCASIN);
			processLine tempProcLine= new processLine();
			tempProcLine.lineXLocation =Xoffset+(width/2);
			tempProcLine.processName = 	node.procs.get(i-1).name;	
			processX.add(tempProcLine);
		}
		//Font font = new Font(12);
		//System.out.println(Font.getFontNames());
		firstArrowYOffset= (YOffset+height)+nodeDiagGC.getCanvas().getHeight()/8;
		
		
	}

	public void sequenceControllerTask() {
		Runnable seqControlTask = new Runnable() {
			public void run() {
				try {
					sequenceControlRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		sequenceControlThread = new Thread(seqControlTask);
		sequenceControlThread.setDaemon(true);
		sequenceControlThread.start();
	}

	public void sequenceControlRun() throws InterruptedException {
		for (int i = 0; i < node.procs.size(); i++) {
			procRun(node.procs.get(i), i);
		}
	}

	public void procRun(processes proc, int procIndex) {
		Runnable procControlTask = new Runnable() {
			public void run() {
				try {
					procThreadRun(proc);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};

		Thread procThread = new Thread(procControlTask);
		procThread.setDaemon(true);
		procThread.start();
	}

	public void procThreadRun(processes proc) throws InterruptedException {
		System.out.println("\nthread for  :" + proc.name);
		System.out.println("\n I have :" + proc.tasks.size() + " tasks\n");

		ServerSocket processListenSocket;

		try {
			processListenSocket = new ServerSocket(0);
			System.out.println("I am listening at :" + processListenSocket.getLocalPort());
			Socket soc = new Socket(controllerIP, serverUpdatePort);
			DataOutputStream updateStream = new DataOutputStream(soc.getOutputStream());
			updateStream.writeUTF(proc.name + "," + Integer.toString(processListenSocket.getLocalPort()));
			soc.close();

			Socket sockReadStart=processListenSocket.accept();
			DataInputStream din = new DataInputStream(sockReadStart.getInputStream());
			String startmsg = din.readUTF();
		//	System.out.println(startmsg);
			if (startmsg.contains("start")) {
				for (int i = 0; i < proc.tasks.size(); i++) {
					if (proc.tasks.get(i).taskType == taskType.SEND)
						processSendTask(proc.tasks.get(i));
					else {
						Socket sock = processListenSocket.accept();
						DataInputStream in = new DataInputStream(sock.getInputStream());
						String msg = in.readUTF();
						System.out.println(msg);
					}
				
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void processSendTask(task currentTask) {

		String toIp = resolvProcess(currentTask);
		List<String> items = Arrays.asList(toIp.split("\\s*,\\s*"));
		int destPort = Integer.valueOf(items.get(1).replaceAll("[^\\d.]", ""));
		String Ip = items.get(0);
		try {
			Socket soc = new Socket(Ip, destPort);
			DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
			dout.writeUTF(currentTask.msg);
			soc.close();
			
			Platform.runLater(new Runnable() {
				public void run() {
				//	System.out.println(tmpTask.from +" "+tmpTask.to+" " +tmpTask.msg );
					
	                drawArrow(currentTask);
					}
		});
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public String resolvProcess(task currentTask) {
		try {
			
			Socket soc = new Socket(controllerIP, serverUpdatePort);
			DataOutputStream req = new DataOutputStream(soc.getOutputStream());
			Gson taskGson = new Gson();
			String proc = taskGson.toJson(currentTask);
			req.writeUTF(proc);
			DataInputStream resp = new DataInputStream(soc.getInputStream());
			String resolved = resp.readUTF();
			soc.close();
			return resolved;
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	

	public void nodeControllerTask() {
		Runnable clientControlTask = new Runnable() {
			public void run() {
				try {
					nodeControlRun();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		nodeControlThread = new Thread(clientControlTask);
		nodeControlThread.setDaemon(true);
		nodeControlThread.start();
	}

	public void nodeControlRun() throws InterruptedException {
		try {
			DatagramSocket broadcastListenSocket;

			try {
				broadcastListenSocket = new DatagramSocket(8880);
				byte[] receiveData = new byte[1024];
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					broadcastListenSocket.receive(receivePacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String sentence = new String(receivePacket.getData());
				System.out.println(sentence);
				InetAddress IPAddress = receivePacket.getAddress();
				broadcastListenSocket.close();
				List<String> items = Arrays.asList(sentence.split("\\s*,\\s*"));
				serverUpdatePort = Integer.valueOf(items.get(1).replaceAll("[^\\d.]", ""));
				controllerIP = IPAddress.getHostAddress();
				Socket soc = new Socket(IPAddress.getHostAddress(),
						Integer.valueOf(items.get(0).replaceAll("[^\\d.]", "")));
				DataOutputStream dout = new DataOutputStream(soc.getOutputStream());
				dout.writeUTF(nodeName.getText());
				DataInputStream in = new DataInputStream(soc.getInputStream());
				String str = (String) in.readUTF();
				System.out.println(str);
				dout.writeUTF("ok");
				dout.flush();
				dout.close();
				in.close();
				soc.close();
				Gson gsonObj = new Gson();
				node = gsonObj.fromJson(str, node.getClass());
				String testString = gsonObj.toJson(node);
				System.out.println(testString);

			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException i) {
			System.out.println(i);
		}

	}

	public void drawArrow (task tmpTask) {
		double fromX=0;
		double toX=0;
		boolean toSet = false;
		for(int i=0;i<processX.size();i++) {
			
			if(processX.get(i).processName.equals(tmpTask.from)) 
			{
				fromX = processX.get(i).lineXLocation;
				
			}
			if(processX.get(i).processName.equals(tmpTask.to))
			{
				toX=processX.get(i).lineXLocation;
				toSet = true;
			}
		}
		if(toSet == false) {
			toX=nodeDiagGC.getCanvas().getWidth()-10;
		}
		
		double Y = firstArrowYOffset + (arrowsDrawn*((nodeDiagGC.getCanvas().getHeight()-firstArrowYOffset)/totalSendTasks()));
		arrowsDrawn++;
		
		nodeDiagGC.setStroke(Color.DARKBLUE);
		nodeDiagGC.setLineWidth(0.85);
		nodeDiagGC.strokeLine(fromX, Y, toX, Y);
		nodeDiagGC.setFont(Font.font(10));
		nodeDiagGC.strokeText(tmpTask.msg, (fromX+(toX-fromX)/4), Y-3);
		if(toX>fromX) {
			nodeDiagGC.strokeLine(toX, Y,toX-6,Y-6);
			nodeDiagGC.strokeLine(toX, Y,toX-6,Y+6);
		}
		else {
			nodeDiagGC.strokeLine(toX, Y,toX+6,Y-6);
			nodeDiagGC.strokeLine(toX, Y,toX+6,Y+6);
		}
		
	}
	public int totalSendTasks() {
		int totSendTask=0;
			for(int j=0;j<node.procs.size();j++) {
				for(int k=0;k<node.procs.get(j).tasks.size();k++)
				if (node.procs.get(j).tasks.get(k).taskType == taskType.SEND)
					totSendTask++;
	
			}
		
		return totSendTask;
	}
}
