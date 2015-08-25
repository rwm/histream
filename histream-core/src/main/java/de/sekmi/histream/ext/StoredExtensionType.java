package de.sekmi.histream.ext;

/*
 * #%L
 * histream
 * %%
 * Copyright (C) 2013 - 2015 R.W.Majeed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.time.Instant;
import java.util.Enumeration;
import java.util.Iterator;


public class StoredExtensionType implements IdExtensionType, ExternalSourceType{
	private boolean dirty;
	private String id;
	// for external source
	private Instant sourceTimestamp;
	private String sourceSystemId;
	
	public final boolean isDirty(){return dirty;}
	
	public void markDirty(boolean dirty){
		this.dirty = dirty;
	}

	@Override
	public final String getId() {return id;}

	@Override
	public final void setId(String id) {
		this.id = id;
	}
	

	@Override
	public final Instant getSourceTimestamp() {
		return sourceTimestamp;
	}

	@Override
	public final void setSourceTimestamp(Instant instant) {
		this.sourceTimestamp = instant;
		markDirty(true);
	}

	@Override
	public final String getSourceId() {
		return sourceSystemId;
	}

	@Override
	public final void setSourceId(String sourceSystemId) {
		this.sourceSystemId = sourceSystemId;
		markDirty(true);
	}
	
	private static final class DirtyIterator<T extends StoredExtensionType> implements Iterator<T>{
		private Enumeration<T> all;
		private T next;
		private T prev;
		
		public DirtyIterator(Enumeration<T> all){
			this.all = all;
		}
		
		@Override
		public boolean hasNext() {
			if( next != null )
				return true; // next element already found
			
			// loop through next elements
			while( all.hasMoreElements() ){
				next = all.nextElement();
				// until a dirty one is found
				if( next.isDirty() )break;
			}
			
			if( next != null && !next.isDirty() )
				next = null; // iterated through all remaining, no dirty found

			// whether a dirty one was found
			return next != null;
		}

		@Override
		public T next() {
			prev = next;
			next = null;
			// scans for next dirty element
			hasNext();
			return prev;
		}
		
		@Override
		public void remove(){
			if( prev == null )throw new IllegalStateException();
			// removes dirty flag
			prev.markDirty(false);
			prev = null;
		}
	}
	
	/**
	 * 
	 * Get an iterator over all cached instances marked as dirty.
	 * Calling the {@link Iterator#remove()} method will clear the
	 * dirty flag for the current element (previously returned by next).
	 * @param <T> extension type
	 * 
	 * @param allElements enumerator over all types, dirty or not
	 * @return iterator which returns only dirty type instances.
	 */
	
	public static final <T extends StoredExtensionType> Iterator<T> dirtyIterator(Enumeration<T> allElements){
		return new DirtyIterator<T>(allElements);
	}
	
//	public static final <T extends StoredExtensionType> Iterable<T> dirtyElements(Hashtable<?, ? extends T> map){

}
