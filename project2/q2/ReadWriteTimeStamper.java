public class ReadWriteTimeStamper {
	private HashMap<Thread, Integer> orderOfThreadEntries = new HashMap<Thread, Integer>();
	private static int threadNumber = 0;

	public void requestRead() {
		orderOfThreadEntries.put(Thread.currentThread(), threadNumber + 1);
	}

	public void read() {
		int timeOfEntry = orderOfThreadEntries.get(Thread.currentThread());
	}

	public void requestWrite() {

	}

	public void write() {
		
	}
}