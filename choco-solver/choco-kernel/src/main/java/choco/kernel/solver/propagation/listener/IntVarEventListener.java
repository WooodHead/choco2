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

package choco.kernel.solver.propagation.listener;

import choco.kernel.solver.ContradictionException;


/**
 * An interface for all the search variable listeners.
 */
public interface IntVarEventListener extends VarEventListener {

  /**
   * Default propagation on improved lower bound: propagation on domain revision.
   */

  public void awakeOnInf(int varIdx) throws ContradictionException;


  /**
   * Default propagation on improved upper bound: propagation on domain revision.
   */

  public void awakeOnSup(int varIdx) throws ContradictionException;


  /**
   * Default propagation on instantiation: full constraint re-propagation.
   */

  public void awakeOnInst(int varIdx) throws ContradictionException;


  /**
   * Default propagation on one value removal: propagation on domain revision.
   */

  public void awakeOnRem(int varIdx, int val) throws ContradictionException;

  /**
   * <i>Network management:</i>
   * Storing that among all listeners linked to the i-th variable of c,
   * this (the current constraint) is found at index idx.
   *
   * @param i   index of the variable in the constraint
   * @param idx index of the constraint in the among all listeners linked to that variable
   */

  void setConstraintIndex(int i, int idx);

  /**
   * <i>Network management:</i>
   * Among all listeners linked to the idx-th variable of c,
   * find the index of constraint c.
   *
   * @param idx index of the variable in the constraint
   */

  int getConstraintIdx(int idx);
}