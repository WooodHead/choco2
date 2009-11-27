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
package choco.cp.solver.search.real;

import choco.cp.solver.search.real.objective.MaxRealObjManager;
import choco.cp.solver.search.real.objective.MinRealObjManager;
import choco.kernel.solver.search.AbstractOptimize;
import choco.kernel.solver.search.IObjectiveManager;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.real.RealVar;

/**
 * J-CHOCO
 * Copyright (C) F. Laburthe, 1999-2003
 * <p/>
 * An open-source Constraint Programming Kernel
 * for Research and Education
 * <p/>
 * Created by: Guillaume on 20 juil. 2004
 */
public class RealBranchAndBound extends AbstractOptimize {

	public final RealVar objective;

	public RealBranchAndBound(RealVar objective, boolean maximize) {
		super( makeDefaultObjManager(objective, maximize), maximize);
		this.objective = objective;
	}
	
	protected final static IObjectiveManager makeDefaultObjManager(RealVar objective, boolean maximize) {
		return maximize ? new MaxRealObjManager(objective) : new MinRealObjManager(objective);
	}
	
	@Override
	public final Var getObjective() {
		return objective;
	}
	
	public final RealVar getIntObjective() {
		return objective;
	}
		
}