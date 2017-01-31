public class PetersonAlgorithm {
	private boolean[] wantingCriticalSection = new boolean[]{false, false};
	private int turn = 0;

	public void requestCriticalSection(int invokingThreadIndex) {
		int otherThreadIndex = 1 - invokingThreadIndex;
		wantingCriticalSection[invokingThreadIndex];
		turn = otherThreadIndex;
		while (turn == otherThreadIndex && wantingCriticalSection[otherThreadIndex]);
	}

	public void releaseCriticalSection(int invokingThreadIndex) {
		wantingCriticalSection[invokingThreadIndex] = false;
	}
}