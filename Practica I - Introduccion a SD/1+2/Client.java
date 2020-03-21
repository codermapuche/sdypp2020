package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
  
  // Server
  private final static String addr = "127.0.0.1";
	private final static int port = 8080;
	
	// Behavior
  private final static int limit = 5;
  private final static int delay = 1;
  	
  public static void main( String[] args ) {
    int counter = 0;
    
    try {
			Socket cli = new Socket(addr, port);
			System.out.println("Client connect to " + addr + ":" + port);
			
			BufferedReader in  = new BufferedReader(new InputStreamReader(cli.getInputStream()));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));
			
			while (counter <limit) {
			  counter++;
			  
				out.write("Hello server! Love.\n");
				out.flush();
				
				String msg = in.readLine();
				System.out.println(msg);
				
				Thread.sleep(delay * 1000);
			}
			
			cli.close();
			System.out.println("Client closed.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
  }
}