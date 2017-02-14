/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
  
	ReentrantLock shovel, sequencer;
	Condition fourUnseeded, eightUnfilled, oneUnseeded, oneUnfilled;
	int holesDug, holesSeeded,holesFilled;
	
  public Garden() {
	  shovel = new ReentrantLock();
	  sequencer = new ReentrantLock();
	  fourUnseeded = sequencer.newCondition();
	  eightUnfilled = sequencer.newCondition();
	  oneUnseeded = sequencer.newCondition();
	  oneUnfilled = sequencer.newCondition();
	  holesDug = 0;
	  holesSeeded = 0;
	  holesFilled = 0;
  }
  
  public void startDigging() { 
	  if(holesDug - holesSeeded == 4)
	  {
		  sequencer.lock();
		  try {
			 // System.out.println("dig waiting for seed");
			fourUnseeded.await();
		  } 
		  catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  finally{
			  sequencer.unlock();
		  }
	  }
	  if(holesDug - holesFilled == 8)
	  {
		  sequencer.lock();
		  try {
			  //System.out.println("dig waiting for fill");
			eightUnfilled.await();
		  } 
		  catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  finally{
			  sequencer.unlock();  
		  }
	  }
	  shovel.lock();
  } 
  
  public void doneDigging() {
	  sequencer.lock();
	  try{
		  holesDug++;
		  oneUnseeded.signal();
	  }
	  finally{
	  	sequencer.unlock();
	  }
	  shovel.unlock();
	  //System.out.println("dug");
  }
  
  public void startSeeding() {   	 
	  if(holesDug == holesSeeded)
	  {
		  sequencer.lock();
		  try {
		//	  System.out.println("seed waiting for dig");
			  oneUnseeded.await();
		  } catch (InterruptedException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
		  finally{
			  sequencer.unlock();
		  }
	  }
  }
  
  public void doneSeeding() {
	  sequencer.lock();
	  try{
		  holesSeeded++;
		  fourUnseeded.signal();
		  oneUnfilled.signal();
	  }
	  finally{
		  sequencer.unlock();
	  }
	  //System.out.println("seeded");
  } 
  
  public void startFilling() { 
	  if(holesFilled == holesSeeded)
	  {
		  sequencer.lock();
		  try {
		//	  System.out.println("fill waiting for seed");
			oneUnfilled.await();
		  } 
		  catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		  }
		  finally{
			  sequencer.unlock();
		  }
	  }
	  shovel.lock();
  }   
  
  public void doneFilling() {
	  sequencer.lock();
	  try{
		  holesFilled++;
		  eightUnfilled.signal();
	  }
	  finally{
		  sequencer.unlock();
	  }
	  shovel.unlock();
	  //System.out.println("filled");
  } 
 
    /*
    * The following methods return the total number of holes dug, seeded or 
    * filled by Newton, Benjamin or Mary at the time the methods' are 
    * invoked on the garden class. */
   public int totalHolesDugByNewton() {
	   return holesDug;
   }
   
   public int totalHolesSeededByBenjamin() {
	   return holesSeeded;
   }
   
   public int totalHolesFilledByMary() {
	   return holesFilled;
   }
   
}
