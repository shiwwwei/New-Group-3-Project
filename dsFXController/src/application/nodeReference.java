package application;

import java.util.ArrayList;
import java.util.List;
final class task {
	
	public enum taskType { SEND,RECV }
	public taskType taskType;
	public String from;
	public String to;
	public String msg;
	public task() {}
	
}
final class processes {
	public String name;
	public List<task> tasks = new ArrayList();
	int recvPort;
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
