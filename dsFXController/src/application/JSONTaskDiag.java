package application;

import java.util.ArrayList;
import java.util.List;

final class procs {
	public String classes;
	public String name;
}
final class nodeContent {
	public String node;
	public String from;
	public String to;
	public String message[];
}
final class dContent {
	public String node;
	public nodeContent content[];
}
final class diag{
	public String node;
	public dContent content[];
}


public class JSONTaskDiag {
	public metadata meta;
	public String type;
	public procs processes[];
	public diag diagram;
	
	public JSONTaskDiag() {}
	public List<taskDetail> getTaskForProcess(String process) {
		List<taskDetail> taskList = new ArrayList();
		taskDetail temp = new taskDetail();
		for(int i=0;i<diagram.content.length;i++) {
			
			for(int j=0;j<diagram.content[i].content.length;j++) {
				
				if(diagram.content[i].content[j].from.equals( process) ||diagram.content[i].content[j].to.equals(process)) {
				//	taskDetail temp = new taskDetail();;
					temp = new taskDetail();
				temp.from = diagram.content[i].content[j].from;
				temp.to = diagram.content[i].content[j].to;
				temp.node = diagram.content[i].content[j].node;
				temp.msg = diagram.content[i].content[j].message;
				temp.parID = i+1;
				
					taskList.add(temp);
					temp = new taskDetail();
				}
			}
		}
			
		return taskList;
	}
	

}
