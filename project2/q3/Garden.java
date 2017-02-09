/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

import java.util.concurrent;

public class Garden {
  // Implement a Garden class which uses ReentrantLock and Condition objects from 
  // java.util.concurrent;
  /*

  (a) Benjamin cannot plant a seed unless at least one empty hole exists and Mary cannot fill
      a hole unless at least one hole exists in which Benjamin has planted a seed.
  (b) Newton has to wait for Benjamin if there are 4 holes dug which have not been seeded
      yet. He also has to wait for Mary if there are 8 unfilled holes. Mary does not care how
      far Benjamin gets ahead of her.
  (c) There is only one shovel that can be used to dig and fill holes, and thus Newton and
      Mary need to coordinate between themselves for using the shovel; ie. only one of them
      can use the shovel at any point of time.
  */
    
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Garden {
  
	ReentrantLock shovel, sequencer;
	Condition fourUnseeded, eightUnfilled, oneUnseeded, oneUnfilled;
	int holesDug, holesSeeded,holesFilled;
	
  public Garden() {
	  shovel = new ReentrantLock();
	  fourUnseeded = sequencer.newCondition();
	  eightUnfilled = sequencer.newCondition();
	  oneUnseeded = sequencer.newCondition();
	  oneUnfilled = sequencer.newCondition();
	  holesDug = 0;
	  holesSeeded = 0;
	  holesFilled = 0;
  }
  
  public void startDigging() { 
	  if(holesDug - holesSeeded >= 4)
	  {
		  try {
			fourUnseeded.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  if(holesDug - holesFilled >= 8)
	  {
		  try {
			eightUnfilled.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  shovel.lock();
	  try{
		  holesDug++;
	  }
	  finally{
		  doneDigging();
	  }
  } 
  
  public void doneDigging() {
	  sequencer.lock();
	  try{
		  oneUnseeded.signal();
	  }
	  finally{
	  	sequencer.unlock();
	  }
	  shovel.unlock();
  }
  
  public void startSeeding() {   	 
	  if(holesDug == holesSeeded)
	  {
		  try {
			  oneUnseeded.await();
		  } catch (InterruptedException e) {
			  // TODO Auto-generated catch block
			  e.printStackTrace();
		  }
	  }
	  holesSeeded++;
	  doneSeeding();
  }
  
  public void doneSeeding() {
	  sequencer.lock();
	  try{
		  if(holesSeeded + 4 > holesDug)
		  {
			  fourUnseeded.signal();
		  }
		  oneUnfilled.signal();
	  }
	  finally{
		  sequencer.unlock();
	  }
  } 
  
  public void startFilling() { 
	  if(holesFilled == holesSeeded)
	  {
		  try {
			oneUnfilled.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  shovel.lock();
	  try{
		  holesFilled++;
	  }
	  finally{
		  doneFilling();
	  }
  }   
  
  public void doneFilling() {
	  sequencer.lock();
	  try{
		  if(holesFilled + 8 > holesSeeded)
		  {
			  eightUnfilled.signal();
		  }
		  oneUnfilled.signal();
	  }
	  finally{
		  sequencer.unlock();
	  }
	  shovel.unlock();
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
