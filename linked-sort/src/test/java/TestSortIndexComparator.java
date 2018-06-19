package de.sekmi.histream.scripting;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.junit.Test;


public class TestSortIndexComparator {

	private class IntComparator implements Comparator<Integer>{
		@Override
		public int compare(Integer o1, Integer o2) {
			return o1.compareTo(o2);
		}
	}
	private class CharComparator implements Comparator<Character>{
		@Override
		public int compare(Character o1, Character o2) {
			return o1.compareTo(o2);
		}
	}
	@Test
	public void verifyComparator() {		
		Character[] testData = new Character[] { 'd', 'b', 'g', 'e', 'h', 'j', 'i', 'c', 'f' , 'k', 'a'};
		
		SortIndexComparator<Character> c = new SortIndexComparator<>(new CharComparator());
		Integer[] order = c.sortIndexes(Arrays.asList(testData));
		
		System.out.println("Original: "+Arrays.toString(testData));
		
		StringBuilder b = new StringBuilder();
		int curr,prev = -1;
		for( int i=0; i<testData.length; i++ ) {
			if( i != 0 ) {
				b.append(", ");
			}
			b.append(testData[order[i]]);
			
			curr = testData[order[i]];
			assertTrue(prev <= curr);
			prev = curr;
		}
		
		System.out.println("with sortIndexes: "+b.toString());
		System.out.println("testData: "+Arrays.toString(testData));
		
		Object[] other = new Object[testData.length];
		for( int i=0; i<testData.length; i++ ) {
			other[i] = new Object();
		}
		List<Character> a = Arrays.asList(testData);
		List<Object> d = Arrays.asList(other);
		c.linkedSort(a, d);
		// output
		b = new StringBuilder();
		for( int i=0; i<a.size(); i++ ) {
			if( i != 0 ) {
				b.append(", ");
			}
			b.append(a.get(i));			
		}
		System.out.println("linkedSort.primary: "+b.toString());
		
	}
	private static class Ref{
		Integer ref;
		public Ref(Integer ref) {
			this.ref = ref;
		}
	}

	/**
	 * TODO implement and verify linked sort method
	 */
//	@Test
	public void verifyLinkedSort() {
		Random rand = new Random(42);
		// generate a random integer array
		Integer[] testData = new Integer[1000];
		// and two copies of the array
		Ref[] copy = new Ref[testData.length];
		// fill with random numbers
		// copies contain different objects but same numbers
		for( int i=0; i<testData.length; i++ ) {
			testData[i] = rand.nextInt();
			// fill copies
			copy[i] = new Ref(testData[i]);
		}

		// perform linked sort		
		SortIndexComparator<Integer> sic = new SortIndexComparator<>(new IntComparator());
		List<Integer> a = new ArrayList<>(Arrays.asList(testData));
		List<Ref> b = new ArrayList<>(Arrays.asList(copy));
		
		sic.linkedSort(a, b);
		
		Integer prev = a.get(0);
		for( int i=0; i<a.size(); i++ ){
			Integer curr = a.get(i);
			// make sure primary was sorted in ascending order
			assertTrue("Sort order violated at index "+i+": "+prev+", "+curr, prev <= curr );
			// make sure the copy was sorted linked to primary
			assertTrue( curr == b.get(i).ref );
			// next
			prev = curr;
		}
		
	}

}
