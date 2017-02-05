/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

import java.util.concurrent.Semaphore;

public class CyclicBarrier {
    private Semaphore isBarrierFull;
    private Semaphore incrementingBarrierCapacity;

    private static int currentBarrierCapacity = 0;
    private int barrierSize;

    public CyclicBarrier(int numberOfThreads) {
        this.barrierSize = numberOfThreads;
        isBarrierFull = new Semaphore(0, true);
        incrementingBarrierCapacity = new Semaphore(1, true);
    }

    public int await() throws InterruptedException {
        int currentCapacity = readCurrentBarrierCapacity();
        updateCapacityExclusively();
        yieldUntilBarrierFull();
        tripBarrier();
        return currentCapacity;
    }

    private int readCurrentBarrierCapacity() {
        return currentBarrierCapacity;
    }

    private void yieldUntilBarrierFull() {
        if (currentBarrierCapacity != barrierSize) {
            acquireSemaphore(isBarrierFull);
        }
    }

    private void updateCapacityExclusively() {
        acquireSemaphore(incrementingBarrierCapacity);
        currentBarrierCapacity++;
        incrementingBarrierCapacity.release();
    }

    private void tripBarrier() {
        isBarrierFull.release();
    }

    private void acquireSemaphore(Semaphore semaphore) {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
