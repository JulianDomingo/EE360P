//Name=Julian Domingo
//UT-EID=jad5348

import java.util.*;
import java.util.concurrent.*;

public class PSort {
	private static ExecutorService executor = Executors.newFixedThreadPool(10000);

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

			// Instructed to sequentially sort the array at upper bound 4 elements.
			if (Math.abs(end - begin) < 4) {
				insertSort(begin, end);
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
					swap(lowerBound, upperBound);
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
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		private void insertSort(int begin, int end) {
			for (int runner = begin + 1; runner < end + 1; runner++) {
				for (int switcher = runner; switcher > 0; switcher--) {
					if (subArray[switcher] < subArray[switcher - 1]) {
						swap(switcher, switcher - 1);
					}
				}
			}
		}

	 	private static int midpointOf(int begin, int end) {
 			return (begin + end) / 2;
 		}

	 	private void swap(int begin, int end) {
	 		int temp = subArray[begin];
	 		subArray[begin] = subArray[end];
	 		subArray[end] = temp;
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