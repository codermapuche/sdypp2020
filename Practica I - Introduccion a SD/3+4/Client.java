package sdypp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
  
  // Server
  private final static String addr = "127.0.0.1";
	private final static int port = 8080;
	private final static String os = System.getProperty("os.name");
	  	
  public static void main( String[] args ) {    
    String cmd = "";
    Scanner scanner = new Scanner(System.in);

    try {
      Socket cli = new Socket(addr, port);
      String msg; 
      
      BufferedReader in  = new BufferedReader(new InputStreamReader(cli.getInputStream()));
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(cli.getOutputStream()));
      
      while (cmd != "exit") {        
        try { 
          if (os.contains("Windows")) {
            for(int i = 0; i < 300; i++)
              System.out.print("\n");
          } else {
            Runtime.getRuntime().exec("clear");
          }
        } catch (final Exception e) {
          // Sorry baby.
        }
        
        System.out.print("<cmd> [@ <opts>]" + "\n" + "\n");
        System.out.print("Examples: " + "\n");
        
        System.out.print("write @ <user> @ <message>" + "\n");
        System.out.print("# write @ bob @ Hello bob, i'am alice, how are you?" + "\n");
        System.out.print("# write @ alice @ Hello alice, i'am nice, you?" + "\n");
        
        System.out.print("read @ <user>" + "\n");
        System.out.print("# read @ bob" + "\n");
        System.out.print("# read @ alice" + "\n");
        
        System.out.print("clean @ <user>" + "\n");
        System.out.print("# clean @ bob" + "\n");
        System.out.print("# clean @ alice" + "\n");
        
        System.out.print("exit" + "\n");
        System.out.print("# exit" + "\n" + "\n");
        System.out.print("# ");
        
        cmd = scanner.nextLine();
        String[] opts = cmd.split(" @ ");
        
        switch (opts[0]) {
          case "exit":
            if (opts.length != 1) {
              System.out.print("Sintaxis invalida." + "\n");
              continue;
            }
            cmd = opts[0];
            break;
            
          case "write":
            if (opts.length != 3) {
              System.out.print("Sintaxis invalida." + "\n");
              continue;
            }
            out.write("write@" + opts[1] + "@" + opts[2] + "\n");   
            out.flush();             
            msg = in.readLine(); 
          
            opts = msg.split("@");
            
            switch (opts[0]) {                
              case "OK":  
                System.out.println("Message writed.");
              break;
              
              default:  
                System.out.println("Error writing messages.");
              break;          
            }   
            break;
            
          case "read":
            if (opts.length != 2) {
              System.out.print("Sintaxis invalida." + "\n");
              continue;
            }
            out.write("read@" + opts[1] + "\n");   
            out.flush();            
            msg = in.readLine(); 
            
            int nro = 1;
            while (msg != null) {
              opts = msg.split("@");
              
              switch (opts[0]) {        
                case "MSG":
                  System.out.println("[" + nro + "]: " + opts[1]); 
                  ++nro;
                  msg = in.readLine();
                break;
                
                case "END":  
                  System.out.println("All messages readed.");
                  msg = null;
                break;
                
                default:  
                  System.out.println("Error reading messages.");
                  msg = null;
                break;          
              }      
            }
            break;
            
          case "clean":
            if (opts.length != 2) {
              System.out.print("Sintaxis invalida." + "\n");
              continue;
            }
            out.write("clean@" + opts[1] + "\n");   
            out.flush();            
            msg = in.readLine(); 
          
            opts = msg.split("@");
            
            switch (opts[0]) {                
              case "OK":  
                System.out.println("All messages clean.");
              break;
              
              default:  
                System.out.println("Error clean messages.");
              break;          
            }         
            break;  
            
          default:
            System.out.print("Opcion invalida.");
            break;        
        }   
        
        scanner.nextLine();        
      }
      
      cli.close();
    } catch (IOException e) {
      e.printStackTrace();
    }    
  }
}