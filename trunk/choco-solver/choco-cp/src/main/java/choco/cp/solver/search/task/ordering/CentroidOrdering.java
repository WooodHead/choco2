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

package choco.cp.solver.search.task.ordering;

import static choco.kernel.common.util.tools.TaskUtils.getCentroidMultByTwo;
import choco.kernel.solver.variables.scheduling.TaskVar;
public class CentroidOrdering extends RandomOrdering {


	public CentroidOrdering(long seed) {
		super(seed);
	}

	@Override
	public int getBestVal(TaskVar t1, TaskVar t2) {
		return getMaxVal(getCentroidMultByTwo(t1), getCentroidMultByTwo(t2));
	}

}