package application;

import java.util.ArrayList;
import java.util.List;

final class map {
	public String process;
	public String device;
	public map() {}
}

public class JSONDepDiag {
	public metadata meta;
	public String type;
	public List<map> mapping ;//= new mapping();
	public JSONDepDiag() {}
	
	public List<String> getProcessForNodes(String physicalName) {
		
		List<String> processes = new ArrayList();
		for(int i=0;i<mapping.size();i++)  {
			
			if(mapping.get(i).device.equals(physicalName)) {
		
				processes.add(mapping.get(i).process);
			}
		}
		
		return processes;
	}
	
	public List<String> getPhysicalDevices() {
		
		int len =mapping.size();
		List<String> devices = new ArrayList();
	
		for(int i=0;i<len;i++) {
			if(devices.contains(mapping.get(i).device)) {
				continue;
			}else {
				devices.add(mapping.get(i).device);
			}
		}
		return devices;
	}

}
