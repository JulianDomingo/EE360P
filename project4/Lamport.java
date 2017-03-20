/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;

public class Lamport {
	int c;

	public Lamport() {
		c = 1;
	}

	public synchronized int getLogicalClock() {
		return c;
	}

	public synchronized void int sendEvent() {
		c++;
		return c;
	}

	public synchronized void receiveEvent(int sentTimeStamp) {
		c = Math.max(sentTimeStamp, c) + 1;
	}
}