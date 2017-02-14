public class ConcurrentStack<T> {
	private ReentrantLock pushLock;
	private ReentrantLock popLock;

	private Node<T> top;

	class Node<T> {
		private T value;
		private Node<T> next;
		
		public T getValue() {
			return value;
		}
	}

	public ConcurrentStack<T>() {
		top = null;
		pushLock = new ReentrantLock();
		popLock = new ReentrantLock();
	}

	public void push(T value) {
		if (value == null) {
			throw new NullPointerException();
		}
		pushLock.lock();
		try {
			Node<T> newNode = new Node<T>(value);
			newNode.next = top;
			top = newNode;
		}
		finally {
			pushLock.unlock();
		}
	}

	public T pop() {
		T popped;
		popLock.lock();
		try {
			if (top.next == null) {
				throw new NoSuchElementException();
			}
			popped = top.getValue();
			top = top.next;
		}
		finally {
			popLock.unlock();
		}
		return popped;
	}

	



}
