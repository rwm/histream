package de.sekmi.histream.scripting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class SortIndexComparator<T> {

	private Comparator<T> comparator;

	public SortIndexComparator(Comparator<T> comparator){
		this.comparator = comparator;
	}

	/**
	 * Determine the sort index for a list without changing the sort order
	 * @param list list
	 * @return sort order
	 */
	public Integer[] sortIndexes(List<T> list) {
		Integer[] indices = new Integer[list.size()];
		// fill array with sequence
		for( int i=0; i<indices.length; i++ ){
			indices[i] = i;
		}
		// determine sort order
		Arrays.sort(indices, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return comparator.compare(list.get(o1), list.get(o2));
			}
		});
		return indices;
		
		// convert to basic array
//		int[] order = new int[indices.length];
//		for( int i=0; i<indices.length; i++ ) {
//			order[i] = indices[i];
//		}
//		return order;
	}

	/**
	 * Sort two lists. The primary list determines the sort order and the second
	 * list is sorted in the same order as the primary.
	 * @param primary primary list to sort
	 * @param linked linked list to be sorted in the same order as the primary list
	 */
	// this implementation does not work
//	public <U> void linkedSort(List<T> primary, List<U> linked){
//		Integer[] indices = sortIndexes(primary);
//		for( int i=0; i<indices.length; i++ ){
//			while( i != indices[i] ){
//
//				// store old target values which will be overridden
//				int oldI = indices[indices[i]];
//				T oldF = primary.get(indices[i]);
//				U oldO = linked.get(indices[i]);
//
//				// replace target values
//				primary.set(indices[i], primary.get(i));
//				linked.set(indices[i], linked.get(i));
//				indices[indices[i]] = indices[i];
//
//				// move old targets to old values
//				indices[i] = oldI;
//				primary.set(i, oldF);
//				linked.set(i, oldO);
//			}
//		}
//	}

//	public <U> void linkedSortNew(List<T> primary, List<U> linked){
//		// calculate sort indices
//		Integer[] indices = sortIndexes(primary);
//		
//		// first pass: transform index to destination
//		/* write 0 at index position index[0],
//		 * store old value and write 
//		 */
//		int j=0;
//		int p=0;
//		int t;
//		t = indices[0];
//		do {
//			t = indices[j];
//			indices[j] = p;
//			j = t;
//		}
//		
//		// TODO clone lists and use random access 
//		for( int i=0; i<indices.length; i++ ){
//			while( i != indices[i] ){
//
//				// store old target values which will be overridden
//				int oldI = indices[indices[i]];
//				T oldF = primary.get(i);
//				U oldO = linked.get(i);
//
//				primary.set(i, primary.get(indices[i]));
//				linked.set(i, linked.get(indices[i]));
//				
//				primary.set(indices[i], oldF);
//				linked.set(indices[i], oldO);
//
//
//				indices[i]
//				
//				// replace target values
//				primary.set(indices[i], primary.get(i));
//				linked.set(indices[i], linked.get(i));
//				indices[indices[i]] = indices[i];
//
//				// move old targets to old values
//				indices[i] = oldI;
//				primary.set(i, oldF);
//				linked.set(i, oldO);
//			}
//		}
//	}

}
