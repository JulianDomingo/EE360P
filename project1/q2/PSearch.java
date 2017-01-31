//Name=Julian Domingo
//UT-EID=jad5348

import java.util.*;
import java.util.concurrent.*;

public class PSearch {
	public static final int FAILED_SEARCH = -1;
	public static int[] arrayToSearch;

	private static class SubArray implements Callable<Integer> {
		private int begin;
		private int end;
		private int desiredValue;

		private SubArray(int begin, int end, int desiredValue) {
			this.begin = begin;
			this.end = end;
			this.desiredValue = desiredValue;
		}

		@Override
		public Integer call() {
			return searchSubArray();
		}

		// Simple linear search.
		private int searchSubArray() {
			int currentIndex = begin;
			while (currentIndex < end) {
				if (valueFoundAt(currentIndex)) {
					return currentIndex;
				}
				currentIndex++;
			}
			return FAILED_SEARCH;
		}	

		private boolean valueFoundAt(int index) {
			return arrayToSearch[index] == desiredValue;
		}
	} 

	public static int parallelSearch(int desiredValue, int[] array, int numThreads){
		arrayToSearch = array;
		ExecutorService executor = Executors.newFixedThreadPool(numThreads);
		ArrayList<Future<Integer>> searchResults = new ArrayList<Future<Integer>>(numThreads);

		if (numThreads > array.length) {
			// Excess number of needed threads. Set upper bound thread count to length of array.
			numThreads = array.length;
		}

		// Determine size of subarrays based on the thread count.
		int splitSize = (int) Math.ceil((double) array.length / (double) numThreads);

		// Create threads to search subarray of size splitSize.
		for (int index = 0; index < array.length; index += splitSize) {
			// Determine start and endpoints for each array chunk.
			int begin = index;
			int end = Math.min(array.length, index + splitSize);

			// Start the thread and gather the future object for it.
			Callable<Integer> callable = new SubArray(begin, end, desiredValue);
			Future<Integer> future = executor.submit(callable);
			searchResults.add(future);
		}

		// Initially, assume none of the contenders find the desired value.
		int contender = FAILED_SEARCH;
		// Fetch the future result of each thread.
		for (Future<Integer> result : searchResults) {
			try {
				contender = result.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				if (contender != FAILED_SEARCH) {
					return contender;
				}
			}
		}
		return FAILED_SEARCH;
	}
}