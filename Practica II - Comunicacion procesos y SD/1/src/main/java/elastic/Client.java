package elastic;

import java.util.Scanner;

import p2p.Network;
import p2p.Node;

public class Client extends Node {
  
  // ----------------------------------------------------------------------------------------------------

  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    
    switch (type) {
      case "balancer":
        parseBalancer(source, task, args);
        break;
        
      default:
        System.out.println("[ERROR] Este cliente solo atiende a peers <manager>.");
        break;
    }
    
  }
  
  public void onStart(Network netw) {       

    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# ElasticNetwork <Client>");
      System.out.println("## Type <help> for help.");
      
      while ( !cmd.equals("exit") ) {
        
        System.out.print("\n> ");
        
        cmd = scanner.nextLine();
        String[] opts = cmd.split(" ");
        
        switch (opts[0]) {
          case "help":
            if (opts.length != 1) {
              System.out.println("Sintaxis invalida.");
              continue;
            }

            System.out.println("# Lanza una serie de tareas.");
            System.out.println("> fire <count> <timer>");
                                    
            System.out.println("# Salir del programa.");
            System.out.println("> exit");
            break;
            
          case "exit":
            if (opts.length != 1) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            cmd = opts[0];
            break;
            
          case "fire":
            if (opts.length != 3) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            try {
              String[] args = new String[] { opts[2] };
              int count = Integer.parseInt(opts[1]);
              while (count > 0) {
                System.out.println("Iniciando tarea...");
                runIn("balancer", "runTask", args, Network._FIRST_); 
                count--;
              }             
            } catch(Exception e) {
              System.out.println(e.getMessage());                 
            }
            break;
                        
          default:
            System.out.print("Opcion invalida." + "\n");
            break;        
        }        
      }
      
      scanner.close();
    } catch (Exception e) {
      e.printStackTrace();
    }    
  }

  // ----------------------------------------------------------------------------------------------------
  
  private void parseBalancer(String source, String task, String[] args) {    
    switch (task) {

      case "taskSuccess":        
        System.out.println("Tarea realizada: <" + args[1] + ">\n");        
        break;
        
      default:
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <manager>.");
        break; 
        
    }    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    ElasticNetwork sdypp = new ElasticNetwork("SDYPP", "224.0.0.1", 8888);
        
    Client client = new Client();    
    sdypp.addNode("client", client);
    
    System.exit(0);
  }

}