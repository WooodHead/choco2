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
package choco.model.constraints;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.global.matching.AllDifferent;
import choco.cp.solver.constraints.integer.EqualXYC;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 15 mai 2008
 * Time: 10:13:57
 * To change this template use File | Settings | File Templates.
 */
public class UsersConstraintTest {

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    public static class MyConstraintEqualManager extends IntConstraintManager {
        public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, HashSet<String> options) {
            if(solver instanceof CPSolver){
                IntDomainVar x = solver.getVar((IntegerVariable)variables[0]);
                IntDomainVar y = solver.getVar((IntegerVariable)variables[1]);
                int c = ((IntegerConstantVariable)variables[2]).getValue();
                return new EqualXYC(x, y, c);
            }
            return null;
        }
    }


    @Test
    public void equalXYCTest() {
        for (int i = 2; i < 20; i++) {
            LOGGER.info("i = " + i);
            Model mod1 = new CPModel();
            Model mod2 = new CPModel();
            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();

            //Model declaration
            // Variables
            IntegerVariable v0 = makeIntVar("v0", 0, i);
            IntegerVariable v1 = makeIntVar("v1", 0, i);
            //Add variables to the model
            mod1.addVariables(v0, v1);

            Constraint m1 = new ComponentConstraint(MyConstraintEqualManager.class, null, new IntegerVariable[]{v0, v1, constant(0)});
            //OR (but need a constructor without any parameters
            // MyConstraint m2 = new MyConstraint(new EqualXYC(), v0, v1, 1);

            //Add my constraints to the model
            mod1.addConstraint(m1);

            s1.read(mod1);

            //mod1.removeMyConstraint(m1);

            mod2.addConstraint(eq(v0, v1));

            s2.read(mod2);

            long t1 = System.currentTimeMillis();
            s1.solveAll();
            long t2 = System.currentTimeMillis();
            s2.solveAll();
            long t3 = System.currentTimeMillis();

            LOGGER.info("solver1:" + (t2 - t1));
            LOGGER.info("solver2:" + (t3 - t2));

            assertTrue(s1.getNbSolutions() == s2.getNbSolutions());
        }
    }

    public static class MyConstraintAllDifferentManager extends IntConstraintManager{
        public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, HashSet<String> options) {
            if(solver instanceof CPSolver){
                return new AllDifferent(solver.getVar((IntegerVariable[])variables));
            }
            return null;
        }
    }

    @Test
    public void allDifferentTest() {

        for (int n = 2; n < 8; n++) {
            Model mod1 = new CPModel();
            Model mod2 = new CPModel();
            Solver s1 = new CPSolver();
            Solver s2 = new CPSolver();
            LOGGER.info("n = " + n);
            //Model declaration
            // Variables
            IntegerVariable[] vars = makeIntVarArray("v", n, 0, n);

            // My constraints
            Constraint m1 = new ComponentConstraint(MyConstraintAllDifferentManager.class, null, vars);

            //Add my constraints to the model
            mod1.addConstraint(m1);

            s1.read(mod1);

            //m.removeMyConstraint(m1);

            mod2.addConstraint(allDifferent(vars));

            s2.read(mod2);
            long t1 = System.currentTimeMillis();
            s1.solveAll();
            long t2 = System.currentTimeMillis();
            s2.solveAll();
            long t3 = System.currentTimeMillis();

            LOGGER.info("solver1:" + (t2 - t1));
            LOGGER.info("solver2:" + (t3 - t2));
            
            assertTrue(s1.getNbSolutions() == s2.getNbSolutions());
        }
    }

}
