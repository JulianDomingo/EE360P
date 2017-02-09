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
    
  public Garden() {   

  }; 

  public synchronized void startDigging() {   }; 
  public synchronized void doneDigging() {   }; 
  public synchronized void startSeeding() {   };
  public synchronized void doneSeeding() {   }; 
  public synchronized void startFilling() {   }; 
  public synchronized void doneFilling() {   }; 
 
  /*
  * The following methods return the total number of holes dug, seeded or 
  * filled by Newton, Benjamin or Mary at the time the methods' are 
  * invoked on the garden class. */
  public synchronized int totalHolesDugByNewton() {
  }; 

  public synchronized int totalHolesSeededByBenjamin() {   }; 
  public synchronized int totalHolesFilledByMary() {   }; 
}