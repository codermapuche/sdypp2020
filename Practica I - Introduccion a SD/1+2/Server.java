package sdypp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
  
  // Behavior
	private final static int port = 8080;
	private final static int limit = Integer.MAX_VALUE;
	
  public static void main( String[] args ) {  
    int counter = 0;
    
    try {
      ServerSocket server = new ServerSocket(port);
      System.out.println("Server listen on " + port);
  	
      while (counter < limit) {
        counter++;
        Socket cli = server.accept();
  		  System.out.println("Client accepted " + cli.getRemoteSocketAddress());
  		
  		  Worker wrk = new Worker(cli, counter);
  		  Thread tsk = new Thread(wrk);
    		tsk.start();
    	}	
  	
      server.close();
      System.out.println("Server closed.");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
