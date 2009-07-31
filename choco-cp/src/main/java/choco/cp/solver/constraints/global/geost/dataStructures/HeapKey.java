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
package choco.cp.solver.constraints.global.geost.dataStructures;

import choco.cp.solver.constraints.global.geost.geometricPrim.Point;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;


public class HeapKey implements Heapable {

	private Point p;
	private int d; //the internal dimension
	private InternalConstraint ictr;
	private int k;//the total dimension of the problem

	public HeapKey(Point p, int d, int dim, InternalConstraint ictr)
	{
		this.p = p;
		this.d = d;
		this.ictr= ictr;
		this.k = dim;
	}


	public int getD() {
		return d;
	}


	public InternalConstraint getIctr() {
		return ictr;
	}


	public Point getP() {
		return p;
	}


	public void setD(int d) {
		this.d = d;
	}


	public void setIctr(InternalConstraint ictr) {
		this.ictr = ictr;
	}


	public void setP(Point p) {
		this.p = p;
	}


	public boolean equalTo(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + this.d) % k;
			if(p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime)) {
				return false;
			}
		}
		return true;
	}

	public boolean greaterThan(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + d) % k;
			if(this.p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime))
			{
                return this.p.getCoord(jPrime) >= (((HeapKey) other).getP()).getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}


	public boolean lessThan(Object other) {
		int jPrime = 0;

		for(int j = 0; j < k; j++)
		{
			jPrime = (j + d) % k;
			if(p.getCoord(jPrime) != (((HeapKey) other).getP()).getCoord(jPrime))
			{
                return p.getCoord(jPrime) <= (((HeapKey) other).getP()).getCoord(jPrime);
			}
		}
		return false; //since they are equal
	}

}