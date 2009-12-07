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
package choco.cp.model.managers.operators;

import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.reified.leaves.arithm.ScalarNode;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ExpressionManager;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.reified.INode;

/* User:    charles
 * Date:    20 août 2008
 */
public class SumManager implements ExpressionManager {

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param vars
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, IntegerExpressionVariable[] vars) {
        if(solver instanceof CPSolver){
            CPSolver s = (CPSolver)solver;
            INode[] nodes = new INode[vars.length];
            for (int i = 0; i < nodes.length; i++) {
                nodes[i] = vars[i].getEm().makeNode(s, vars[i].getConstraints(), vars[i].getVariables());
            }
            return new ScalarNode(nodes);
        }
        throw new ModelException("Could not found a node manager in " + this.getClass() + " !");
    }
}
