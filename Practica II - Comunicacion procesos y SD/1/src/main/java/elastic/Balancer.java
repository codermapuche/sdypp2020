package elastic;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import p2p.Network;
import p2p.Node;

public class Balancer extends Node {
  private final int _RETRY_TIMER_ = 1000;
  
  private final int LVL_CARGA_VACIO = 0;
  private final int LVL_CARGA_NORMAL = 5;
  private final int LVL_CARGA_ALERTA = 10;
  private final int LVL_CARGA_CRITICO = 20;
  
  private int _id = 0;
  private int _load = 0;
  private int _workers = 0;
  
  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    switch (type) {
      case "worker":
        parseWorker(source, task, args);
        break;
      case "client":
        parseClient(source, task, args);
        break;
      default:
        System.out.println("[ERROR] Solo atiende a peers <client> o <worker>.");
        break;
    }
  }
  
  public void onStart(Network netw) {

    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# ElasticNetwork <Balancer>");
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

            System.out.println("# Muestra el estado actual de balanceador.");
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

            System.out.println("Tasks: " + _load);
            System.out.println("Workers: " + _workers);
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
  
  private void addWorker() {
    try {
      // Get https://misitio.com/addworker
      String javaHome  = System.getProperty("java.home");
      String javaBin   = javaHome + File.separator + "bin" + File.separator + "java";
      String classpath = System.getProperty("java.class.path");
      String className = Worker.class.getName();
      List<String> command = new LinkedList<String>();
      
      command.add(javaBin);
      command.add("-cp");
      command.add(classpath);
      command.add(className);
    
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.inheritIO().start();
      _workers++;
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private void addTask(String[] args) {
    Boolean pending = true;
    
    while (pending) {
      try {
        runIn("worker", "resolve", args, Network._ROUND_ROBIN_);
        pending = false;
      } catch (Exception e) {
        try {
          Thread.sleep(_RETRY_TIMER_);
        } catch (InterruptedException e1) {
          e1.printStackTrace();
        }
      }     
    }
  }
  
  private int getNeeded() {    
    int needed = 0;
    
    if (_load > LVL_CARGA_VACIO) {
      needed = 1;
    }
    
    if (_load > LVL_CARGA_NORMAL) {
      needed = 2;            
    }
    
    if (_load > LVL_CARGA_ALERTA) {
      needed = 4;            
    }
    
    if (_load > LVL_CARGA_CRITICO) {
      needed = 6 + Math.round(_load / 500);                        
    }
    
    return needed;
  }
  
  private void parseClient(String source, String task, String[] args) {
    
    switch (task) {
      case "runTask":
        int id = ++_id;
        
        try {
          _load++;          
          setupWorkers();         
          addTask(new String[] { args[0], "" + id, source });
        } catch (Exception e) {
          System.out.println(e.getMessage());      
        }    
      break;
            
      default:     
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <client>.");
        break;
    } 
    
  }
  
  private synchronized void setupWorkers() {
    int needed = getNeeded();
    
    while(_workers > needed) {  
      try {
        runIn("worker", "leave", new String[] { }, Network._ROUND_ROBIN_);
        _workers--;
      } catch (Exception e) {
        System.out.println(e.getMessage());      
      }   
    }    
    
    while(_workers < needed) {
      addWorker();
    }
  }
  
private void parseWorker(String source, String task, String[] args) {
  
  switch (task) {
    case "taskSuccess":      
      try {
        runIn(args[2], "taskSuccess", args);
        _load--;
        setupWorkers();
      } catch (Exception e) {
        System.out.println(e.getMessage());      
      }    
    break;
          
    default:     
      System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <worker>.");
      break;
  } 
  
}
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    ElasticNetwork sdypp = new ElasticNetwork("SDYPP", "224.0.0.1", 8888);
    
    Balancer balancer = new Balancer();    
    sdypp.addNode("balancer", balancer);
    
    System.exit(0);
  }
    
}
