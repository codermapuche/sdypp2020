package sdypp;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
  
  // Behavior
  private final static String addr = "127.0.0.1";
  private final static int port = 12000;
    
  public static void main(String[] args) {
    try {
      Registry registry   = LocateRegistry.getRegistry(addr, port);
      System.out.println("Successfully connected to " + addr + ":" + port);
      
      RemoteWorker server = (RemoteWorker) registry.lookup("worker");            
      System.out.println("Clima del server: " + server.getClima());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
    
}
