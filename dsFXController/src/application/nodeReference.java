package application;

import java.util.ArrayList;
import java.util.List;
final class task {
	
	public enum taskType { SEND,RECV }
	public taskType taskType;
	public String from;
	public String to;
	public String msg;
	public int paraID;
	public task() {}
	
}
final class portData {
	public int port;
	public int paraID;
}
final class processes {
	public String name;
	public List<task> tasks = new ArrayList();
	public List<portData> recvPort = new ArrayList();
	public int paraNum = 0;
	
	public processes() {
		
	}
}

public class nodeReference {
	public String name;
	public List<processes> procs = new ArrayList();
	public String ipAddress;
	
	public nodeReference() {
		
	}

}
