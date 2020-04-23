package fsnet;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import p2p.Network;
import p2p.Node;

public class Master extends Node {
  private SortedSet<String> index = new TreeSet<String>();
  private Pattern decodePattern = Pattern.compile("^([^.]+\\.[^.]+\\.[^.]+)\\.(.*)$");
  
  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    switch (type) {
      case "master":
        parseMaster(source, task, args);
        break;
      case "client":
        parseClient(source, task, args);
        break;
      default:
        System.out.println("[MASTER-ERROR] Este cliente solo atiende a peers <client> o <master>.");
        break;
    }
  }
  
  public void onStart(Network netw) {   
    
    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# FileSharingNetwork <Master>");
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
            
            System.out.println("# Replicarse en todos los masters disponibles.");
            System.out.println("> replicate");
            
            System.out.println("# Buscar archivos en la red.");
            System.out.println("> search <filename>");

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
            
          case "search":            
            if (opts.length != 2) {
              System.out.println("Sintaxis invalida.");
              continue;
            }

            String[] entrys = index.toArray(new String[index.size()]);
            List<String> results = new ArrayList<String>();
            
            for (int idx=0;idx < entrys.length; idx++) {
              if ( entrys[idx].toLowerCase().indexOf(opts[1].toLowerCase()) > -1 ) {
                results.add(entrys[idx]);
              }
            }
            
            System.out.println("Se encontraron " + results.size() + " resultado(s):\n");
            
            for (int idx=0; idx < results.size(); idx++) {
              Matcher m = decodePattern.matcher(results.get(idx));    
              m.find();
              System.out.println("[" + m.group(1) + "] " + m.group(2));
            }
            break;
            
          case "replicate":
            if (opts.length != 2) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            try {          
              System.out.print(String.join("@", index.toArray(new String[index.size()])));   
              runIn("master", "replicate", index.toArray(new String[index.size()]), Network._ALL_);    
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
  
  private void parseMaster(String source, String task, String[] args) {
    
    switch (task) {      
      case "replicate":        
        for (int idx = 0; idx < args.length; idx++) {
          index.add(args[idx]);
        }      
      break;
      
      default:
        System.out.println("[MASTER -> MASTER] <from:" + source + "> [" + task + "]");
        System.out.println(String.join("|", args));      
        System.out.println("[MASTER-ERROR] La tarea solicitada (" + task + ") no es valida para peers <master>.");
        break;
    }
    
  }
  
  private void parseClient(String source, String task, String[] args) {
    
    switch (task) {
      case "index":        
        SortedSet<String> oldSet = index.subSet(args[0], args[0] + "_");
        String[] oldFiles = oldSet.toArray(new String[oldSet.size()]);
        for (int idx=0; idx<oldFiles.length; idx++) {
          index.remove(oldFiles[idx]);
        }
        
        for (int idx=1; idx<args.length; idx++) {
          index.add(args[idx]);
        }        
        
        try {
          runIn("master", "replicate", index.toArray(new String[index.size()]), Network._ALL_);
        } catch (Exception e) {
          System.out.println(e.getMessage());      
        }    
      break;
      
      case "search":
        String[] entrys = index.toArray(new String[index.size()]);
        List<String> results = new ArrayList<String>();
        
        for (int idx=0;idx < entrys.length; idx++) {   
          if ( entrys[idx].toLowerCase().indexOf(args[0].toLowerCase()) > -1 ) {
            results.add(entrys[idx]);
          }
        }
        
        try {
          runIn(source, "results", results.toArray(new String[results.size()]));              
        } catch(Exception e) {
          System.out.println(e.getMessage());                   
        }
        break;
      
      default:
        System.out.println("[CLIENT -> MASTER] <from:" + source + "> [" + task + "]");
        System.out.println(String.join("|", args));      
        System.out.println("[MASTER-ERROR] La tarea solicitada (" + task + ") no es valida para peers <client>.");
        break;
    } 
    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    FileSharingNetwork sdypp = new FileSharingNetwork("SDYPP", "224.0.0.1", 8888);
    
    Master master = new Master();    
    sdypp.addNode("master", master);
    
    System.exit(0);
  }
    
}
