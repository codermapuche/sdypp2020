package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Writer {
  
  // Server
  private final static String addr = "127.0.0.1";
	private final static int port = 8080;
	
  // Behavior
  private final static int counter = 3;
	  	
  public static void main( String[] args ) {
    int msgid = 0;
    
    try {
			Socket cli = new Socket(addr, port);
			
			BufferedReader in  = new BufferedReader(new InputStreamReader(cli.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));
			
			while (msgid < counter) {
			  msgid++;
	      out.write("write@bob@hello bob! I alice, how are you? This is my message #" + msgid + ".\n");		
        out.flush();
        
	      String msg = in.readLine();
	      String[] cmd = msg.split("@");
	      
	      switch (cmd[0]) {
	        case "OK":
	          // Nothing to-do
	        break;
	        
	        default:
	          System.out.println("Error writing message #" + msgid + " (" + cmd[1] + ") " + cmd[0]);   
	        break;
	      }  
			}				
			
			cli.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
  }
}