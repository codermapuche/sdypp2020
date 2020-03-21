package sdypp;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
  
  // Behavior
  private final static int port = 12000;
	
  public static void main( String[] args ) {
         
    try {
      Registry registry = LocateRegistry.createRegistry(port);  
      RemoteWorker worker = new Worker();
      registry.rebind("worker", worker);
      System.out.println("Server RMI ready on port: " + port);
    } catch (RemoteException e) {
      e.printStackTrace();
    }   
    
  }
}
