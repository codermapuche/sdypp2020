package sdypp;


import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;

public class TaskRandom extends UnicastRemoteObject implements Task {

  private static final long serialVersionUID = 1L;
  
  protected int min;
  protected int max;
  protected int val;

  protected TaskRandom() throws RemoteException {
    super();
  }
  
  public void setMin(int _min) {
    min = _min;
  }
  
  public void setMax(int _max) {
    max = _max;
  }
  
  public int getVal() {
    return val;
  }
  
  public void run() {
    Random random = new Random();
    val = random.nextInt(max - min) + min;
  }
}