package bank;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import p2p.Network;
import p2p.Node;

public class Manager extends Node {
  private Hashtable<Integer, Integer> accounts = new Hashtable<Integer, Integer>();
  private Hashtable<Integer, Integer> tokens = new Hashtable<Integer, Integer>();
  
  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    switch (type) {
      case "client":
        parseClient(source, task, args);
        break;
      default:
        System.out.println("[ERROR] Solo atiende a peers <client>.");
        break;
    }
  }
  
  public void onStart(Network netw) {   
    
    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# BankNetwork <Manager>");
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
                        
            System.out.println("# Muestra el estado actual de las cuentas.");
            System.out.println("> status");

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
            
          case "status":            
            if (opts.length != 1) {
              System.out.println("Sintaxis invalida.");
              continue;
            }

            Set<Integer> keys = accounts.keySet();
            Iterator<Integer> itr = keys.iterator();
            
            System.out.println("Acc\t|\tSaldo\t|\tToken");
            while ( itr.hasNext() ) {
              Integer acc = itr.next();              
              System.out.println(acc + "\t|\t"+accounts.get(acc)+"\t|\t" + tokens.get(acc));
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
    
  private void parseClient(String source, String task, String[] args) {
    Integer acc, token, leave;
    
    switch (task) {
      case "createAccount":
        acc = accounts.size() + 1;
        accounts.put(acc, 0);
        
        try {
          runIn(source, "createAccountSuccess", new String[] { acc.toString(), "0" });
        } catch (Exception e) {
          System.out.println(e.getMessage());      
        }    
      break;
            
      case "writeAccount":
        acc = Integer.parseInt(args[0]);
        token = (int) (new Date().getTime() / 1000);
        leave = tokens.get(acc);

        if (leave == null || token > leave || leave == Integer.parseInt(args[2])) {    
          accounts.put(acc, Integer.parseInt(args[1]));
          try {
            runIn(source, "writeAccountSuccess", args);
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          }
        } else {
          try {
            runIn(source, "writeAccountError", args);
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          }          
        }    
      break;
      
      case "getAccount":
        acc = Integer.parseInt(args[0]);
        token = (int) (new Date().getTime() / 1000);
        leave = tokens.get(acc);
        
        if (leave == null || token > leave) {
          token += 1000 * 90; // 90 seconds
          tokens.put(acc, token);
          try {
            runIn(source, "getAccountSuccess", new String[] { 
                args[0], 
                accounts.get(Integer.parseInt(args[0])).toString(),
                token.toString()
            });
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          } 
        } else {
          try {
            runIn(source, "getAccountError", args);
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          }           
        }        
      break;
      
      case "leaveAccount": 
        acc = Integer.parseInt(args[0]);
        
        if (tokens.get(acc) == Integer.parseInt(args[1])) {
          tokens.remove(acc);
          try {
            runIn(source, "leaveAccountSuccess", args);
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          } 
        } else {
          try {
            runIn(source, "leaveAccountError", args);
          } catch (Exception e) {
            System.out.println(e.getMessage());      
          }          
        }   
      break;
            
      default:     
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <client>.");
        break;
    } 
    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    BankNetwork sdypp = new BankNetwork("SDYPP", "224.0.0.1", 8888);
    
    Manager manager = new Manager();    
    sdypp.addNode("manager", manager);
    
    System.exit(0);
  }
    
}
