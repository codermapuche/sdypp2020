package fsnet;

import p2p.Network;

public class FileSharingNetwork extends Network {

  public FileSharingNetwork(String netw, String addr, int port) {
    super(netw, addr, port);    
  }
  
  // Event driven
  public void onDiscover(String id, String name, String ip, int port) {
    //System.out.println("Discover " + name + "<" + id + "> at " + ip + ":" + port);    
  }
  
}
