/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

import java.util.concurrent.Semaphore;

public class CyclicBarrier {
    private Semaphore isBarrierFull;
    private Semaphore criticalSection;

    private static int currentBarrierCapacity = 0;
    private int barrierSize;

    public CyclicBarrier(int numberOfThreads) {
        this.barrierSize = numberOfThreads;
        isBarrierFull = new Semaphore(0, true);
        criticalSection = new Semaphore(1, true);
    }

    public int await() throws InterruptedException {
        criticalSection.acquire();
        int currentCapacity = currentBarrierCapacity;
        currentBarrierCapacity++;
        criticalSection.release();

        yieldUntilBarrierFull();

        if (isBarrierFilled()) {
            resetCurrentCapacity();
            tripBarrier();
        }
        return currentCapacity;
    }

    private void yieldUntilBarrierFull() {
        if (!isBarrierFilled()) {
            acquireSemaphore(isBarrierFull);
        }
    }

    private void tripBarrier() {
        isBarrierFull.release(barrierSize - 1);
    }

    private void acquireSemaphore(Semaphore semaphore) {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resetCurrentCapacity() {
        currentBarrierCapacity = 0;
    }

    private boolean isBarrierFilled() {
        return currentBarrierCapacity == barrierSize;
    }
}
