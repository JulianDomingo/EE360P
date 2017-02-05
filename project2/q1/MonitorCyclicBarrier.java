public class MonitorCyclicBarrier {
	public int numberOfThreads;
	public static int currentBarrierCapacity; 

	public MonitorCyclicBarrier(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public synchronized int await() throws InterruptedException {
		int currentCapacity = readCurrentBarrierCapacity();
		currentBarrierCapacity++;
		yieldUntilBarrierFull();
		tripBarrier();
		return currentCapacity;
	}

	private void yieldUntilBarrierFull() {
		while (maxCapacityNotReached()) {
			putThreadToSleep();
		}
	}

	private void putThreadToSleep() {
		try {
			wait();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void tripBarrier() {
		if (maxCapacityReached()) {
			notifyAll();
		}
	}

	private int readCurrentBarrierCapacity() {
		return currentBarrierCapacity;
	}

	private boolean maxCapacityReached() {
		return currentBarrierCapacity == numberOfThreads;
	}

	private boolean maxCapacityNotReached() {
		return currentBarrierCapacity < numberOfThreads;
	}
}