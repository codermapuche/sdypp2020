package sdypp;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Worker extends UnicastRemoteObject implements RemoteWorker {

  private static final long serialVersionUID = 1L;
  
  protected Worker() throws RemoteException {
    super();
  }
  
  // #5
	public String getClima() throws java.rmi.RemoteException {
	  ArrayList<String> climas = new ArrayList<String>();
    Random random = new Random();
    
	  climas.add("Soleado");
	  climas.add("Nublado");
	  climas.add("Lluvioso");
	  climas.add("Granizo");
	  climas.add("Calido");
	  	  
	  return climas.get(random.nextInt(climas.size()));
	}

	// #6
  public ArrayList<Integer> doSum(ArrayList<Integer> numsa, ArrayList<Integer> numsb) throws RemoteException {
    ArrayList<Integer> numsc = new ArrayList<Integer>();

    Iterator<Integer> ia = numsa.iterator();
    Iterator<Integer> ib = numsb.iterator();
    
    while (ia.hasNext() && ib.hasNext()) {
       numsc.add(ia.next() + ib.next());
    }
    
    while ( ia.hasNext() ) {
      numsc.add(ia.next());
    }
    
    while ( ib.hasNext() ) {
      numsc.add(ib.next());
    }
    
    return numsc;
  }

  public ArrayList<Integer> doSub(ArrayList<Integer> numsa, ArrayList<Integer> numsb) throws RemoteException {
    ArrayList<Integer> numsc = new ArrayList<Integer>();
    Iterator<Integer> ia = numsa.iterator();
    Iterator<Integer> ib = numsb.iterator();
    
    while (ia.hasNext() && ib.hasNext()) {
      numsc.add(ia.next() - ib.next());
    }
    
    while ( ia.hasNext() ) {
      numsc.add(ia.next());
    }
    
    while ( ib.hasNext() ) {
      numsc.add(-1 * ib.next());
    }
    
    return numsc;
  }

  public ArrayList<Integer> doTask(ArrayList<Integer> numsa, ArrayList<Integer> numsb) throws RemoteException {
    ArrayList<Integer> numsc = new ArrayList<Integer>();
    Iterator<Integer> ia = numsa.iterator();
    Iterator<Integer> ib = numsb.iterator();
    
    while (ia.hasNext() && ib.hasNext()) {
      numsc.add(ia.next() - ib.next());
    }
    
    while ( ia.hasNext() ) {
      numsc.add(ia.next());
    }
    
    while ( ib.hasNext() ) {
      numsc.add(-1 * ib.next());
    }
    
    return numsc;
  }
  
  // #7
  public void doTask(Tarea task) throws RemoteException {
    System.out.println("Oka!");
    task.run();
    //Thread tsk = new Thread(task);
    //tsk.start();  
  }

}