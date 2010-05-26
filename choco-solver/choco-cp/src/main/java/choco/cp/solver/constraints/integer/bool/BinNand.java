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
package choco.cp.solver.constraints.integer.bool;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * maintain v1 NAND v2 where v1 and v2 are boolean variables
 * i.e variables of domain {0,1}
 */
public final class BinNand extends AbstractBinIntSConstraint {

	BinNand(IntDomainVar v1, IntDomainVar v2) {
		super(v1, v2);
	}


    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINT_MASK;
    }

    public void propagate() throws ContradictionException {
		if (v0.isInstantiatedTo(1)) v1.instantiate(0, this, false);
		if (v1.isInstantiatedTo(1)) v0.instantiate(0, this, false);
	}

	public void awakeOnInst(int idx) throws ContradictionException {
		int val;
		if (idx == 0) {
			val = v0.getVal();
			if (val == 1) v1.instantiate(0, this, false);
		} else {
			val = v1.getVal();
			if (val == 1) v0.instantiate(0, this, false);
		}
	}

  public void awakeOnInf(int varIdx) throws ContradictionException {
  }

  public void awakeOnSup(int varIdx) throws ContradictionException {
  }

  public void awakeOnRem(int varIdx, int val) throws ContradictionException {
  }

  public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 0 || tuple[1] == 0;
	}

	public Boolean isEntailed() {
		if (v0.isInstantiatedTo(0) ||
				v1.isInstantiatedTo(0))
			return Boolean.TRUE;
		else if (v0.isInstantiatedTo(1) &&
				v1.isInstantiatedTo(1))
			return Boolean.FALSE;
		else return null;
	}

}