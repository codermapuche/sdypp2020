package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;

public class Worker implements Runnable {
	
  // Drop-in
  private Socket cli;
  private Hashtable<String, ArrayList<String>> messages = new Hashtable<String, ArrayList<String>>();
  
	public Worker(Socket _cli, Hashtable<String, ArrayList<String>> _messages) {
		cli = _cli;			
		messages = _messages;
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
      
        String[] cmd = msg.split("@");
        ArrayList<String> inbox;
        
        switch(cmd[0]) { 
        
          case "write":
            if (!messages.containsKey(cmd[1])) {
              messages.put(cmd[1], new ArrayList<String>());              
            }
            
            inbox = messages.get(cmd[1]);
            inbox.add(cmd[2]);
            
            out.write("OK@Message ok.\n");
            out.flush(); 
          break; 
          
          case "read":            
            if (messages.containsKey(cmd[1])) {
              inbox = messages.get(cmd[1]);              
            } else {       
              inbox = new ArrayList<String>(); 
            }
            
            for (String post : inbox) {
              out.write("MSG@" + post + ".\n");
              out.flush();
            }            
            
            out.write("END@Read done.\n");
            out.flush();
          break; 
          
          case "clean": 
            messages.remove(cmd[1]);            
            out.write("OK@Message ok.\n");
            out.flush(); 
          break; 
          
          default: 
            System.out.println("Protocol error."); 
            out.write("ERR@Protocol error.\n");
            out.flush(); 
          break; 
        }      
				
        msg = in.readLine();	
			}
			
			cli.close();
		} catch (IOException e) {
      System.out.println("Client closed."); 
    }		
	}

}
