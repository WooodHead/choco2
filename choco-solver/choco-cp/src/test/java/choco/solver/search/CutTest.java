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
package choco.solver.search;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;
// **************************************************
// *                   J-CHOCO                      *
// *   Copyright (C) F. Laburthe, 1999-2003         *
// **************************************************
// *  an open-source Constraint Programming Kernel  *
// *     for Research and Education                 *
// **************************************************

public class CutTest {
    protected static final Logger LOGGER = ChocoLogging.getTestLogger();
    private Model m;
    private CPSolver s;
    private IntegerVariable v1;
    private IntegerVariable v2;
    private IntegerVariable v3;

    @Before
    public void setUp() {
        LOGGER.fine("StoredInt Testing...");
        m = new CPModel();
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);
        m.addVariables(v1, v2);
        s.read(m);
    }

    @After
    public void tearDown() {
        v1 = null;
        v2 = null;
        v3 = null;
        m = null;
        s = null;
    }

    /**
     * one cut that sets the model to the first solution -> no more solutions afterwards
     */
    @Test
    public void test1() {
        LOGGER.info("test1");
        LOGGER.finer("test1");
        s.solve();
        int valv1 = s.getVar(v1).getVal();
        int valv2 = s.getVar(v2).getVal();
        s.postCut(s.eq(s.getVar(v1), valv1));
        s.postCut(s.eq(s.getVar(v2), valv2));
        Boolean nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    /**
     * one cut that forces each variable to be different from their value in the first solution
     * -> only one other solution (flipping all variables)
     */
    @Test
    public void test2() {
        LOGGER.finer("test1");
        LOGGER.info("test2");
        s.solve();
        int valv1 = s.getVar(v1).getVal();
        int valv2 = s.getVar(v2).getVal();
        s.postCut(s.neq(s.getVar(v1), valv1));
        s.postCut(s.neq(s.getVar(v2), valv2));
        Boolean nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        assertEquals(s.getVar(v1).getVal(), 1 - valv1);
        assertEquals(s.getVar(v2).getVal(), 1 - valv2);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    /**
     * one cut that sets the first variable to its value in the first solution
     * -> only one more solutions afterwards
     */
    @Test
    public void test3() {
        LOGGER.info("test3");
        LOGGER.finer("test3");
        s.solve();
        int valv1 = s.getVar(v1).getVal();
        s.postCut(s.eq(s.getVar(v1), valv1));
        Boolean nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        assertEquals(valv1, s.getVar(v1).getVal());
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    /**
     * one cut that sets the first variable to its value in the first solution
     * -> only one more solutions afterwards
     */
    @Test
    public void test4() {
        LOGGER.info("test4");
        LOGGER.finer("test4");
        s.solve();
        int valv2 = s.getVar(v2).getVal();
        s.postCut(s.eq(s.getVar(v2), valv2));
        Boolean nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        assertEquals(valv2, s.getVar(v2).getVal());
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    @Test
    public void test5() {
        LOGGER.info("test5");
        LOGGER.finer("test5");
        IntegerVariable v3 = makeIntVar("v3", 0, 1);
        m.addVariable(v3);
        s.read(m);
        s.solve();  // first solution 0,0,0
        s.postCut(s.leq(s.plus(s.getVar(v1), s.plus(s.getVar(v2), s.getVar(v3))), 1));
        // now three more solutions
        Boolean nxt;
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    @Test
    public void test6() {
        LOGGER.info("test6");
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        IntegerVariable v3 = makeIntVar("v3", 0, 1);
        m.addVariable(v3);
        s.read(m);
        s.solve();  // first solution 0,0,0
        s.postCut(s.geq(s.plus(s.getVar(v1), s.plus(s.getVar(v2), s.getVar(v3))), 2));
//         now three more solutions
//        while(s.nextSolution()){
//            LOGGER.info(s.pretty());
//        }
        Boolean nxt;
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        assertEquals(nxt, Boolean.FALSE);
    }

    @Test
    public void test7() {
        LOGGER.info("test7");
        LOGGER.finer("test7");
        IntegerVariable v3 = makeIntVar("v3", 0, 1);
        m.addVariable(v3);
        s.read(m);
        s.solve();  // first solution 0,0,0
        s.postCut(s.eq(s.plus(s.getVar(v1), s.plus(s.getVar(v2), s.getVar(v3))), 2));
        // now three more solutions
        Boolean nxt;
        nxt = s.nextSolution();
        LOGGER.info(s.pretty());
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        LOGGER.info(s.pretty());
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        LOGGER.info(s.pretty());
        assertEquals(nxt, Boolean.TRUE);
        nxt = s.nextSolution();
        LOGGER.info(s.pretty());
        assertEquals(nxt, Boolean.FALSE);
    }

    @Test
    public void test8() {
        LOGGER.info("test8");
        LOGGER.finer("test8");
        IntegerVariable v3 = makeIntVar("v3", 0, 1);
        IntegerVariable v4 = makeIntVar("v4", 0, 1);
        IntegerVariable v5 = makeIntVar("v5", 0, 1);
        IntegerVariable v6 = makeIntVar("v6", 0, 1);
        m.addConstraint(geq(sum(new IntegerVariable[]{v1, v2, v3, v4, v5, v6}), 5));
        s.read(m);
        Boolean found;
        found = s.solve();
        LOGGER.info(s.pretty());
        assertEquals(found, Boolean.TRUE);

        s.postCut(s.eq(s.getVar(v1), 1));
        s.postCut(s.leq(s.sum(s.getVar(v1, v2, v3, v4, v5, v6)), 5));
        for (int i = 0; i < 5; i++) {
            found = s.nextSolution();
            LOGGER.info(s.pretty());
            assertEquals(found, Boolean.TRUE);
        }

        found = s.nextSolution();
        LOGGER.info(s.pretty());
        assertEquals(found, Boolean.FALSE);
    }

    @Test
    public void test9() {
        LOGGER.info("test9");
        m.removeVariables(v1, v2);
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);

        m.addVariables(v1, v2);
        s.read(m);

        SConstraint ct = s.neq(s.getVar(v1), s.getVar(v2));
        SConstraint ct2 = s.neq(s.getVar(v1), s.getVar(v2));
        s.postCut(ct);
        s.postCut(ct2);
        s.eraseConstraint(ct);
        s.eraseConstraint(ct2);

        s.solveAll();

        LOGGER.info("Nombre de solutions : " + s.getNbSolutions());
        assertEquals(s.getNbSolutions(),4);
    }

    @Test
    public void test9bis() {
        LOGGER.info("test9b");
        m.removeVariables(v1, v2);
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);
        m.addVariables(v1, v2);
        s.read(m);

        SConstraint ct = s.neq(s.getVar(v1), s.getVar(v2));
        s.postCut(ct);
        s.eraseConstraint(ct);

        s.solveAll();

        LOGGER.info("Nombre de solutions : " + s.getNbSolutions());
        assertEquals(s.getNbSolutions(),4);
    }

    @Test
    public void test10() {
        LOGGER.info("test10");
        m.removeVariables(v1, v2);
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);
        m.addVariables(v1, v2);
        s.read(m);
        int baseworld = s.getEnvironment().getWorldIndex();
        s.worldPush();

        s.solveAll();
        LOGGER.info("Nombre de solutions : " + s.getNbSolutions());
        assertEquals(s.getNbSolutions(),4);
        s.worldPopUntil(baseworld);
        s.solveAll();
        LOGGER.info("Nombre de solutions : " + s.getNbSolutions());
        assertEquals(s.getNbSolutions(),4);
    }


    @Test
    public void testNogood1() {
        LOGGER.info("testNogood110");
        m = new CPModel();
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);
        v3 = makeIntVar("v3", 0, 1);
        IntegerVariable v4 = makeIntVar("v4", 0, 1);
        IntegerVariable v5 = makeIntVar("v5", 0, 1);

        m.addVariables(v1, v2, v3, v4, v5);
        m.addConstraint(eq(sum(new IntegerVariable[]{v1, v2, v3, v4, v5}),3));
        s.read(m);

        s.solve();
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal() + " , " + s.getVar(v4).getVal() + " , " + s.getVar(v5).getVal());

        s.addNogood(new IntDomainVar[]{s.getVar(v1)},new IntDomainVar[]{s.getVar(v3),s.getVar(v4)});

        s.nextSolution();
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v3).getVal() == 0 ||s.getVar(v4).getVal() == 0);
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal() + " , " + s.getVar(v4).getVal() + " , " + s.getVar(v5).getVal());

        s.addNogood(new IntDomainVar[]{s.getVar(v1)},new IntDomainVar[]{s.getVar(v2)});
        s.nextSolution();
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v3).getVal() == 0 ||s.getVar(v4).getVal() == 0);
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v2).getVal() == 0 );
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal() + " , " + s.getVar(v4).getVal() + " , " + s.getVar(v5).getVal());

        s.addNogood(new IntDomainVar[]{s.getVar(v5),s.getVar(v2)},new IntDomainVar[]{});
        s.nextSolution();
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v3).getVal() == 0 ||s.getVar(v4).getVal() == 0);
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v2).getVal() == 0 );
        assertTrue(s.getVar(v5).getVal() == 1 || s.getVar(v2).getVal() == 1 );        
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal() + " , " + s.getVar(v4).getVal() + " , " + s.getVar(v5).getVal());

    }

    @Test
    public void testNogood2() {
        LOGGER.info("testNogood110");
        m = new CPModel();
        s = new CPSolver();
        v1 = makeIntVar("v1", 0, 1);
        v2 = makeIntVar("v2", 0, 1);
        v3 = makeIntVar("v3", 0, 1);

        m.addVariables(v1, v2, v3);
        s.read(m);

        s.solve();
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal());

        s.addNogood(new IntDomainVar[]{s.getVar(v1),s.getVar(v2)},new IntDomainVar[]{});

        s.nextSolution();
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v2).getVal() == 1);
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal());

        s.addNogood(new IntDomainVar[]{},new IntDomainVar[]{s.getVar(v1),s.getVar(v2)});
        s.nextSolution();
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal());
        assertTrue(s.getVar(v1).getVal() == 0 || s.getVar(v2).getVal() == 0);
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v2).getVal() == 1);

        s.addNogood(new IntDomainVar[]{s.getVar(v3)},new IntDomainVar[]{});
        s.nextSolution();
        LOGGER.info(s.getVar(v1).getVal() + " , " + s.getVar(v2).getVal() + " , " + s.getVar(v3).getVal());
        assertTrue(s.getVar(v1).getVal() == 0 || s.getVar(v2).getVal() == 0);
        assertTrue(s.getVar(v1).getVal() == 1 || s.getVar(v2).getVal() == 1);
        assertTrue(s.getVar(v3).getVal() == 1);

    }
}