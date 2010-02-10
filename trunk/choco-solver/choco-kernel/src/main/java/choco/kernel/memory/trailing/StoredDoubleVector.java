/* * * * * * * * * * * * * * * * * * * * * * * * *
 *          _       _                            *
 *         |  �(..)  |                           *
 *         |_  J||L _|        CHOCO solver       *
 *                                               *
 *    Choco is a java library for constraint     *
 *    satisfaction problems (CSP), constraint    *
 *    programming (CP) and explanation-based     *
 *    constraint solving (e-CP). It is built     *
 *    on a event-based propagation mechanism     *
 *    with backtrackable structures.             *
 *                                               *
 *    Choco is an open-source software,          *
 *    distributed under a BSD licence            *
 *    and hosted by sourceforge.net              *
 *                                               *
 *    + website : http://choco.emn.fr            *
 *    + support : choco@emn.fr                   *
 *                                               *
 *    Copyright (C) F. Laburthe,                 *
 *                  N. Jussien    1999-2008      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.kernel.memory.trailing;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateDoubleVector;
import choco.kernel.memory.trailing.trail.StoredDoubleVectorTrail;

/**
 * Implements a backtrackable search vector.
 * <p/>
 * Cette classe permet de stocker facilment des entiers dans un tableau
 * backtrackable d'entiers.
 */
public final class StoredDoubleVector implements IStateDoubleVector {


	/**
	 * Minimal capacity of a vector
	 */
	public static final int MIN_CAPACITY = 8;

	/**
	 * Contains the elements of the vector.
	 */

	private double[] elementData;

	/**
	 * Contains time stamps for all entries (the world index of the last update for each entry)
	 */

    public int[] worldStamps;

	/**
	 * A backtrackable search with the size of the vector.
	 */

	private StoredInt size;


	/**
	 * The current environment.
	 */

	private final EnvironmentTrailing environment;


	/**
	 * Constructs a stored search vector with an initial size, and initial values.
	 *
	 * @param env          The current environment.
	 * @param initialSize  The initial size.
	 * @param initialValue The initial common value.
	 */

	public StoredDoubleVector(EnvironmentTrailing env, int initialSize, double initialValue) {
		int initialCapacity = MIN_CAPACITY;
		int w = env.getWorldIndex();

		if (initialCapacity < initialSize)
			initialCapacity = initialSize;

		this.environment = env;
		this.elementData = new double[initialCapacity];
		this.worldStamps = new int[initialCapacity];
		for (int i = 0; i < initialSize; i++) {
			this.elementData[i] = initialValue;
			this.worldStamps[i] = w;
		}
		this.size = new StoredInt(env, initialSize);
	}


	public StoredDoubleVector(EnvironmentTrailing env, double[] entries) {
		int initialCapacity = MIN_CAPACITY;
		int w = env.getWorldIndex();
		int initialSize = entries.length;

		if (initialCapacity < initialSize)
			initialCapacity = initialSize;

		this.environment = env;
		this.elementData = new double[initialCapacity];
		this.worldStamps = new int[initialCapacity];
		for (int i = 0; i < initialSize; i++) {
			this.elementData[i] = entries[i]; // could be a System.arrayCopy but since the loop is needed...
			this.worldStamps[i] = w;
		}
		this.size = new StoredInt(env, initialSize);
	}

	/**
	 * Constructs an empty stored search vector.
	 *
	 * @param env The current environment.
	 */

	public StoredDoubleVector(EnvironmentTrailing env) {
		this(env, 0, 0);
	}

	private boolean rangeCheck(int index) {
		return index < size.get() && index >= 0;
	}

	/**
	 * Returns the current size of the stored search vector.
	 */

	public int size() {
		return size.get();
	}


	/**
	 * Checks if the vector is empty.
	 */

	public boolean isEmpty() {
		return (size.get() == 0);
	}

	/*    public Object[] toArray() {
        // TODO : voir ci c'est utile
        return new Object[0];
    }*/


	/**
	 * Checks if the capacity is great enough, else the capacity
	 * is extended.
	 *
	 * @param minCapacity the necessary capacity.
	 */

	public void ensureCapacity(int minCapacity) {
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			double[] oldData = elementData;
			int[] oldStamps = worldStamps;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = new double[newCapacity];
			worldStamps = new int[newCapacity];
			System.arraycopy(oldData, 0, elementData, 0, size.get());
			System.arraycopy(oldStamps, 0, worldStamps, 0, size.get());
		}
	}


	/**
	 * Adds a new search at the end of the vector.
	 *
	 * @param i The search to add.
	 */

	public void add(double i) {
		int newsize = size.get() + 1;
		ensureCapacity(newsize);
		size.set(newsize);
		elementData[newsize - 1] = i;
		worldStamps[newsize - 1] = environment.getWorldIndex();
	}

	/**
	 * Removes an int.
	 *
	 * @param i The search to remove.
	 */
	@Override
	public void remove(int i) {
		System.arraycopy(elementData, i, elementData, i+1, size.get());
		System.arraycopy(worldStamps, i, worldStamps, i+1, size.get());

		//        for(int j = i; j < size.get()-1; j++){
		//            elementData[j] = elementData[j+1];
		//            worldStamps[j] = worldStamps[j+1];
		//        }
		int newsize = size.get() - 1;
		if (newsize >= 0)
			size.set(newsize);
	}

	/**
	 * removes the search at the end of the vector.
	 * does nothing when called on an empty vector
	 */

	public void removeLast() {
		int newsize = size.get() - 1;
		if (newsize >= 0)
			size.set(newsize);
	}

	/**
	 * Returns the <code>index</code>th element of the vector.
	 */

	public double get(int index) {
		if (rangeCheck(index)) {
			return elementData[index];
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}

    @Override
    public double quickGet(int index) {
    	assert(rangeCheck(index));
    	return elementData[index];
    }


    /**
	 * Assigns a new value <code>val</code> to the element <code>index</code>.
	 */

	public double set(int index, double val) {
		if (rangeCheck(index)) {
			//<hca> je vire cet assert en cas de postCut il n est pas vrai ok ?
			//assert(this.worldStamps[index] <= environment.getWorldIndex());
			return quickSet(index, val);
			
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}                                                                               


    public double quickSet(int index, double val)
    {
    	assert(rangeCheck(index));
    	double oldValue = elementData[index];
        if (val != oldValue) {
		    int oldStamp = this.worldStamps[index];
			if (oldStamp < environment.getWorldIndex()) {
			    environment.savePreviousState(this, index, oldValue, oldStamp);
				worldStamps[index] = environment.getWorldIndex();
		    }
		    elementData[index] = val;
		}
        return oldValue;

    }


	/**
	 * Sets an element without storing the previous value.
     * @param index  Index where to set
     * @param val   value to be changed
     * @param stamp world when the modification is done
     * @return the old value
     */

    public double _set(int index, double val, int stamp) {
    	assert(rangeCheck(index));
    	double oldval = elementData[index];
		elementData[index] = val;
		worldStamps[index] = stamp;
		return oldval;
	}


	@Override
	public DisposableIntIterator getIterator() {
		throw new UnsupportedOperationException("not yet implemented");
	}


}