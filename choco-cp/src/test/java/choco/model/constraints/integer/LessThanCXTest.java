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
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

/* File choco.currentElement.search.GreaterOrEqualXCTest.java, last modified by Francois 23 ao�t 2003:17:40:29 */
package choco.model.constraints.integer;

import static choco.Choco.lt;
import static choco.Choco.makeIntVar;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.AbstractSConstraint;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

public class LessThanCXTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private CPModel m;
  private CPSolver s;
  private IntegerVariable x;
  private IntegerVariable y;
  private Constraint c1;
  private Constraint c2;

    @Before
  public void setUp() {
    LOGGER.fine("GreaterOrEqualXCTest Testing...");
    m = new CPModel();
    s = new CPSolver();
    x = makeIntVar("X", 1, 5);
    y = makeIntVar("Y", 1, 5);
        m.addVariables("cp:bound", x, y);
    c1 = lt(x, 2);
    c2 = lt(y, 3);
  }

    @After
  public void tearDown() {
    c1 = null;
    c2 = null;
    x = null;
    y = null;
    m = null;
    s = null;
  }

    @Test
  public void test1() {
    LOGGER.finer("test1");
    try {
      m.addConstraints(c1, c2);
      s.read(m);
      s.propagate();
      assertTrue(s.getVar(x).isInstantiated());
      assertFalse(s.getVar(y).isInstantiated());
      assertEquals(1, s.getVar(x).getInf());
      assertEquals(1, s.getVar(y).getInf());
      LOGGER.finest("domains OK after first propagate");
      assertTrue(((AbstractSConstraint)s.getCstr(c1)).isConsistent());
      assertTrue(((AbstractSConstraint)s.getCstr(c2)).isConsistent());
    } catch (ContradictionException e) {
      assertTrue(false);
    }
  }
}