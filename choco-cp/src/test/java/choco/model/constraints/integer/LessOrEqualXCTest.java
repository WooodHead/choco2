/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.model.constraints.integer;

import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.propagation.Propagator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.leq;
import static choco.Choco.makeIntVar;
import static org.junit.Assert.*;


/* File choco.currentElement.search.LessOrEqualXC.java, last modified by Francois 11 sept. 2003 00:46:25 */

public class LessOrEqualXCTest {
	private final static Logger LOGGER = ChocoLogging.getTestLogger();
  private CPModel m;
    private CPSolver s;
  private IntegerVariable x;
  private IntegerVariable y;
  private IntegerVariable z;
  private Constraint c1;
  private Constraint c2;

    @Before
  public void setUp() {
    LOGGER.finer("LessOrEqualXC Testing...");
    m = new CPModel();
        s = new CPSolver();
    x = makeIntVar("X", 1, 5);
    y = makeIntVar("Y", 1, 5);
        m.addVariables(Options.V_BOUND, x, y);
    c1 = leq(x, 1);
    c2 = leq(y, 2);
  }

    @After
  public void tearDown() {
    c1 = null;
    c2 = null;
    x = null;
    y = null;
    z = null;
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
    } catch (ContradictionException e) {
      assertTrue(false);
    }
    assertTrue(s.getVar(x).isInstantiated());
    assertFalse(s.getVar(y).isInstantiated());
    assertEquals(1, s.getVar(x).getSup());
    assertEquals(2, s.getVar(y).getSup());
    LOGGER.finest("domains OK after first propagate");
    assertTrue(s.getCstr(c1).isSatisfied());
    assertEquals(((Propagator) s.getCstr(c2)).isEntailed(), Boolean.TRUE);
  }
}
