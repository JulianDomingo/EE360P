public class MonitorCyclicBarrier {
	public int numberOfThreads;
	public static int currentBarrierCapacity = 0;

	public MonitorCyclicBarrier(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public synchronized int await() throws InterruptedException {
		currentBarrierCapacity++;
		int barrierVacanciesLeft = numberOfThreads - currentBarrierCapacity;
		yieldUntilBarrierFull();
		tripBarrier();
		return barrierVacanciesLeft;
	}

	private void yieldUntilBarrierFull() {
		while (maxCapacityNotReached()) {
			putThreadToSleep();
		}
	}

	private void putThreadToSleep() {
		try {
			this.wait();
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