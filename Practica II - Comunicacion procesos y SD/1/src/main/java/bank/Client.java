package bank;

import java.util.Hashtable;
import java.util.Scanner;

import p2p.Network;
import p2p.Node;

public class Client extends Node {
  
  Integer await = 0;
  
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

      System.out.println("# BankNetwork <Client>");
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

            System.out.println("# Dispara una serie de tareas, indicando cantidad, tiempo entre inicio, y tiempo de resolucion.");
            System.out.println("> shot <count> <throttlems> <delayms>");
                                    
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
                        
          case "shot":
            if (opts.length != 4) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            Integer count = Integer.parseInt(opts[1]),
                    throttlems = Integer.parseInt(opts[2]),
                    delayms = Integer.parseInt(opts[3]);
            
            await += count;
            try {
              System.out.println("Iniciando shotting...");
              while (count > 0) {
                String[] args = new String[] { "" + count, "" + delayms };
                runIn("balancer", "getAccount", args, Network._FIRST_);                
                Thread.sleep(throttlems);
                count--;
              }
              System.out.println("Shotting finalizado.");
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

      case "taskResult":     
        await--;
        if (await % 10 == 0) {
          System.out.println("Tareas pendientes: <" + await + ">\n");         
        }       
        break; 
        
      default:
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <balancer>.");
        break; 
        
    }
    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    BankNetwork sdypp = new BankNetwork("SDYPP", "224.0.0.1", 8888);
        
    Client client = new Client();    
    sdypp.addNode("client", client);
    
    System.exit(0);
  }

}