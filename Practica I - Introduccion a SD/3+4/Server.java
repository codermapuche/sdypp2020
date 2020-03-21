package sdypp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class Server {
  
  // Behavior
	private final static int port = 8080;
	private final static int limit = Integer.MAX_VALUE;
	
  private final static Hashtable<String, ArrayList<String>> messages = new Hashtable<String, ArrayList<String>>();

  public static void main( String[] args ) {  
    int counter = 0;
    
    try {
      ServerSocket server = new ServerSocket(port);
      System.out.println("Server listen on " + port);
  	
      while (counter < limit) {
        counter++;
        Socket cli = server.accept();
  		  System.out.println("Client accepted " + cli.getRemoteSocketAddress());
  	    
  		  Worker wrk = new Worker(cli, messages);
  		  Thread tsk = new Thread(wrk);
    		tsk.start();
    	}	
  	
      server.close();
      System.out.println("Server closed.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
