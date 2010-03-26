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
package choco.cp.solver.search.integer.varselector;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.propagation.listener.PropagationEngineListener;
import choco.kernel.solver.search.integer.DoubleHeuristicIntVarSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 */
public class DomOverWDeg extends DoubleHeuristicIntVarSelector implements PropagationEngineListener {

    private AbstractSConstraint reuseCstr;

	private static final int ABSTRACTCONTRAINT_EXTENSION =
			AbstractSConstraint.getAbstractSConstraintExtensionNumber("choco.cp.cpsolver.search.integer.varselector.DomOverWDeg");

	public DomOverWDeg(Solver solver) {
		super(solver);
        DisposableIterator<SConstraint> iter = solver.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
		}
        iter.dispose();
		solver.getPropagationEngine().addPropagationEngineListener(this);
	}

	public DomOverWDeg(Solver solver, IntDomainVar[] vs) {
		super(solver, vs);
        DisposableIterator<SConstraint> iter = solver.getConstraintIterator();
		for (; iter.hasNext();) {
			AbstractSConstraint c = (AbstractSConstraint) iter.next();
			c.addExtension(ABSTRACTCONTRAINT_EXTENSION);
		}
        iter.dispose();
		solver.getPropagationEngine().addPropagationEngineListener(this);
	}

    /**
     * Define action to do just before a deletion.
     */
    @Override
    public void safeDelete() {
        solver.getPropagationEngine().removePropagationEngineListener(this);
    }

    public void initConstraintForBranching(SConstraint c) {
        ((AbstractSConstraint) c).addExtension(ABSTRACTCONTRAINT_EXTENSION);
    }

    public double getHeuristic(IntDomainVar v) {
		int dsize = v.getDomainSize();
		int weight = 0;
		// Calcul du poids:
		DisposableIntIterator c = v.getIndexVector().getIndexIterator();
		int idx = 0;
        while (c.hasNext()) {
            idx = c.next();
            reuseCstr = (AbstractSConstraint) v.getConstraint(idx);
			if (SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())
                && reuseCstr.getNbVarNotInst() > 1) {
				weight += reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).get() + reuseCstr.getFineDegree(v.getVarIndex(idx));
			}
        }
        c.dispose();
		if (weight == 0)
			return Double.MAX_VALUE;
		else
			return (double) dsize / ((double) weight);
	}

	public void contradictionOccured(ContradictionException e) {
		Object cause = e.getDomOverDegContradictionCause();
		if (cause != null) {
			reuseCstr = (AbstractSConstraint) cause;
            if(SConstraintType.INTEGER.equals(reuseCstr.getConstraintType())){
			    reuseCstr.getExtension(ABSTRACTCONTRAINT_EXTENSION).add(1);
            }
		}
	}
}
