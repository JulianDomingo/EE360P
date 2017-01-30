//Name=Julian Domingo
//UT-EID=jad5348

import java.util.*;
import java.util.concurrent.*;

public class PSearch {
	public static final int INDEX_NOT_FOUND = -1;
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

		private int searchSubArray() {
			while (begin < end) {
				if (valueFound(begin)) {
					return begin;
				}
				begin++;
			}
			return INDEX_NOT_FOUND;
		}	

		private boolean valueFound(int index) {
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

		int splitSize = (int) Math.ceil((double) array.length / (double) numThreads);

		// Split array into subarrays based on splitSize.
		for (int index = 0; index < array.length; index += splitSize) {
			int begin = index;
			int end = Math.min(array.length, index + splitSize);

			Callable<Integer> callable = new SubArray(begin, end, desiredValue);
			Future<Integer> future = executor.submit(callable);
			searchResults.add(future);
		}

		int contender = INDEX_NOT_FOUND;
		for (Future<Integer> result : searchResults) {
			try {
				contender = result.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} finally {
				if (contender != INDEX_NOT_FOUND) {
					return (int) contender;
				}
			}
		}

		return contender;
	}
}