public class FairReadWriteLock {
	private static int numberOfReaders;
	private static int numberOfWriters;
	private static int readOrWritesRequested;
	private static int overallTurn;

	public FairReadWriteLock(int numberOfReaders) {
		numberOfReaders = 0;
		numberOfWriters = 0;
		overallTurn = 0;
	}
                        
	public synchronized void beginRead() {
		int thisThreadTurn = readOrWritesRequested;
		readOrWritesRequested++;

		while (readRequirementsViolated() || overallTurn > thisThreadTurn) {
			putThreadToSleep();
		}

		overallTurn++;
		numberOfReaders++;
		notifyAll();
	}
	
	public synchronized void endRead() {
		numberOfReaders--;
		if (numberOfReaders == 0) {
			notifyAll();
		}
	}
	
	public synchronized void beginWrite() {
		int thisThreadTurn = readOrWritesRequested;
		readOrWritesRequested++;

		while (writeRequirementsViolated() || overallTurn > thisThreadTurn) {
			putThreadToSleep();
		}

		overallTurn++;
		numberOfWriters++;
		notifyAll();
	}

	private boolean readRequirementsViolated() {
		if (numberOfWriters > 0) { return true; }
	}

	private boolean writeRequirementsViolated() {
		if (numberOfReaders > 0 || numberOfWriters > 0) { return true; }
	}

	public synchronized void endWrite() {
		numberOfWriters--;
		if (numberOfWriters == 0) {
			notifyAll();
		}
	}

	private void putThreadToSleep() {
		try {
			wait();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
