package p2p;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

public class Network implements Runnable {
  private static final int _UDP_READ_ = 1;  
  
  public static final int _FIRST_       = 1;  
  public static final int _ALL_         = 2;  
  public static final int _ROUND_ROBIN_ = 3;
  static final String _SEP_             = "%";  
  static final String _SPLIT_           = "\n";
  
  private String _addr;
  private String _name;
  private int _port;
  private int _rr = 0;

  private Vector<Integer> threads = new Vector<Integer>();
  private Hashtable<String, List<String>> peers = new Hashtable<String, List<String>>();
  private Hashtable<String, Integer> ports  = new Hashtable<String, Integer>();
  private Hashtable<String, String> addrs   = new Hashtable<String, String>();
  private Hashtable<String, String> types   = new Hashtable<String, String>();
  private Hashtable<String, Socket> sockets = new Hashtable<String, Socket>();
    
  public Network(String name, String addr, int port) {
    _addr = addr;
    _port = port;
    _name = name;

    threads.add(_UDP_READ_);
    Thread tthread = new Thread(this);
    tthread.start();   
  }
  
  public void addNode(String name, Node node) {
    node.connectTo(this, name, _addr, _port);
  }
  
  public String getName() {
    return _name;
  }
  
  public String getTypeOf(String id) {
    return types.get(id);
  }  
  
  public void runFrom(Node source, String target, String task, String[] args, int _MODE_) throws Exception {
    List<String> nodes = peers.get(target);
    if (nodes == null) {
      throw new Exception("No hay nodos <" + target + "> disponibles.");      
    }
    
    switch (_MODE_) {
      case _FIRST_:
      case _ALL_:
        for (int idx=0; idx<nodes.size(); idx++) {
          String peer = nodes.get(idx);
          
          if ( peer.equals(source.getId()) ) {
            continue;
          }
          
          try {
            runFrom(source, peer, task, args);  
            if (_MODE_ == _FIRST_) {
              break;
            }
          } catch(Exception e) {
            // ...
          }
        }
        
        nodes = peers.get(target);
        if (nodes == null) {
          throw new Exception("Todos los nodos <" + target + "> se desconectaron.");      
        }
        break;
      case _ROUND_ROBIN_:      
        
        Boolean runned = false;        
        int base = _rr,
            top = nodes.size();
        
        while ( !runned ) {
          if ( base >= top ) {
            base = 0;
          }
                    
          for ( int idx=base; idx < top; idx++ ) {
            String peer = nodes.get(idx);
            
            if ( peer.equals( source.getId() ) ) {
              continue;
            }
            
            try {
              runFrom(source, peer, task, args);
              runned = true;
              _rr = idx + 1;
              break;
            } catch(Exception e) {
              // ...
            }
          }
          
          if (runned) {
            break;
          }
          
          runned = true;   
          base = 0;
          top = _rr;       
        }
        
        nodes = peers.get(target);
        if (nodes == null) {
          throw new Exception("Todos los nodos <" + target + "> se desconectaron.");      
        }       
        break;
        
      default:
        throw new Exception("Modo de ejecucion invalido.");
    }
  }
  
  private synchronized void sockWrite(Socket cli, String data) {
    try {
      OutputStream out = cli.getOutputStream();
      out.write((data + _SPLIT_).getBytes());
      out.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public void runFrom(Node source, String id, String task, String[] args) throws Exception {          
    try {
      Socket cli = sockets.get(id);
      
      if (cli == null) {
        String addr = addrs.get(id);
        int port = ports.get(id);
        
        cli = new Socket(addr, port);    
        sockets.put(id, cli);
      }   
      
      sockWrite(cli, source.getId() + _SEP_ + task + _SEP_ + String.join(_SEP_, args));  
    } catch (Exception e) {
      List<String> nodes = peers.get(types.get(id));
      nodes.remove(id);
      if (nodes.size() == 0) {
        peers.remove(types.get(id));
      }
      
      types.remove(id);
      ports.remove(id);
      addrs.remove(id);
      
      throw new Exception("El peer esta desconectado.");
    }
  }
  
  protected void onDiscover(String id, String name, String ip, int port) {
    // Override me...  
  }
  
  private void parseBroadcast(String[] event) {    
    if ( !_name.equals(event[0]) || !"BROAD".equals(event[1])) {
      return;
    }

    String type  = event[2];
    String id    = event[3];
    Integer port = Integer.parseInt(event[4]);  
    String addr  = event[5];
    
    List<String> list;        
    if ( peers.containsKey(type) ) {
      list = peers.get(type);
    } else {
      list = new ArrayList<String>();
    }

    if ( !list.contains(id) ) {
      list.add(id);
      peers.put(type, list);
      types.put(id, type);
      ports.put(id, port);
      addrs.put(id, addr);
      onDiscover(id, type, addr, port);
    }    
  }

  public void run() {
    int _task = threads.remove(0);
        
    if (_task == _UDP_READ_) {      
      MulticastSocket socket;
      InetAddress group;
      InetSocketAddress addr;
      NetworkInterface iface; 

      try {      
        socket = new MulticastSocket(_port);
        group = InetAddress.getByName(_addr);      
        addr = new InetSocketAddress(group, _port);
        iface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        
        socket.setBroadcast(true);
        socket.joinGroup(addr, iface);   
        
        while (true) {
          byte[] buff = new byte[256];
          DatagramPacket pkg = new DatagramPacket(buff, buff.length);
          socket.receive(pkg);
          String msg = new String(pkg.getData()).trim();
          msg += _SEP_ + pkg.getAddress().getHostAddress();
          String cmd[] = msg.split(_SEP_);
          parseBroadcast(cmd);
        }        
      } catch (Exception e) {
        e.printStackTrace();
      }      
    }    
  }
}
