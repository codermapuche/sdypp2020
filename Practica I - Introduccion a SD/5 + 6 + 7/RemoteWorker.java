package sdypp;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RemoteWorker extends Remote {
  
  // #5
	public String getClima () throws java.rmi.RemoteException;
	
	// #6
	public ArrayList<Integer> doSum(ArrayList<Integer> numsa, ArrayList<Integer> numsb) throws RemoteException;
  public ArrayList<Integer> doSub(ArrayList<Integer> numsa, ArrayList<Integer> numsb) throws RemoteException;
  
  // #7
  public void doTask(Tarea task) throws RemoteException;
}