package sobel;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import p2p.Network;
import p2p.Node;

public class Balancer extends Node {
  private Hashtable<Integer, BufferedImage> buffers = new Hashtable<Integer, BufferedImage>();
  private Hashtable<Integer, String[]> clients = new Hashtable<Integer, String[]>();
  private Hashtable<Integer, Hashtable<Integer, Integer[]>> sobels = new Hashtable<Integer, Hashtable<Integer, Integer[]>>();
  
  private final int _RETRY_TIMER_ = 10000;

  private final int LVL_CARGA_VACIO = 0;
  private final int LVL_CARGA_NORMAL = 5;
  private final int LVL_CARGA_ALERTA = 10;
  private final int LVL_CARGA_CRITICO = 20;
  
  private final int SQUARE_SIZE = 300;
  
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

      System.out.println("# SobelNetwork <Balancer>");
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
    
    System.out.println("Needed: " + needed);
    
    return needed;
  }
  
  private void parseClient(String source, String task, String[] args) {
    
    switch (task) {
      case "runSobel":
        try {
          Integer id = ++_id;
          
          BufferedImage buffer = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(args[1])));
          
          buffers.put(id, buffer);
          clients.put(id, new String[] { source, args[0] });
          
          Hashtable<Integer, Integer[]> subimages = new Hashtable<Integer, Integer[]>();
          sobels.put(id, subimages);                           
          
          for (int x = 0; x < buffer.getWidth(); x += SQUARE_SIZE) {
            for (int y = 0; y < buffer.getHeight(); y += SQUARE_SIZE) {              
              int w = buffer.getWidth() - x >= SQUARE_SIZE ? SQUARE_SIZE : buffer.getWidth() - x,
                  h = buffer.getHeight() - y >= SQUARE_SIZE ? SQUARE_SIZE : buffer.getHeight() - y;
              
              subimages.put(subimages.size(), new Integer[] { x, y, w, h });
            }
          }
          
          _load += subimages.size();
          setupWorkers(); 
          
          while (subimages.size() > 0) { 
            System.out.println("Starting " + subimages.size() + " subtasks.");
                        
            for (Iterator<Integer> iterator = subimages.keys().asIterator(); iterator.hasNext();) {
              Integer tid = iterator.next();     
              Integer[] curr = subimages.get(tid);
              BufferedImage frag = buffer.getSubimage(curr[0], curr[1], curr[2], curr[3]);
              ByteArrayOutputStream os = new ByteArrayOutputStream();
              ImageIO.write(frag, "PNG", Base64.getEncoder().wrap(os));
              String[] targs = new String[] { "" + id, "" + tid, os.toString(StandardCharsets.ISO_8859_1.name()) };
              
              try {
                runIn("worker", "runSobel", targs, Network._ROUND_ROBIN_);
              } catch (Exception eRun) {  
                // ...
              }
            }
            
            Thread.sleep(_RETRY_TIMER_);
          }
          
        } catch (Exception e1) {
          e1.printStackTrace();
        }   
      break;
            
      default:     
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <client>.");
        break;
    } 
    
  }
  
  private synchronized void setupWorkers() {
    int needed = getNeeded();
    System.out.println("[NEEDED] La tarea necesita (" + needed + " workers).");
    
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
        Integer id = Integer.parseInt(args[0]),
                tid = Integer.parseInt(args[1]);

        BufferedImage image = buffers.get(id);
        if (image == null) {
          return;
        }
        
        Hashtable<Integer, Integer[]> sobel = sobels.get(id);
        if (sobel == null) {
          return;
        }

        Integer[] curr = sobels.get(id).remove(tid);
        if (curr == null) {
          return;
        }
        
        System.out.println("[worker] " + task + ". Load: " + _load);        
        _load--;

        BufferedImage result = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(args[2])));
        
        BufferedImage frag = image.getSubimage(curr[0], curr[1], curr[2], curr[3]);
        Graphics2D g2d = (Graphics2D) frag.getGraphics();
        g2d.drawImage(result, 0, 0, null); 
        g2d.dispose();
                                
        if (sobel.size() > 0) {
          return;
        }
        
        sobels.remove(id);
        
        System.out.println("[balancer] sobelSuccess.");        
        String[] cl = clients.get(id);        
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", Base64.getEncoder().wrap(os));
        runIn(cl[0], "sobelSuccess", new String[] { cl[1], os.toString(StandardCharsets.ISO_8859_1.name()) });
            
        clients.remove(id);
        buffers.remove(id);        
        setupWorkers();        
      } catch (Exception e) {
        e.printStackTrace();    
      }    
    break;
          
    default:     
      System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <worker>.");
      break;
  } 
  
}
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    SobelNetwork sdypp = new SobelNetwork("SDYPP", "224.0.0.1", 8888);
    
    Balancer balancer = new Balancer();    
    sdypp.addNode("balancer", balancer);
    
    System.exit(0);
  }
    
}
