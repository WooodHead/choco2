/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |  �(..)  |                           *
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
 *                  N. Jussien    1999-2010      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.cp.model.managers.constraints.global;

import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.constraints.global.IncreasingNValue;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.HashSet;

/**
 * User : xlorca
 * Mail : xlorca(a)emn.fr
 * Date : 29 janv. 2010
 * Since : Choco 2.1.1
 */
public class IncreasingNValueManager extends IntConstraintManager {

    public SConstraint makeConstraint(Solver solver, IntegerVariable[] integerVariables, Object parameters, HashSet<String> options) {
        IntDomainVar[] vars = new IntDomainVar[integerVariables.length-1];
        System.arraycopy(solver.getVar(integerVariables),1,vars,0,integerVariables.length-1);
        if(options.contains("cp:atleast")){
            return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.ATLEAST);
        }
        if(options.contains("cp:atmost")){
            return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.ATMOST);
        }   
        return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.BOTH);
    }


}
