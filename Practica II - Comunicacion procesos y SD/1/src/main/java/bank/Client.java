package bank;

import java.util.Scanner;

import p2p.Network;
import p2p.Node;

public class Client extends Node {
  
  Integer delay = 0;
  Integer ammount = 0;
  
  // ----------------------------------------------------------------------------------------------------

  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    
    switch (type) {
      case "manager":
        parseManager(source, task, args);
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

            System.out.println("# Crea una cuenta bancaria.");
            System.out.println("> crear");
            
            System.out.println("# Deposita en una cuenta bancaria.");
            System.out.println("> depositar <cuenta> <importe> <delay>");
            
            System.out.println("# Retira de una cuenta bancaria.");
            System.out.println("> retirar <cuenta> <importe> <delay>");
                                    
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
                        
          case "crear":
            if (opts.length != 1) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            runIn("manager", "createAccount", new String[] { }, Network._FIRST_);      
            break;     
            
          case "depositar":
            if (opts.length != 4) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            runIn("manager", "getAccount", new String[] { opts[1] }, Network._FIRST_);
            ammount = Integer.parseInt(opts[2]);
            delay = Integer.parseInt(opts[3]);
            break;  
            
          case "retirar":
            if (opts.length != 4) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            runIn("manager", "getAccount", new String[] { opts[1] }, Network._FIRST_);
            ammount = Integer.parseInt(opts[2]) * -1;
            delay = Integer.parseInt(opts[3]);
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
  
  private void parseManager(String source, String task, String[] args) {    
    switch (task) {

      case "createAccountSuccess":     
        System.out.println("Cuenta creada: <" + args[0] + ">\n"); 
        break; 
        
      case "writeAccountSuccess":     
        System.out.println("Cuenta guardada: <" + args[0] + ">\n"); 
        break; 
        
      case "writeAccountError":     
        System.out.println("La cuenta no se guardo: <" + args[0] + ">\n"); 
        break; 
        
      case "getAccountSuccess":  
        try {  
          System.out.println("Cuenta obtenida: <" + args[0] + "> <$" + args[1] + "> [" + args[2] + "]\n");
          ammount += Integer.parseInt(args[1]);   
          if (ammount < 0) {
            System.out.println("El saldo es menor a 0.\n");
            runIn("manager", "leaveAccount", new String[] { args[0], args[2] }, Network._FIRST_);            
          } else {
            runIn("manager", "writeAccount", new String[] { args[0], ammount.toString(), args[2] }, Network._FIRST_); 
            System.out.println("Esperando <" + delay + "s>\n");
            Thread.sleep(delay * 1000);
            runIn("manager", "leaveAccount", new String[] { args[0], args[2] }, Network._FIRST_);            
          }
        } catch (Exception e) {
          e.printStackTrace();
        }   
        break; 
        
      case "leaveAccountSuccess":     
        System.out.println("Cuenta liberada.\n"); 
        break; 
        
      case "leaveAccountError":     
        System.out.println("La cuenta no pudo ser liberada.\n"); 
        break; 
        
      case "getAccountError":     
        System.out.println("La cuenta no se pudo obtener: <" + args[0] + ">\n"); 
        break; 
        
      default:
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <manager>.");
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