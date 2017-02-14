// Julian Domingo
import java.util.concurrent.*;

public class ConcurrentQueue<T> {
	private ReentrantLock enqueueLock;
	private ReentrantLock dequeueLock;

	private Node<T> head;
	private Note<T> tail;

	public ConcurrentQueue() {
		enqueueLock = new ReentrantLock();
		dequeueLock = new ReentrantLock();
		head = new Node<T>(null);
		head = tail;
	}

	class Node<T> {
		private T value;
		private Node<T> next;
		
		public Node(T value) {
			this.value = value;
		}
		
		public T getValue() {
			return value;
		}
	}

	public void enqueue(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		enqueue.lock();
		try {
			Node<T> newNode = new Node<T>(value);
			tail.next = newNode;
			tail = newNode;
		}
		finally {
			enqueue.unlock();
		}
	}

	public T dequeue() {
		T popped;
		dequeueLock.lock();
		try {
			if (isEmpty()) {
				throw new EmptyException();
			}
			popped = head.getValue();
			head = head.next;
		}
		finally {
			dequeue.unlock();
		}
		return popped;
	}
		
	public boolean isEmpty() {
		return head.next == null;
	}
}
