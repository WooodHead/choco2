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
package choco.kernel.common.logging;



/**
 * This enum defines choco verbosity level.
 * @author Arnaud Malapert</br> 
 * @since 16 avr. 2009 version 2.1.0</br>
 * @version 2.1.0</br>
 */
public enum Verbosity {
	
	/** disable logging*/
	OFF(Integer.MIN_VALUE),
	/** display only severe messages from core loggers and warning messages otherwise*/
	SILENT(0), 
	/** display only severe messages from core loggers and info messages otherwise.*/
	QUIET(100), 
	/** display information about initial and final state of the search */
	DEFAULT(200), 
	/** display search information at regular node intervals */
	VERBOSE(300), 
	/** display all solutions */
	SOLUTION(400), 
	/** display the search tree */
	SEARCH(500) , 
	/** display all logs*/
	FINEST(Integer.MAX_VALUE);
	
	private final int levelValue;
	
	
	private Verbosity(int levelValue) {
		this.levelValue = levelValue;
	}


	public int intValue() {
		return levelValue;
	}
}