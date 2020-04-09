package sdypp;

import java.rmi.Remote;

public interface Tarea extends Remote {
	public void run() throws java.rmi.RemoteException;
}