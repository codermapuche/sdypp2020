package sobel;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;

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
        
      case "runSobel":
        _leave = false;
        load++;     

        try {
          BufferedImage sobel = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(args[2])));
          BufferedImage imgGrey = Sobel.greyscale(sobel);
          BufferedImage edgesX = Sobel.edgeDetectionX(imgGrey);
          BufferedImage edgesY = Sobel.edgeDetectionY(imgGrey);
          sobel = Sobel.sobel(edgesX,edgesY);
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          ImageIO.write(sobel, "PNG", Base64.getEncoder().wrap(os));
          runIn(source, "taskSuccess", new String[] { 
              args[0],
              args[1],
              os.toString(StandardCharsets.ISO_8859_1.name())
          });
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
    SobelNetwork sdypp = new SobelNetwork("SDYPP", "224.0.0.1", 8888);
        
    Worker worker = new Worker();    
    sdypp.addNode("worker", worker);
  }

}