package com.describer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This set is unchangeable and throws UnsupportedOperationException
 * when you try to change it.
 * 
 * @author Mike V
 *
 * @param <E>
 */
public class LazySubSets<E> implements Set<Set<E>> {

    private final LinkedHashSet<E> baseSet;
    
    private final List<E> baseSetAsList;
    
    private final int intSize;
    
    private final int baseSetSize;
    
    private final long longSize;

    public LazySubSets(Set<E> baseSet) {
	this.baseSet = new LinkedHashSet<E>(baseSet);
	baseSetAsList = new ArrayList<E>(baseSet);
	baseSetSize = baseSet.size();
	longSize = (long) Math.pow(2, baseSetSize);
	intSize = longSize > Integer.MAX_VALUE ? -1 : (int) longSize;
    }

    /**
     * Returns the size of this set, or -1, if
     * the size is a long value.
     * @see LazySubSets#longSize()
     */
    @Override
    public int size() {
	return intSize;
    }

    /**
     * Returns the size of this set.
     * @return
     */
    public long longSize() {
	return longSize;
    }

    @Override
    public boolean isEmpty() {
	return false;
    }

    @Override
    public boolean contains(Object o) {
	if (o instanceof Set<?>) {
	    return baseSet.containsAll((Set<?>) o);
	}
	return false;
    }

    @Override
    public Iterator<Set<E>> iterator() {
	class Iter implements Iterator<Set<E>> {
	    
	    private List<Byte> indList = new ArrayList<Byte>();
	    
	    boolean lastFlag = true;

	    @Override
	    public boolean hasNext() {	
		if (indList.size() < baseSetSize)
		    return true;
		if (lastFlag) {
		    lastFlag = false;
		    return true;
		}
		return false;
	    }

	    @Override
	    public Set<E> next() {
		Set<E> subset = new HashSet<E>();
		for (byte b : indList) {
		    subset.add(baseSetAsList.get(b));
		}
		shift(indList, baseSetSize - 1);
		return subset;
	    }
	    
	    private void shift(List<Byte> list, int pos) {
		byte i = 0;
		if (list.size() == 0) {
		    list.add((byte) 0);
		    return;
		}
		while (i < list.size() - 1 &&
			list.get(i) + 1 == list.get(i + 1))
		    i++;
		if (list.get(i) < pos) {
		    list.set(i, (byte) (list.get(i) + 1));
		    while (i > 0) {
			i--;
			list.set(i, i);
		    }
		} else if (list.size() - 1 < pos) {
		    
		    while (i >= 0) {		
			list.set(i, i);
			i--;
		    }
		    list.add((byte) list.size());
		}
	    }

	    @Override
	    public void remove() {
		throw new UnsupportedOperationException();		
	    }
	    
	}
	return new Iter();
    }

    @Override
    public Object[] toArray() {
	throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Set<E> e) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
	for (Object o : c) {
	    if (!contains(o))
		return false;
	}
	return true;
    }

    @Override
    public boolean addAll(Collection<? extends Set<E>> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
	throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
	throw new UnsupportedOperationException();
    }
    
    // testing
    public static void main(String[]strs) {
	Set<Set<Integer>> set = new LazySubSets<Integer>(new HashSet<Integer>(java.util.Arrays.asList(1,2,3,4,5,6,7,8)));
	for (Set<Integer> i : set) {
	    System.out.println(i);
	}

    }

}