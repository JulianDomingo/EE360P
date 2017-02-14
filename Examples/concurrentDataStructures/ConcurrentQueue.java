// Julian Domingo
import java.util.concurrent.*;

public class ConcurrentQueue {
	private ReentrantLock criticalSection;
	private Condition emptyQueue;
	private Node head;

	public ConcurrentQueue() {
		criticalSection = new ReentrantLock;
		emptyQueue = criticalSection.newCondition();
	}

	class Node {
		private int value;
		private Node next;
		
		public Node(int value) {
			this.value = value;
		}
	}

	public void enqueue(int value) {
		if (head == null) {
			head = new Node(value);
		}
		else {
			criticalSection.lock();
			Node runner = head;
			while (runner.next != null) {
				runner = runner.next;
			}
			runner.next = new Node(value);
			criticalSection.unlock();
		}
		try {
			emptyQueue.signal();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
		
	public int dequeue() {
		if (head == null) {
			try {
				emptyQueue.await();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		else {
			criticalSection.lock();
			head = head.next;
			criticalSection.unlock();	
		}	
	}

	public boolean isEmpty() {
		return head == null;
	}
}
