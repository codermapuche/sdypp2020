package elastic;

import p2p.Node;

public class Worker extends Node {
  
  private int load = 0;
  private Boolean _leave = false;
  
  // ----------------------------------------------------------------------------------------------------

  // Event driven
  public void onMessage(String source, String type, String task, String[] args) {
    
    switch (type) {
      case "balancer":
        parseBalancer(source, task, args);
        break;
        
      default:
        System.out.println("[ERROR] Este cliente solo atiende a peers <balancer>.");
        break;
    }
    
  }

  // ----------------------------------------------------------------------------------------------------
  
  private void parseBalancer(String source, String task, String[] args) {    
    switch (task) {

      case "exit":        
        System.exit(0);      
        break;  
        
      case "leave":
        _leave = true;
        if (load == 0) {  
          System.exit(0);           
        }
        break; 
        
      case "resolve":
        _leave = false;
        load++;        
        try {
          System.out.println("Iniciando la tarea <"+args[2]+"> !"+load+"!.");
          Thread.sleep(Integer.parseInt(args[0]));
          args[0] = "Sleeped: " + Integer.parseInt(args[0]);
          runIn(source, "taskSuccess", args);
          System.out.println("Finalida la tarea <"+args[2]+"> !"+load+"!.");
        } catch (Exception e) {
          e.printStackTrace();
        } 
        load--;
        
        if (load == 0 && _leave) {  
          System.exit(0);           
        }
        break; 
        
      default:
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <manager>.");
        break; 
        
    }
    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    ElasticNetwork sdypp = new ElasticNetwork("SDYPP", "224.0.0.1", 8888);
        
    Worker worker = new Worker();    
    sdypp.addNode("worker", worker);
  }

}