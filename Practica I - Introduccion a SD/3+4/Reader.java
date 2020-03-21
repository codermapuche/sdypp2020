package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Reader {
  
  // Server
  private final static String addr = "127.0.0.1";
	private final static int port = 8080;
	
  // Behavior
  private final static String user = "bob";
  	
  public static void main( String[] args ) {    
    try {
      Socket cli = new Socket(addr, port);
      
      BufferedReader in  = new BufferedReader(new InputStreamReader(cli.getInputStream()));
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));
      
      out.write("read@" + user + "\n");   
      out.flush();

      String msg = in.readLine(); 
      String[] cmd;    
      
      while (msg != null) {
        cmd = msg.split("@");
        
        switch (cmd[0]) {        
          case "MSG":
            System.out.println("Read message: " + cmd[1]);            
            msg = in.readLine();
          break;
          
          case "END":  
            out.write("clean@" + user + "\n");   
            out.flush();       
            msg = in.readLine();
          break;
          
          case "OK":      
            System.out.println("All messages readed and cleaned.");
            msg = null;
          break;
          
          default:  
            System.out.println("Error reading messages.");
            msg = null;
          break;          
        }     
      }       
      
      cli.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}