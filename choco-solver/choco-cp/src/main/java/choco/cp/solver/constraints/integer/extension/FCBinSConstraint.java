package choco.cp.solver.constraints.integer.extension;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.AbstractSConstraint;
import choco.kernel.solver.constraints.integer.extension.BinRelation;
import choco.kernel.solver.constraints.integer.extension.ConsistencyRelation;
import choco.kernel.solver.constraints.integer.extension.CspBinSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 *
 * A binary constraint for simple forward checking
 */
public final class FCBinSConstraint extends CspBinSConstraint {

    public FCBinSConstraint(IntDomainVar x0, IntDomainVar x1, BinRelation rela) {//int[][] consistencyMatrice) {
		super(x0, x1, rela);
	}

    public int getFilteredEventMask(int idx) {
        return IntVarEvent.INSTINTbitvector;
    }


    public Object clone() {
		return new FCBinSConstraint(this.v0, this.v1, this.relation);
	}

	// standard filtering algorithm initializing all support counts
	public void propagate() throws ContradictionException {
        if (v0.isInstantiated())
            awakeOnInst(0);
        if (v1.isInstantiated())
            awakeOnInst(1);
    }

	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) {
			int value = v0.getVal();
			DisposableIntIterator itv1 = v1.getDomain().getIterator();
			try {
				while (itv1.hasNext()) {
					int val = itv1.next();
					if (!relation.isConsistent(value, val)) {
						v1.removeVal(val, this, false);
					}
				}
			} finally {
				itv1.dispose();
			}
		} else {
			int value = v1.getVal();
			DisposableIntIterator itv0 = v0.getDomain().getIterator();
			try {
				while (itv0.hasNext()) {
					int val = itv0.next();
					if (!relation.isConsistent(val, value)) {
						v0.removeVal(val, this, false);
					}
				}
			} finally {
				itv0.dispose();
			}
		}	}


	public AbstractSConstraint opposite(Solver solver) {
		BinRelation rela2 = (BinRelation) ((ConsistencyRelation) relation).getOpposite();
		AbstractSConstraint ct = new FCBinSConstraint(v0, v1, rela2);
		return ct;
	}

	public String pretty() {
		StringBuilder sb = new StringBuilder();
		sb.append("FC(").append(v0.pretty()).append(", ").append(v1.pretty()).append(", ").
				append(this.relation.getClass().getSimpleName()).append(")");
		return sb.toString();
	}
}
