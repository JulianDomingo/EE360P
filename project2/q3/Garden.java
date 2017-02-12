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
		  try {
			fourUnseeded.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  if(holesDug - holesFilled == 8)
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
		  if(holesDug - holesSeeded == 1)
		  {
			  oneUnseeded.signal();
		  }
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
		  if(holesDug - holesSeeded == 3)
		  {
			  fourUnseeded.signal();
		  }
		  if(holesSeeded - holesFilled == 1)
		  {
			  oneUnfilled.signal();
		  }
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
		  if(holesDug - holesFilled == 7)
		  {
			  eightUnfilled.signal();
		  }
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
