//Name=Julian Domingo
//UT-EID=jad5348

import java.util.*;
import java.util.concurrent.*;

public class PSort {
	public static final int MAX_THREADS = Runtime.getRuntime().availableProcessors();
	private static ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);

	private static class SubArray implements Runnable {
		private int[] subArray;
		private int begin;
		private int end;

		private SubArray(int[] subArray, int begin, int end) {
			this.subArray = subArray;
			this.begin = begin;
			this.end = end;
		}

		@Override
		public void run() {
			// Execute quicksort.
			if (isEdgeCase(subArray, begin, end)) {
				return;
			}

			if (subArray.length <= 4) {
				insertSort(subArray, begin, end);
				return;
			}

			int pivotValue = subArray[midpointOf(begin, end)];

			int lowerBound = begin;
			int upperBound = end;

			while (lowerBound < upperBound) {
				while (subArray[lowerBound] < pivotValue) {
					lowerBound++;
				}
				while (subArray[upperBound] > pivotValue) {
					upperBound--;
				}
				if (lowerBound <= upperBound) {
					swap(subArray, lowerBound, upperBound);
					lowerBound++;
					upperBound--;
				}
			}

			// Fork the subarray lower than the pivot
			if (begin < upperBound) {
				SubArray lesserThanPivotArray = new SubArray(subArray, begin, upperBound);
				Future<?> futureLesser = executor.submit(lesserThanPivotArray);
				try {
					futureLesser.get();
					// Debug to show that the sorting task is properly forking the sorting to multiple processes.
					// System.out.println("Properly forked arrays.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			// Fork the subarray greater than the pivot.
			if (end > lowerBound) {
				SubArray greaterThanPivotArray = new SubArray(subArray, lowerBound, end);
				Future<?> futureGreater = executor.submit(greaterThanPivotArray);
				try {
					futureGreater.get();
					// Debug to show that the sorting task is properly forking the sorting to multiple processes.
					// System.out.println("Properly forked arrays.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		private static void insertSort(int[] array, int begin, int end) {
			// System.out.println("Insert sort is properly called when subarray reaches <= 4 elements.");
			for (int runner = 1; runner < array.length; runner++) {
				for (int switcher = runner; switcher > 0; switcher--) {
					if (array[switcher] < array[switcher - 1]) {
						swap(array, switcher, switcher - 1);
					}
				}
			}
		}

	 	private static int midpointOf(int begin, int end) {
 			return (begin + end) / 2;
 		}

	 	private static void swap(int[] array, int begin, int end) {
	 		int temp = array[begin];
	 		array[begin] = array[end];
	 		array[end] = temp;
	 	}
	}

	public static void parallelSort(int[] array, int begin, int end){
		if (isEdgeCase(array, begin, end)) {
			return;
		}

		// "end - 1" because supplied tests provide the length of array for 'end'.
		Future<?> future = executor.submit(new SubArray(array, begin, end - 1));

		try {
			future.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}

	private static boolean isEdgeCase(int[] array, int begin, int end) {
 		if (array.length == 0 || begin >= end) {
 			return true;
 		}
 		return false;
 	}
}