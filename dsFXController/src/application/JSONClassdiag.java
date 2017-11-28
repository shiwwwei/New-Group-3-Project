package application;

import java.util.ArrayList;
import java.util.List;

final class metadata {
	public String format;
	public String version;
	public List <String> extentions = new ArrayList();
	public metadata() {};
}
final class field {
	public String name;
	public String type;
}
final class DevClass {
	public String name;
	public List <field> fields = new ArrayList();
	public DevClass() {};
}
final class relation {
	public String type;
	public String superclass;
	public String subclass;
	public relation() {};
}

public class JSONClassdiag {
	public metadata meta;
	public String type;
	public List <DevClass> classes = new ArrayList();
	public List <relation> relationships = new ArrayList();
	public JSONClassdiag() {};	
	@Override
	public String toString() {
		return "Meta Data : [" + meta.format+"," + meta.version+  "]" +"\nJSON Type :" + type  ; 
	}
	public List<classTree> parse() {
		List <classTree> ClassTree = new ArrayList();
		int nodesInTree=-1;
		for(int i=0;i<classes.size();i++) {
			boolean isRoot = true;
			for(int j=0;j<relationships.size();j++) {
				if(classes.get(i).name.equals(relationships.get(j).subclass)) {
					isRoot = false;
					if(nodesInTree >=0) {
						classTree temp = new classTree();
						temp.name=classes.get(i).name;
						temp.fields = classes.get(i).fields;
						
						for(int k=0;k<ClassTree.size();k++)
						{
							if(ClassTree.get(k).name.equals(relationships.get(j).superclass)) {
								ClassTree.get(k).nodes.add(temp);
								System.out.println("added a node");
								
							}
							
						}
					}
				}
			}
			if(isRoot) {
				classTree temp = new classTree();
				temp.name=classes.get(i).name;
				temp.fields = classes.get(i).fields;
				temp.nodes = new ArrayList();
				ClassTree.add(temp);
				nodesInTree+=1;
			}
			
		}
		return ClassTree;
	}
}