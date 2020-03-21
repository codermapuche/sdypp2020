package sdypp;

import java.rmi.Remote;

public interface Task extends Remote {
	public void run() throws java.rmi.RemoteException;
}