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
	public List<nodeContent> getTaskForProcess(String process) {
		List<nodeContent> taskList = new ArrayList();
		for(int i=0;i<diagram.content.length;i++) {
			
			for(int j=0;j<diagram.content[i].content.length;j++) {
				if(diagram.content[i].content[j].from.equals( process) ||diagram.content[i].content[j].to.equals(process))
					taskList.add(diagram.content[i].content[j]);
			}
		}
			
		return taskList;
	}
	

}
