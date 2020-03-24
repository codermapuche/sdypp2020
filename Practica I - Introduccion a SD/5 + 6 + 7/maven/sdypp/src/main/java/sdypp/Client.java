package sdypp;


import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Client {
  
  // Behavior
  private final static String addr = "127.0.0.1";
  private final static int port = 12000;
  
  private static String intsJoin(ArrayList<Integer> src, String glue) {
    String join = "";

    for (Integer nro : src) {
      join += "<" + nro + ">" + glue;
    } 
    
    return join.trim();    
  }
  
  public static void main(String[] args) {
    
    try {
      Registry registry = LocateRegistry.getRegistry(addr, port);
      System.out.println("Successfully connected to " + addr + ":" + port);
      RemoteWorker server = (RemoteWorker) registry.lookup("worker");   
      
      System.out.println("============ #5 ============");        
      System.out.println("Clima del server: " + server.getClima());
      System.out.println("============================");      
                  
      System.out.println("============ #6 ============");  
      ArrayList<Integer> numsa = new ArrayList<Integer>();
      ArrayList<Integer> numsb = new ArrayList<Integer>();
      ArrayList<Integer> numsc;
      
      numsa.add(1); numsb.add(2);
      numsa.add(3); numsb.add(4);
      numsa.add(5); numsb.add(6);
      numsa.add(7); numsb.add(8);
      numsa.add(9); numsb.add(10);
      
      System.out.println("A: " + intsJoin(numsa, " "));   
      System.out.println("B: " + intsJoin(numsb, " "));     
      
      System.out.println("----------------------------");
      
      numsc = server.doSum(numsa, numsb);
      System.out.println("SUM: " + intsJoin(numsc, " "));
      System.out.println("A: " + intsJoin(numsa, " "));   
      System.out.println("B: " + intsJoin(numsb, " ")); 
      
      System.out.println("----------------------------");   

      numsc = server.doSub(numsa, numsb);
      System.out.println("SUB: " + intsJoin(numsc, " "));
      System.out.println("A: " + intsJoin(numsa, " "));   
      System.out.println("B: " + intsJoin(numsb, " "));               

      System.out.println("============ #7 ============");  
      TaskRandom random = new TaskRandom();
      random.setMin(100);
      random.setMax(200);
      server.doTask(random);
      System.out.println("Resultado: " + random.getVal());       
      System.out.println("============================");  
      
    } catch (Exception e) {
      e.printStackTrace();
    }
    
  }
    
}
