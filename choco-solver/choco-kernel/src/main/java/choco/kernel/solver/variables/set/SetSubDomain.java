/* ************************************************
 *           _       _                            *
 *          |  °(..)  |                           *
 *          |_  J||L _|        CHOCO solver       *
 *                                                *
 *     Choco is a java library for constraint     *
 *     satisfaction problems (CSP), constraint    *
 *     programming (CP) and explanation-based     *
 *     constraint solving (e-CP). It is built     *
 *     on a event-based propagation mechanism     *
 *     with backtrackable structures.             *
 *                                                *
 *     Choco is an open-source software,          *
 *     distributed under a BSD licence            *
 *     and hosted by sourceforge.net              *
 *                                                *
 *     + website : http://choco.emn.fr            *
 *     + support : choco@emn.fr                   *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                   N. Jussien    1999-2008      *
 **************************************************/
package choco.kernel.solver.variables.set;

import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.variables.delta.IDeltaDomain;

import java.util.logging.Logger;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 20 janv. 2009
* Since : Choco 2.0.1
* Update : Choco 2.0.1
*/
public interface SetSubDomain {

	public static final Logger LOGGER = ChocoLogging.getEngineLogger();
	
    public boolean contains(int val);

    public boolean remove(int val);

    public int getSize();

    public int getFirstVal();

    public int getLastVal();

    public void clearDeltaDomain();

    public void freezeDeltaDomain();

    public boolean releaseDeltaDomain();

    public boolean getReleasedDeltaDomain();

    public DisposableIntIterator getDeltaIterator();

    public IDeltaDomain copyDelta();
}
