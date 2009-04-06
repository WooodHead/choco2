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
package choco.kernel.solver.variables;

import choco.kernel.common.HashCoding;
import choco.kernel.common.util.DisposableIntIterator;
import choco.kernel.common.util.IntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.PartiallyStoredIntVector;
import choco.kernel.memory.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.propagation.VarEvent;

import java.util.HashMap;
import java.util.Iterator;
/** History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * */
/**
 * An abstract class for all implementations of domain variables.
 */
public abstract class AbstractVar implements Var {

    /**
     * The (optimization or decision) model to which the entity belongs.
     */

    public Solver solver;


  /**
   * A name may be associated to each variable.
   */
  protected String name;


    private int index = -1;

  /**
   * The variable var associated to this variable.
   */
  protected VarEvent<? extends AbstractVar> event;


  /**
   * The list of constraints (listeners) observing the variable.
   */
  protected PartiallyStoredVector<SConstraint> constraints;

    /**
   * List of indices encoding the constraint network.
   * <i>v.indices[i]=j</i> means that v is the j-th variable
   * of its i-th constraint.
   */
  protected PartiallyStoredIntVector indices;

    /**
   * The number of extensions registered to this class
   */
  private static int ABSTRACTVAR_EXTENSIONS_NB = 0;

  /**
   * The set of registered extensions (in order to deliver one and only one index for each extension !)
   */
  private static final HashMap<String, Integer> REGISTERED_ABSTRACTVAR_EXTENSIONS = new HashMap<String, Integer>();

  /**
   * Returns a new number of extension registration
   * @param name A name for the extension (should be an UID, like the absolute path for instance)
   * @return a number that can be used for specifying an extension (setExtension method)
   */
  public static int getAbstractVarExtensionNumber(String name) {
    Integer ind = REGISTERED_ABSTRACTVAR_EXTENSIONS.get(name);
    if (ind == null) {
      ind = ABSTRACTVAR_EXTENSIONS_NB++;
      REGISTERED_ABSTRACTVAR_EXTENSIONS.put(name, ind);
    }
    return ind;
  }

  /**
   * The extensions of this constraint, in order to add some data linked to this constraint (for specific algorithms)
   */
  public Object[] extensions = new Object[4];

  public String getName() {
	  return name;
  }

  /**
   * Initializes a new variable.
   * @param solver The model this variable belongs to
   * @param name The name of the variable
   */
  public AbstractVar(final Solver solver, final String name) {
    this.solver =solver;
    this.name = name;
    IEnvironment env = solver.getEnvironment();
    constraints = env.makePartiallyStoredVector();
    indices = env.makePartiallyStoredIntVector();
      index = solver.getIndexfactory().getIndex();
  }


    @Override
	public int hashCode() {
		return HashCoding.hashCodeMe(new Object[]{index});
	}

    /**
     * Unique index of an object in the master object
     * (Different from hashCode, can change from one execution to another one)
     *
     * @return
     */
    @Override
    public int getIndexIn(int masterIndex) {
        return index;
    }

    /**
     * Attribute the value of the index
     *
     * @param ind
     */
    @Override
    public void setIndexIn(int masterInd, int ind) {
        index = ind;
    }


    /**
   * Adds a new extension.
   * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
   * @param extension the extension to store to add some algorithm specific data
   */
  public void setExtension(int extensionNumber, Object extension) {
    if (extensionNumber >= extensions.length) {
      Object[] newArray = new Object[extensions.length * 2];
      System.arraycopy(extensions, 0, newArray, 0, extensions.length);
      extensions = newArray;
    }
    extensions[extensionNumber] = extension;
  }

  /**
   * Returns the queried extension
   * @param extensionNumber should use the number returned by getAbstractSConstraintExtensionNumber
   * @return the queried extension
   */
  public Object getExtension(int extensionNumber) {
    return extensions[extensionNumber];
  }

  /**
   * Useful for debugging.
   * @return the name of the variable
   */
  public String toString() {
    return name;
  }

  /**
   * Returns the variable event.
   * @return the event responsible for propagating variable modifications
   */
  public VarEvent<? extends AbstractVar> getEvent() {
    return event;
  }


  /**
   * Retrieve the constraint i involving the variable.
   * Be careful to use the correct constraint index (constraints are not
   * numbered from 0 to number of constraints minus one, since an offset
   * is used for some of the constraints).
   * @param i the number of the required constraint
   * @return the constraint number i according to the variable
   */
  public SConstraint getConstraint(final int i) {
    return constraints.get(i);
  }


  /**
   * Returns the number of constraints involving the variable.
   * @return the number of constraints containing this variable
   */
  public int getNbConstraints() {
    return constraints.size();
  }

  /**
   * Access the data structure storing constraints involving a given variable.
   * @return the backtrackable structure containing the constraints
   */
  public PartiallyStoredVector<SConstraint> getConstraintVector() {
    return constraints;
  }

  /**
   * Access the data structure storing indices associated to constraints 
   * involving a given variable.
   * @return the indices associated to this variable in each constraint
   */
  public PartiallyStoredIntVector getIndexVector() {
    return indices;
  }

  /**
   * Returns the index of the variable in its constraint i.
   * @param constraintIndex the index of the constraint 
   * (among all constraints linked to the variable)
   * @return the index of the variable
   */
  public int getVarIndex(final int constraintIndex) {
    return indices.get(constraintIndex);
  }

  /**
   * Removes (permanently) a constraint from the list of constraints 
   * connected to the variable.
   * @param c the constraint that should be removed from the list this variable
   * maintains.
   */
  public void eraseConstraint(final SConstraint c) {
    int idx = constraints.remove(c);
    indices.remove(idx);
  }

  // ============================================
  // Managing Listeners.
  // ============================================

  /**
   * Adds a new constraints on the stack of constraints
   * the addition can be dynamic (undone upon backtracking) or not.
   * @param c the constraint to add
   * @param varIdx the variable index accrding to the added constraint
   * @param dynamicAddition states if the addition is definitic (cut) or
   * subject to backtracking (standard constraint)
   * @return the index affected to the constraint according to this variable
   */
  public int addConstraint(final SConstraint c, final int varIdx,
                           final boolean dynamicAddition) {
    int constraintIdx;
    if (dynamicAddition) {
      constraintIdx = constraints.add(c);
      indices.add(varIdx);
    } else {
      constraintIdx = constraints.staticAdd(c);
      indices.staticAdd(varIdx);
    }
    return constraintIdx;
  }

  /**
   * This methods should be used if one want to access the different constraints
   * currently posted on this variable.
   *
   * Indeed, since indices are not always
   * consecutive, it is the only simple way to achieve this.
   *
   * Warning ! this iterator should not be used to remove elements.
   * The <code>remove</code> method throws an
   * <code>UnsupportedOperationException</code>.
   *
   * @return an iterator over all constraints involving this variable
   */
  public Iterator<SConstraint> getConstraintsIterator() {
    return new Iterator<SConstraint>() {
      IntIterator indices = constraints.getIndexIterator();

      public boolean hasNext() {
        return indices.hasNext();
      }

      public SConstraint next() {
        return constraints.get(indices.next());
      }

      public void remove() {
        throw new UnsupportedOperationException();
      }
    };

  }

    /**
     * * CPRU 07/12/2007: DomOverFailureDeg implementation
     * This methods at least raise the number of failure.
     *
     * @throws choco.kernel.solver.ContradictionException
     */
    public void fail() throws ContradictionException {
    }


    /**
     * CPRU 07/12/2007: DomOverFailureDeg implementation
     * This method takes into account the instantiation of the variables.
     * CPRU_BUG_1877365
     *
     */
    public void updateNbVarInstanciated(){
      DisposableIntIterator cstIter = constraints.getIndexIterator();
      while(cstIter.hasNext()) {
        AbstractSConstraint cstr = (AbstractSConstraint) constraints.get(cstIter.next());
        cstr.decNbVarNotInst();
      }
      cstIter.dispose();
    }

}
