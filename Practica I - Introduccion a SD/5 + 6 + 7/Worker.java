package sdypp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class Worker extends UnicastRemoteObject implements RemoteWorker {

  private static final long serialVersionUID = 1L;
  
  protected Worker() throws RemoteException {
    super();
  }
  
	public String getClima() throws java.rmi.RemoteException {
	  return "Soleado";
	}
	
}