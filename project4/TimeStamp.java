/*
 * Julian Domingo : jad5348
 * Alec Bargas : apb973
 */

import java.util.*;

public class TimeStamp {
	private int logicalClock;
	private int processID;

	public TimeStamp(int logicalClock, int processID) {
		this.logicalClock = logicalClock;
		this.processID = processID;
	}
 
	public int getLogicalClock() {
		return logicalClock;
	}

	public int getPID() {
		return processID;
	}
}