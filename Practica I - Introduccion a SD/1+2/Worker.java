package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Worker implements Runnable {
	
  // Drop-in
  private Socket cli;
  private int id;
  
	public Worker(Socket _cli, int _id) {
		cli = _cli;			
		id = _id;
	}

	@Override
	public void run() {
	  String msg;
	  BufferedReader in;
	  BufferedWriter out;
	      
		try {
      in = new BufferedReader(new InputStreamReader(cli.getInputStream()));
      out = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));

      msg = in.readLine();
      
			while (msg != null) {				
				// Echo
        System.out.println(msg);
        out.write(msg.replace("server", "client #" + id) + '\n');
        out.flush();		
				
        msg = in.readLine();	
			}
			
			cli.close();
			System.out.println("Worker closed.");
		} catch (IOException e) {
      e.printStackTrace();
    }		
	}

}
