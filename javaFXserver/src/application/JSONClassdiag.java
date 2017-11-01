package application;
final class metadata {
	public String format;
	public String version;
	public String extentions[];
	public metadata() {};
}
final class DevClass {
	public String name;
	public String Fields[];
	public DevClass() {};
}
final class relation {
	public String type;
	public String superclass;
	public String subclass;
	public relation() {};
}

public class JSONClassdiag {
	private metadata meta;
	private String type;
	private DevClass classes[];
	private relation relationship[];
	public JSONClassdiag() {};	
	@Override
	public String toString() {
		return "Meta Data : [" + meta.format+"," + meta.version+  "]" +"\nJSON Type :" + type  ; 
	}
}