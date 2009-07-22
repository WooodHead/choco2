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
package choco.cp.solver.search.restart;

import choco.kernel.solver.search.limit.Limit;



/**
 * @author Arnaud Malapert
 *
 */
public final class GeometricalRestart extends AbstractParametrizedRestartStrategy {

	

	public GeometricalRestart(Limit type, int scaleFactor,
			double geometricalFactor) {
		super(type, scaleFactor, geometricalFactor);
	}
	
	
	
	@Override
	public final String getName() {
		return "GEOM";
	}


	@Override
	protected int getNextLimit() {
		return (int) Math.ceil( Math.pow(geometricalFactor,nbRestarts) * scaleFactor );
	}
}