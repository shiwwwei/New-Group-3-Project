package application;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

public class broadcastIntent {

	private static DatagramSocket socket = null;
	static List<InetAddress> inetAddrs = new ArrayList<>();
	public boolean broadcastNow;
	
	public broadcastIntent() {
		broadcastNow = true;
	}
	
	public  void sendBroadcast(String message) {
		if(broadcastNow) { 
		try {
			inetAddrs = listAllBroadcastAddresses();
			for(int i=0;i<inetAddrs.size();i++) {
				broadcast(message,inetAddrs.get(i));	
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Broadcast Failure....\n");
			e.printStackTrace();
		}
		}
		
	}

	

	public static void broadcast(String broadcastMessage, InetAddress address) throws IOException {
		socket = new DatagramSocket();
		socket.setBroadcast(true);


		byte[] buffer = broadcastMessage.getBytes();

		DatagramPacket packet = new DatagramPacket(buffer, buffer.length,address, 8880);
//		System.out.println("Broadcast message to " + address.getHostAddress() );
		socket.send(packet);
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket.close();
	}

	static List<InetAddress> listAllBroadcastAddresses() throws SocketException {
		List<InetAddress> broadcastList = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface networkInterface = interfaces.nextElement();

			if (networkInterface.isLoopback() || !networkInterface.isUp()) {
				continue;
			}

			networkInterface.getInterfaceAddresses().stream().map(a -> a.getBroadcast()).filter(Objects::nonNull)
					.forEach(broadcastList::add);
		}
		return broadcastList;
	}

}
