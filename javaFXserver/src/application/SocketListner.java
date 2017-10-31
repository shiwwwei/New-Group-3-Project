package application;

public interface SocketListner {
	 public void onReceive(FileData fileReference);
	 public void onClosedStatus(boolean isClosed);
}
