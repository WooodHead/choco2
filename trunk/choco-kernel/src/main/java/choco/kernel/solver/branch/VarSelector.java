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
package choco.kernel.solver.branch;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.AbstractVar;

/**
 * an interface for objects controlling the selection of a variable (for heuristic purposes)
 */
public interface VarSelector {
  /**
   * each VarSelector is associated to a branching strategy
   *
   * @return the associated branching strategy
   */
  public IntBranching getBranching();

  /**
   * the VarSelector can be asked to return a variable
   *
   * @return a variable on whose domain an alternative can be set (such as a non instantiated search variable)
   */
  public AbstractVar selectVar() throws ContradictionException;
}