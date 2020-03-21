package sdypp;

import java.rmi.Remote;

public interface RemoteWorker extends Remote {
	public String getClima () throws java.rmi.RemoteException;
}