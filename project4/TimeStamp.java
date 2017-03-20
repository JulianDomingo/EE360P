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

	// 1 if invoking timestamp has higher priority / smallest
	// 0 if same timestamp
	// -1 if other has higher priority / smallest
	public int compare(TimeStamp other) {
		if (this.logicalClock < other.getLogicalClock()) {
			return 1;
		}
		if (this.logicalClock == other.getLogicalClock() &&
			this.processID < other.getPID())
		{
			return 1;
		}
		if (this.logicalClock == other.getLogicalClock() &&
			this.processID == other.getLogicalClock()) 
		{
			return 0;
		}
		return -1;
	}
 
 	public void setLogicalClockSend() {
 		this.logicalClock++;
 	}

 	public void setLogicalClockReceive(int senderLogicalClock) {
 		this.logicalClock = Math.max(logicalClock, senderLogicalClock) + 1;
 	}

	public int getLogicalClock() {
		return logicalClock;
	}

	public int getPID() {
		return processID;
	}
}