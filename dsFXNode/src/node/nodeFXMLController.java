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
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import node.task.taskType;

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

	public nodeReference node = new nodeReference();

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

		sequenceControllerTask();

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

}
