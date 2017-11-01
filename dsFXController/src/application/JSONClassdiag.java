package application;
final class metadata {
	public String format;
	public String version;
	public String extentions[];
	public metadata() {};
}
final class field {
	public String name;
	public String type;
}
final class DevClass {
	public String name;
	public field fields[];
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
	private relation relationships[];
	public JSONClassdiag() {};	
	@Override
	public String toString() {
		return "Meta Data : [" + meta.format+"," + meta.version+  "]" +"\nJSON Type :" + type  ; 
	}
}