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
package choco.cp.solver.constraints.global.scheduling;

import gnu.trove.TIntArrayList;

import java.util.BitSet;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.trailing.StoredBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntDomainVar;

public class IncrementalPertSConstraint extends PertSConstraint {

	protected final StoredBitSet tasksToPropagateOnInf;

	protected final StoredBitSet tasksToPropagateOnSup;

	public IncrementalPertSConstraint(Solver solver, IntDomainVar uppBound) {
		super(solver, uppBound);
		final IEnvironment env= solver.getEnvironment();
		tasksToPropagateOnInf =  (StoredBitSet) env.makeBitSet(getNbTasks());
		tasksToPropagateOnSup = (StoredBitSet) env.makeBitSet(getNbTasks());
		propagationControlInf.set(false);
		propagationControlSup.set(false);
	}



	@Override
	public void awake() throws ContradictionException {
		super.awake();
		//initial propagation
		propagateLowerBounds();
		propagateUpperBounds();
	}



	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		if(varIdx < taskIntVarOffset) {
			final int tidx = varIdx % getNbTasks();
			updateCompulsoryPart( tidx);
			tasksToPropagateOnInf.set(tidx);
			propagationControlInf.set(true);
			this.constAwake(false);
		}

	}


	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if(idx < taskIntVarOffset) {
			final int tidx = idx % getNbTasks();
			updateCompulsoryPart(tidx);
			propagationControlInf.set(true);
			tasksToPropagateOnInf.set(tidx);
			propagationControlSup.set(true);
			tasksToPropagateOnSup.set(tidx);
		} else {propagationControlMakespan.set(true);}
		this.constAwake(false);
	}


	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		if(varIdx < taskIntVarOffset) {
			final int tidx = varIdx % getNbTasks();
			updateCompulsoryPart(tidx);
			propagationControlSup.set(true);
			tasksToPropagateOnSup.set(tidx);
		} else {propagationControlMakespan.set(true);}
		this.constAwake(false);
	}



	public final void incrPropagateLowerBounds() throws ContradictionException {
		final int[] order=network.getTopologicalOrder();
		final int[] index=network.getTopologicalOrderIndex();
		//read touched subgraph
		final BitSet subgraph=new BitSet(index.length);
		for (int i = tasksToPropagateOnInf.nextSetBit(0); i >= 0; i = tasksToPropagateOnInf.nextSetBit(i + 1)) {
			subgraph.set(index[i]);
		}
		for (int i = subgraph.nextSetBit(0); i >= 0; i = subgraph.nextSetBit(i + 1)) {
			final int task=order[i];
			//propagate on successors
			TIntArrayList succ = network.getSuccessors(task);
			for (int j = 0; j < succ.size(); j++) {
				final int dest = succ.get(j);
				final boolean modified= taskvars[dest].start().updateInf( taskvars[task].getECT(), getCIndiceStart(dest));
				updateDuration(task, dest);
				if(modified) {
					updateCompulsoryPart(dest);
					subgraph.set(index[dest]);
				}
			}
		}
		tasksToPropagateOnInf.clear();
		propagationControlInf.set(false);
	}

	public final void incrPropagateUpperBounds() throws ContradictionException {
		//CPSolver.flushLogs();
		final int[] order=network.getTopologicalOrder();
		final int[] index=network.getTopologicalOrderIndex();
		final BitSet subgraph=new BitSet(index.length);
		//invert index order to have a decreasing index loop (all successor are checked).
		final int n= getNbTasks();
		for (int i = tasksToPropagateOnSup.nextSetBit(0); i >= 0; i = tasksToPropagateOnSup.nextSetBit(i + 1)) {
			subgraph.set(n -1 - index[i]);
		}
		for (int i = subgraph.nextSetBit(0); i >= 0; i = subgraph.nextSetBit(i + 1)) {
			final int task=order[n - 1 - i];
			//propagate on predecessors
			TIntArrayList pred = network.getPredecessors(task);
			for (int j = 0; j < pred.size(); j++) {
				final int orig = pred.get(j);
				final boolean modified= taskvars[orig].start().updateSup( taskvars[task].getLST() - taskvars[orig].getMinDuration(), getCIndiceStart(orig));
				updateDuration(task, orig);
				if(modified) {
					updateCompulsoryPart(orig);
					subgraph.set(n -1 - index[orig]);
				}
			}
		}
		tasksToPropagateOnSup.clear();
		propagationControlSup.set(false);		
	}

	@Override
	public void propagate() throws ContradictionException {
		if (propagationControlInf.get()) {
			incrPropagateLowerBounds();
			//propagateLowerBounds();
		}

		if(propagationControlMakespan.get()) {
			propagateUpperBounds();
			tasksToPropagateOnSup.clear();
		}

		if(propagationControlSup.get()) {
			incrPropagateUpperBounds();
			//propagateUpperBounds();
		}
	}



}