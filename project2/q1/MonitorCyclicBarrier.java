public class MonitorCyclicBarrier {
    private int barrierSize;
    private int currentBarrierVacancies;
    private static int resets;

    public MonitorCyclicBarrier(int numberOfThreads) {
        barrierSize = numberOfThreads;
        currentBarrierVacancies = numberOfThreads;
        resets = 0;
    }

    public synchronized int await() throws InterruptedException {
        currentBarrierVacancies--;

        if (maxCapacityReached()) {
            resetCurrentBarrierCapacity();
            updateResets();
            tripBarrier();
        }
        else {
            yieldUntilBarrierFull();
        }
        return currentBarrierVacancies;
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
        currentBarrierVacancies = barrierSize;
    }

    private boolean maxCapacityReached() {
        return barrierSize - currentBarrierVacancies == barrierSize;
    }
}