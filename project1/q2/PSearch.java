//Name=Julian Domingo, Alec Bargas
//UT-EID=jad5348, apb973

import java.util.*;
import java.util.concurrent.*;

public class PSearch {
	public static final int FAILED_SEARCH = -1;
	public static int[] arrayToSearch;
	public int splitSize;

	public ExecutorService executor;
	public ArrayList<Future<Integer>> searchResults;
	public ArrayList<Callable<Integer>> callableThreads;

	private static class SubArray implements Callable<Integer> {
		private int start;
		private int end;
		private int desiredValue;

		private SubArray(int start, int end, int desiredValue) {
			this.start = start;
			this.end = end;
			this.desiredValue = desiredValue;
		}

		@Override
		public Integer call() {
			return searchSubArray();
		}

		// Simple linear search.
		private int searchSubArray() {
			int currentIndex = start;
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
		executor = Executors.newFixedThreadPool(numThreads);
		searchResults = new ArrayList<Future<Integer>>(numThreads);
		splitSize = determineSplitSizeFrom(numThreads);

		callableThreads = createCallableThreads();
		executeCallableThreads();

		return findDesiredValueFromSearchResults();
	}

	private ArrayList<Callable<Integer>> createCallableThreads() {
		ArrayList<Callable<Integer>> callableThreads = new ArrayList<Callable<Integer>>();
		for (int currentIndex = 0; currentIndex < array.length; currentIndex += splitSize) {
			int start = currentIndex;
			int end = Math.min(arrayToSearch.length, currentIndex + splitSize);
			Callable<Integer> callable = new SubArray(startAndEnd, desiredValue);
			callableThreads.add(callable);
		}
		return callableThreads;
	}

	private int findDesiredValueFrom(ArrayList<Future<Intger>> searchResults) {
		int contender = FAILED_SEARCH;
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

	private int determineSplitSizeFrom(int numThreads) {
		if (numThreads > arrayToSearch.length) {
			numThreads = arrayToSearch.length;
		}
		int splitSize = (int) Math.ceil((double) arrayToSearch.length / (double) numThreads);
	}
}