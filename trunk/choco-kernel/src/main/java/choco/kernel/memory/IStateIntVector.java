/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |  °(..)  |                           *
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

package choco.kernel.memory;

import java.util.logging.Logger;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.DisposableIntIterator;

/**
 * Describes an search vector with states (describing some history of the data structure).
 */
public interface IStateIntVector {

	public final static Logger LOGGER = ChocoLogging.getEngineLogger();
  /**
   * Minimal capacity of a vector
   */
  public static final int MIN_CAPACITY = 8;

  /**
   * Returns the current size of the stored search vector.
   */

  public int size();

  /**
   * Checks if the vector is empty.
   */

  public boolean isEmpty();

  /**
   * Adds a new search at the end of the vector.
   *
   * @param i The search to add.
   */

  public void add(int i);


  /**
   * Removes an int.
   *
   * @param i The search to remove.
   */

  public void remove(int i);


  /**
   * removes the search at the end of the vector.
   * does nothing when called on an empty vector
   */

  public void removeLast();

  /**
   * Returns the <code>index</code>th element of the vector.
   */

  public int get(int index);

  /**
   * Assigns a new value <code>val</code> to the element <code>index</code> and returns
   * the old value
   */

  public int set(int index, int val);
  
  public DisposableIntIterator getIterator();
}