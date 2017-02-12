/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

import java.util.concurrent.Semaphore;

public class CyclicBarrier {
    private Semaphore isBarrierFull;
    private Semaphore criticalSection;

    private int currentBarrierVacancies;
    private int barrierSize;

    public CyclicBarrier(int numberOfThreads) {
        currentBarrierVacancies = numberOfThreads;
        barrierSize = numberOfThreads;
        isBarrierFull = new Semaphore(0);
        criticalSection = new Semaphore(1);
    }

    public int await() throws InterruptedException {
        criticalSection.acquire();
        currentBarrierVacancies--;
        criticalSection.release();

        yieldUntilBarrierFull();

        if (isBarrierFilled()) {
            resetCurrentCapacity();
            tripBarrier();
        }
        return currentBarrierVacancies;
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
            // System.out.println("Current capacity: " + currentBarrierVacancies);
            semaphore.acquire();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void resetCurrentCapacity() {
        currentBarrierVacancies = barrierSize;
    }

    private boolean isBarrierFilled() {
        return barrierSize - currentBarrierVacancies == barrierSize;
    }
}
