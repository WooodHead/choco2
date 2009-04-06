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
package choco.cp.solver.variables.set;

import choco.kernel.common.util.IntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.set.SetSubDomain;
import choco.kernel.solver.variables.set.SetVar;

import java.util.logging.Logger;

/*
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Since : Choco 2.0.0
 *
 */
public class BitSetEnumeratedDomain implements SetSubDomain {
  protected static final Logger logger = Logger.getLogger("choco.kernel.solver.propagation");

    /**
     * The (optimization or decision) model to which the entity belongs.
     */

    public Solver solver;
    
  /**
   * The offset, that is the minimal value of the domain (stored at index 0).
   * Thus the entry at index i corresponds to x=i+offset).
   */

  protected final int offset;

  /**
   * Number of present values.
   */

  protected IStateInt size;


  /**
   * A bit set indicating for each value whether it is present or not
   */

  protected IStateBitSet contents;

  /**
   * the initial size of the domain (never increases)
   */
  private int capacity;

  /**
   * A chained list implementing two subsets of values:
   * - the removed values waiting to be propagated
   * - the removed values being propagated
   * (each element points to the index of the enxt element)
   * -1 for the last element
   */
  protected int[] chain;

  /**
   * start of the chain for the values waiting to be propagated
   * -1 for empty chains
   */
  protected int firstIndexToBePropagated;

  /**
   * start of the chain for the values being propagated
   * -1 for empty chains
   */
  protected int firstIndexBeingPropagated;

  /**
   * Constructs a new domain for the specified variable and bounds.
   *
   * @param v    The involved variable.
   * @param a    Minimal value.
   * @param b    Maximal value.
   * @param full indicate if the initial bitSetDomain is full or empty (env or ker)
   */

  public BitSetEnumeratedDomain(SetVar v, int a, int b, boolean full) {
    solver = v.getSolver();
    IEnvironment env = solver.getEnvironment();
    capacity = b - a + 1;           // number of entries
    this.offset = a;
    if (full)
      size = env.makeInt(capacity);
    else
      size = env.makeInt(0);
    contents = env.makeBitSet(capacity);
    if (full) {
      for (int i = 0; i < capacity; i++)
        contents.set(i);
    }
    chain = new int[capacity];
    firstIndexToBePropagated = -1;
    firstIndexBeingPropagated = -1;
  }

  public BitSetEnumeratedDomain(SetVar v, int[] sortedValues, boolean full) {
      solver = v.getSolver();
      IEnvironment env = solver.getEnvironment();
      int a = sortedValues[0];
      int b = sortedValues[sortedValues.length - 1];
      capacity = b - a + 1;           // number of entries
      this.offset = a;
      if (full) {
          size = env.makeInt(sortedValues.length);
      } else {
          size = env.makeInt(0);
      }
      contents = env.makeBitSet(capacity);
      if (full) {
          for (int i = 0; i < sortedValues.length; i++) {  // TODO : could be improved...
              contents.set(sortedValues[i] - a);
          }
      }
      chain = new int[capacity];
      firstIndexToBePropagated = -1;
      firstIndexBeingPropagated = -1;
  }

    /**
     * Specific constructor for set variable with empty domain
     * @param v
     */
    private BitSetEnumeratedDomain(SetVar v) {
      solver = v.getSolver();
      IEnvironment env = solver.getEnvironment();
      capacity = 0;           // number of entries
      this.offset = 0;
      size = env.makeInt(0);
      contents = env.makeBitSet(capacity);
      chain = new int[capacity];
      firstIndexToBePropagated = -1;
      firstIndexBeingPropagated = -1;
  }


    /**
     * Specific constructor for empty set variable
     * @param v the set variable with no value
     * @return empty BitSetEnumeratedDomain
     */
    public static BitSetEnumeratedDomain empty(SetVar v) {
        return new BitSetEnumeratedDomain(v);
    }


  /**
   * Returns the minimal present value.
   */
  public int getFirstVal() {
    if (size.get() > 0)
      return contents.nextSetBit(0) + offset;
    else
      return -1;
  }


  /**
   * Returns the maximal present value.
   */

  public int getLastVal() {
    if (size.get() > 0)
      return contents.prevSetBit(capacity - 1) + offset;
    else
      return -1;
  }

  /**
   * Checks if the value is present.
   *
   * @param x The value to check.
   */

  public boolean contains(int x) {
    int i = x - offset;
    return (i >= 0 && i < capacity && contents.get(i));
  }

  /**
   * Removes a value.
   */

  public boolean remove(int x) {
    int i = x - offset;
    if (contents.get(i)) {
      removeIndex(i);
      return true;
    } else {
      return false;
    }
  }

  private void removeIndex(int i) {
    if (i == firstIndexToBePropagated)
      logger.severe("RemoveIndex BIZARRE !!!!!!!!!!!!");
    contents.clear(i);
    chain[i] = firstIndexToBePropagated;
    firstIndexToBePropagated = i;
    if (contents.get(i))
      logger.severe("etrange etrange");
    size.add(-1);
  }


  /**
   * add a value.
   */

  public boolean add(int x) {
    int i = x - offset;
    if (!contents.get(i)) {
      addIndex(i);
      return true;
    } else {
      return false;
    }
  }

  private void addIndex(int i) {
    if (i == firstIndexToBePropagated)
      logger.severe("AddIndex BIZARRE !!!!!!!!!!!!");
    contents.set(i);
    chain[i] = firstIndexToBePropagated;
    firstIndexToBePropagated = i;
    if (!contents.get(i))
      logger.severe("etrange etrange");
    size.add(1);
  }

  /**
   * Returns the current size of the domain.
   */

  public int getSize() {
    return size.get();
  }


  /**
   * Returns the value following <code>x</code>
   * if non exist return -1
   */

  public int getNextValue(int x) {
    int i = x - offset;
    int val = contents.nextSetBit(i + 1);
    if (val > 0)
      return val + offset;
    else
      return -1;
  }


  /**
   * Returns the value preceding <code>x</code>
   * if non exist return -1
   */

  public int getPrevValue(int x) {
    int i = x - offset;
    int val = contents.prevSetBit(i - 1);
    if (val > 0)
      return val + offset;
    else
      return -1;
  }


  /**
   * Checks if the value has a following value.
   */

  public boolean hasNextValue(int x) {
    int i = x - offset;
    return (contents.nextSetBit(i + 1) != -1);
  }


  /**
   * Checks if the value has a preceding value.
   */

  public boolean hasPrevValue(int x) {
    int i = x - offset;
    return (contents.prevSetBit(i - 1) != -1);
  }

  public IntIterator getDeltaIterator() {
    return new BitSetEnumeratedDomain.DeltaDomainIterator(this);
  }

  protected class DeltaDomainIterator implements IntIterator {
    protected BitSetEnumeratedDomain domain;
    protected int currentIndex = -1;

    private DeltaDomainIterator(BitSetEnumeratedDomain dom) {
      domain = dom;
      currentIndex = -1;
    }

    public boolean hasNext() {
      if (currentIndex == -1) {
        return (firstIndexBeingPropagated != -1);
      } else {
        return (chain[currentIndex] != -1);
      }
    }

    public int next() {
      if (currentIndex == -1) {
        currentIndex = firstIndexBeingPropagated;
      } else {
        currentIndex = chain[currentIndex];
      }
      return currentIndex + offset;
    }

    public void remove() {
      if (currentIndex == -1) {
        throw new IllegalStateException();
      } else {
        throw new UnsupportedOperationException();
      }
    }
  }

  /**
   * The delta domain container is "frozen" (it can no longer accept new value removals)
   * so that this set of values can be iterated as such
   */
  public void freezeDeltaDomain() {
    // freeze all data associated to bounds for the the event
    //super.freezeDeltaDomain();
    // if the delta domain is already being iterated, it cannot be frozen
    if (firstIndexBeingPropagated != -1) {
    }//throw new IllegalStateException();
    else {
      // the set of values waiting to be propagated is now "frozen" as such,
      // so that those value removals can be iterated and propagated
      firstIndexBeingPropagated = firstIndexToBePropagated;
      // the container (link list) for values waiting to be propagated is reinitialized to an empty set
      firstIndexToBePropagated = -1;
    }
  }

  /**
   * after an iteration over the delta domain, the delta domain is reopened again.
   *
   * @return true iff the delta domain is reopened empty (no updates have been made to the domain
   *         while it was frozen, false iff the delta domain is reopened with pending value removals (updates
   *         were made to the domain, while the delta domain was frozen).
   */
  public boolean releaseDeltaDomain() {
    // release all data associated to bounds for the the event
    //super.releaseDeltaDomain();
    // special case: the set of removals was not being iterated (because the variable was instantiated, or a bound was updated)
    if (firstIndexBeingPropagated == -1) {
      // remove all values that are waiting to be iterated
      firstIndexToBePropagated = -1;
      // return true because the event has been "flushed" (nothing more is awaiting)
      return true;
    } else { // standard case: the set of removals was being iterated
      // empty the set of values that were being propagated
      firstIndexBeingPropagated = -1;
      // if more values are waiting to be propagated, return true
      return (firstIndexToBePropagated == -1);
    }
  }

  public boolean getReleasedDeltaDomain() {
    return ((firstIndexBeingPropagated == -1) && (firstIndexToBePropagated == -1));
  }

  /**
   * cleans the data structure implementing the delta domain
   */
  public void clearDeltaDomain() {
    firstIndexBeingPropagated = -1;
    firstIndexToBePropagated = -1;
  }
}
