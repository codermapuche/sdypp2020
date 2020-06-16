package p2p;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Vector;

public class Node implements Runnable {
  
  private static final int _LATENCY_     = 1000;
  
  private static final int _TCP_SERVER_  = 1;  
  private static final int _UDP_BROAD_   = 2;  
  private static final int _TCP_WORKER_  = 3;  
  private static final int _RUN_TASK_    = 4;  

  protected String _id;
  protected Network _netw;
  
  private int    _port;
  private String _addr;
  private String _name;

  private Vector<Integer> threads = new Vector<Integer>();
  private Vector<Socket> clients = new Vector<Socket>();
  private Vector<String> tasks = new Vector<String>();
  private Integer _tcp;
  
  public Node() {
    Date date = new Date();    
    _id = Base64.getEncoder().encodeToString(BigInteger.valueOf(date.getTime()).toByteArray());    
  }
  
  public String getId() {
    return _id;  
  }
  
  public void connectTo(Network netw, String name, String addr, int port) {
    _name = name;
    _netw = netw;
    _addr = addr;
    _port = port;
    
    threads.add(_TCP_SERVER_);
    Thread serverthread = new Thread(this);
    serverthread.start();   
    
    onStart(netw);
  }  

  public void onMessage(String source, String type, String task, String[] args) {   
    // ...
  }
  
  public void onStart(Network netw) {
    // ...
  }

  public void runIn(String target, String task, String[] args, int mode) throws Exception {   
    _netw.runFrom(this, target, task, args, mode);
  }
  
  public void runIn(String id, String task, String[] args) throws Exception {          
    _netw.runFrom(this, id, task, args);
  }
  
  public void run() {  
    int _task = threads.remove(0);

    if (_task == _RUN_TASK_) {    
      String msg = tasks.remove(0);
      
      try {
        String[] args = msg.split(Network._SEP_);
        onMessage(args[0], _netw.getTypeOf(args[0]), args[1], Arrays.copyOfRange(args, 2, args.length));        
      } catch(Exception e) {
        //System.out.println("ERROR: " + msg);
      }
    }
    
    if (_task == _TCP_WORKER_) {      
      try {
        Socket _cli = clients.remove(0);
        BufferedReader in = new BufferedReader(new InputStreamReader(_cli.getInputStream()));

        String msg = in.readLine();
        
        while (msg != null) {          
          tasks.add(msg);

          threads.add(_RUN_TASK_);
          Thread tthread = new Thread(this);
          tthread.start();    
          
          msg = in.readLine();  
        }
        
        _cli.close();        
      } catch (Exception e) {
        // ...
      }     
    }
    
    if (_task == _TCP_SERVER_) {
      Boolean accept = true;
      
      try {
        ServerSocket _server = new ServerSocket(0);
        _tcp = _server.getLocalPort();
        
        threads.add(_UDP_BROAD_);        
        Thread broadthread = new Thread(this);
        broadthread.start(); 
        
        while (accept) {
          Socket cli = _server.accept();

          clients.add(cli);
          threads.add(_TCP_WORKER_);        
          Thread workerthread = new Thread(this);
          workerthread.start();             
        }
        
        _server.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    if (_task == _UDP_BROAD_) {
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
          byte[] buff;
          buff = (_netw.getName() + Network._SEP_ + "BROAD" + Network._SEP_ + _name + Network._SEP_ + _id + Network._SEP_ + _tcp).getBytes();
          DatagramPacket pkg = new DatagramPacket(buff, buff.length, InetAddress.getByName("255.255.255.255"), _port);
          pkg.setData(buff);        
          socket.send(pkg);
          Thread.sleep(_LATENCY_); 
        }
        
      } catch (Exception e) {
        e.printStackTrace();
      }      
    }
    
  }
}
