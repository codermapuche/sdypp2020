package sobel;

import java.util.Base64;
import java.util.Scanner;

import p2p.Network;
import p2p.Node;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

public class Client extends Node {
  
  private long startTime;
  
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
    BufferedImage imgIn;
    
    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# SobelNetwork <Client>");
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

            System.out.println("# Procesa el operador de sobel de forma local.");
            System.out.println("> localsobel <imagepath> <targetpath>");
            
            System.out.println("# Procesa el operador de sobel de forma remota.");
            System.out.println("> remotesobel <imagepath> <targetpath>");
                                    
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

          case "localsobel":
            if (opts.length != 3) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            opts[1] = "C:\\Users\\Nehuen\\Pictures\\bg.jpg";
            opts[2] = "C:\\Users\\Nehuen\\Pictures\\sobel.jpg";
            
            startTime = System.nanoTime();
            System.out.println("Loading Image...");
            imgIn = ImageIO.read(new File(opts[1]));
            System.out.println("Done!" );            
            System.out.println("Transforming to greyscale image..." );
            BufferedImage imgGrey = Sobel.greyscale(imgIn);
            System.out.println("Done!" );
            System.out.println("Sobel in progress... " );
            BufferedImage edgesX = Sobel.edgeDetectionX(imgGrey);
            BufferedImage edgesY = Sobel.edgeDetectionY(imgGrey);
            BufferedImage sobel = Sobel.sobel(edgesX,edgesY);
            ImageIO.write(sobel, "PNG", new File(opts[2]));
            System.out.println("Done!" );

            long endTime = System.nanoTime(),
                 timeElapsed = endTime - startTime;
            
            System.out.println("Execution time: " + (timeElapsed / 1000000) + "ms");            
          break;
            
          case "remotesobel":
            if (opts.length != 3) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            startTime = System.nanoTime();
            System.out.println("Loading Image...");
            imgIn = ImageIO.read(new File(opts[1]));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(imgIn, "PNG", Base64.getEncoder().wrap(os));
            System.out.println("Done!");
            System.out.println("Runing remote sobel...");

            runIn("balancer", "runSobel", new String[] { 
                opts[2],
                os.toString(StandardCharsets.ISO_8859_1.name())
            }, Network._FIRST_);                
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

      case "sobelSuccess":        
        long endTime = System.nanoTime(),
             timeElapsed = endTime - startTime;
        
        try {
          BufferedImage sobel = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(args[1])));
          ImageIO.write(sobel, "PNG", new File(args[0]));
        } catch (IOException e) {
          e.printStackTrace();
        }
        
        System.out.println("Done!" ); 
        System.out.println("Execution time: " + (timeElapsed / 1000000) + "ms");     
        break;
        
      default:
        System.out.println("[ERROR] La tarea solicitada (" + task + ") no es valida para peers <manager>.");
        break; 
        
    }    
  }
  
  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    SobelNetwork sdypp = new SobelNetwork("SDYPP", "224.0.0.1", 8888);
        
    Client client = new Client();    
    sdypp.addNode("client", client);
    
    System.exit(0);
  }

}