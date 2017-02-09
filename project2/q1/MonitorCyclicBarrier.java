/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 *
 */

public class MonitorCyclicBarrier {
    private int barrierSize;
    private static int currentBarrierCapacity;
    private static int resets;

    public MonitorCyclicBarrier(int numberOfThreads) {
        this.barrierSize = numberOfThreads;
        this.currentBarrierCapacity = 0;
        this.resets = 0;
    }

    public synchronized int await() throws InterruptedException {
        currentBarrierCapacity++;

        if (maxCapacityReached()) {
            resetCurrentBarrierCapacity();
            updateResets();
            tripBarrier();
        }
        else {
            yieldUntilBarrierFull();
        }
        return currentBarrierCapacity;
    }

    private void yieldUntilBarrierFull() {
        int snapShotOfResetsAmount = resets;
        while (snapShotOfResetsAmount == resets) {
            putThreadToSleep();
        }
    }

    private void tripBarrier() {
        notifyAll();
    }

    private void putThreadToSleep() {
        try {
            wait();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void updateResets() {
        resets++;
    }

    private void resetCurrentBarrierCapacity() {
        currentBarrierCapacity = 0;
    }

    private boolean maxCapacityReached() {
        return currentBarrierCapacity == barrierSize;
    }
}