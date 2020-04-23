package fsnet;

import java.io.File;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Date;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import p2p.Network;
import p2p.Node;

public class Client extends Node {

  private SortedSet<String> index = new TreeSet<String>();
  private Hashtable<String, String> paths = new Hashtable<String, String>();
  private Hashtable<String, String> downloads = new Hashtable<String, String>();
  private Pattern decodePattern = Pattern.compile("^([^.]+\\.[^.]+\\.[^.]+)\\.(.*)$");
  
  // 10MB/S (_CHUNK_SIZE_ = 10MB / _THROTTLE = 1s)
  private final int _CHUNK_SIZE_ = 10 * 1024 * 1024;
  
  // ----------------------------------------------------------------------------------------------------

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
        System.out.println("[CLIENT-ERROR] Este cliente solo atiende a peers <client> o <master>.");
        break;
    }
    
  }
  
  public void onStart(Network netw) {       

    try {      
      String cmd = "";
      Scanner scanner = new Scanner(System.in);

      System.out.println("# FileSharingNetwork <Client>");
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

            System.out.println("# Indexar un directorio.");
            System.out.println("> index <fullpath>");
            
            System.out.println("# Publicar un indice en los masters.");
            System.out.println("> share <index>");
            
            System.out.println("# Buscar archivos en la red.");
            System.out.println("> search <filename> <seconds>");
            
            System.out.println("# Descargar archivo.");
            System.out.println("> download <fileid> <fullsavepath>");

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
            if (opts.length != 3) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            try {
              String[] args = new String[] { opts[1] };
              runIn("master", "search", args, Network._FIRST_);              
            } catch(Exception e) {
              System.out.println(e.getMessage());                 
            }
            
            Thread.sleep(Integer.parseInt(opts[2]) * 1000);
            break;
            
          case "index":
            if (opts.length != 2) {
              System.out.println("Sintaxis invalida.");
              continue;
            }

            Date idate = new Date();    
            Long timestamp = idate.getTime();
            
            File[] files = new File(opts[1]).listFiles(); 
            String iid = Base64.getEncoder().encodeToString(BigInteger.valueOf(timestamp).toByteArray());  
            String prefix = getId() + "." + iid + ".";
            paths.put(iid, opts[1]);

            System.out.println("------------------");
            System.out.println("# INDICE: " + iid);  
            System.out.println("------------------\n");    
            for (int idx = 0; idx < files.length; idx++) {
              if ( files[idx].isFile() ) {
                timestamp++; 
                String fid = Base64.getEncoder().encodeToString(BigInteger.valueOf(timestamp).toByteArray());  
                index.add(prefix + fid + "." + files[idx].getName());
                System.out.println("[" + prefix + fid + "] " + files[idx].getName());
              }
            }
       
            break;

          case "share":
            if (opts.length != 2) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            SortedSet<String> share = index.subSet(getId() + "." + opts[1], getId() + "." + opts[1] + "_");
            share.add(getId() + "." + opts[1]);
            
            try {          
              runIn("master", "index", share.toArray(new String[share.size()]), Network._FIRST_);    
              System.out.println("Shared " + (share.size() - 1) + " file(s).");
            } catch(Exception e) {
              System.out.println(e.getMessage());              
            }
            break;

          case "download":
            if (opts.length != 3) {
              System.out.println("Sintaxis invalida.");
              continue;
            }
            
            String[] src = opts[1].split("\\.");
            downloads.put(opts[1], opts[2]);
            
            try {
              runIn(src[0], "download", new String[] { src[1], src[2] });
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
  
  private void parseMaster(String source, String task, String[] args) {
    
    switch (task) {

      case "results":        
        System.out.println("Se encontraron " + args.length + " resultado(s):\n");
        
        for (int idx=0; idx < args.length; idx++) {
          Matcher m = decodePattern.matcher(args[idx]);      
          m.find();    
          System.out.println("[" + m.group(1) + "] " + m.group(2));
        }
        break;  
        
      default:
        System.out.println("[MASTER -> CLIENT] <from:" + source + "> [" + task + "]");
        System.out.println(String.join("|", args));   
        System.out.println("[CLIENT-ERROR] La tarea solicitada (" + task + ") no es valida para peers <master>.");
        break; 
        
    }
    
  }
  
  private synchronized void writeFile(String pathname, int offset, byte[] data) {
    try {
      RandomAccessFile target = new RandomAccessFile(pathname, "rw");
      target.seek(offset);
      target.write(data);
      target.close();
    } catch (Exception e) {
      // ...
    }    
  }
  
  private void parseClient(String source, String task, String[] args) {
    
    switch (task) {

      case "response":        
        try {
          byte[] data = Base64.getDecoder().decode(args[3]);        
          System.out.println(args[1] + " -> " + data.length);
          writeFile(downloads.get(args[0]), Integer.parseInt(args[1]), data);
        } catch(Exception e) {
          System.out.println(e.getMessage());          
        }
        
        if ( Integer.parseInt(args[2]) == 0 ) {
          System.out.println("Download complete [" + args[0] + "] -> " + downloads.get(args[0]));
          downloads.remove(args[0]);
        }
        break;
      
      case "download":
        String indexKey = getId() + "." + args[0] + "." + args[1];        
        String filePath = paths.get(args[0]);
        String fileName = index.tailSet(indexKey).first();
        Matcher m = decodePattern.matcher(fileName);      
        m.find();    
        fileName = m.group(2);        
        filePath +=  "/" + fileName;
        
        try {
          RandomAccessFile target = new RandomAccessFile(filePath, "r");
          
          int left = (int) target.length();         
          int right = 0;
          byte[] chunk;  

          while (left > 0) {
            target.seek(right);
            chunk = new byte[Math.min(_CHUNK_SIZE_, left)];
            target.read(chunk);            
            
            left -= chunk.length;
                        
            String base64 = Base64.getEncoder().encodeToString(chunk);        
            System.out.println(right + " => " + base64.length());        
            runIn(source, "response", new String[] { indexKey, "" + right, "" + left, base64 });

            right += chunk.length;
          }
          
          target.close();   
        } catch (Exception e) {
          System.out.println(e.getMessage());
        }
        break;
    
      default:
        System.out.println("[CLIENT -> CLIENT] <from:" + source + "> [" + task + "]");
        System.out.println(String.join("|", args));   
        System.out.println("[CLIENT-ERROR] La tarea solicitada (" + task + ") no es valida para peers <client>.");
        break;
    }    
    
  }

  // ----------------------------------------------------------------------------------------------------
  
  public static void main(String[] args) {
    FileSharingNetwork sdypp = new FileSharingNetwork("SDYPP", "224.0.0.1", 8888);
        
    Client client = new Client();    
    sdypp.addNode("client", client);
    
    System.exit(0);
  }

}