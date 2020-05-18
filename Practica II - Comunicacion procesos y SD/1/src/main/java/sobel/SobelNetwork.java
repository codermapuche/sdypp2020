package sobel;

import p2p.Network;

public class SobelNetwork extends Network {

  public SobelNetwork(String netw, String addr, int port) {
    super(netw, addr, port);    
  }
  
  // Event driven
  public void onDiscover(String id, String name, String ip, int port) {
    //System.out.println("Discover " + name + "<" + id + "> at " + ip + ":" + port);    
  }
  
}
