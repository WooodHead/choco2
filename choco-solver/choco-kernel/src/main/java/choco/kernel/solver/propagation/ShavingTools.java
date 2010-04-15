package choco.kernel.solver.propagation;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.tools.VariableUtils;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.search.AbstractGlobalSearchStrategy;
import choco.kernel.solver.search.IObjectiveManager;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.integer.IntVar;


public class ShavingTools {

	public final Solver solver;

	public final IntDomainVar[] vars;

	public boolean backwardPropagation = true;

	public boolean shaveLowerBound= false;

	public boolean shaveFinalLowerBound= true;

	private int nbRemovals;

	/**
	 * 
	 * @param solver
	 * @param vars The scope of shaving algorithm must not contain the objective.
	 */
	public ShavingTools(Solver solver, IntDomainVar[] vars) {
		super();
		this.solver = solver;
		this.vars = vars;
	}

	public ShavingTools(Solver solver) {
		this(solver, buildVars(solver));
	}	
	private static IntDomainVar[] buildVars(Solver solver) {
		if(solver.getObjective() != null && solver.getObjective() instanceof IntDomainVar ) {
			final IntVar obj = (IntVar) solver.getObjective();
			final int n = solver.getNbIntVars();
			final IntDomainVar[] vars = new IntDomainVar[n - 1];
			int idx = 0;
			for (int i = 0; i < n; i++) {
				final IntDomainVar v = (IntDomainVar) solver.getIntVarQuick(i);
				if(v != obj) vars[idx++] = v;
			}
			return vars;
		}else return VariableUtils.getIntVars(solver);
	}


	public final boolean isBackwardPropagation() {
		return backwardPropagation;
	}



	public final void setBackwardPropagation(boolean backwardPropagation) {
		this.backwardPropagation = backwardPropagation;
	}

	public final Solver getSolver() {
		return solver;
	}

	public final IntDomainVar[] getVars() {
		return vars;
	}


	public final int getNbRemovals() {
		return nbRemovals;
	}


	protected final void shaveVars() throws ContradictionException, LuckySolutionException {
		nbRemovals = 0;
		for (IntDomainVar var : vars) {
			if( ! var.isInstantiated() ) {
				if(var.hasEnumeratedDomain()) shaveEnumVar(var);
				else shaveBoundVar(var);			
			}
		}	
		if(nbRemovals > 0 && ! backwardPropagation) {
			solver.propagate();
		}
	}

	public final void shaving() throws ContradictionException {
		try {
			shaveVars();
		} catch (LuckySolutionException e) {}
	}

	protected void shaveEnumVar(IntDomainVar var) throws LuckySolutionException, ContradictionException {
		final DisposableIntIterator iter = var.getDomain().getIterator();
		try{
			while (iter.hasNext()) {
				shaving(var, iter.next());
			} 
		}finally {iter.dispose();}
	}

	protected void shaveBoundVar(IntDomainVar var) throws LuckySolutionException, ContradictionException {
		int oldNbR;
		do {
			oldNbR = nbRemovals;
			shaving(var, var.getInf());
		}while(nbRemovals > oldNbR);
		do {
			oldNbR = nbRemovals;
			shaving(var, var.getSup());
		}while(nbRemovals > oldNbR);
	}

	protected void shaving(IntDomainVar var, int val) throws LuckySolutionException, ContradictionException  {
		solver.worldPush();
		try {
			var.instantiate(val, null, true);
			solver.propagate();
			detectLuckySolution();	
			solver.worldPop();
		} catch (ContradictionException e) {
			solver.worldPop();
			nbRemovals++;
			var.removeVal(val, null, true);
			if(backwardPropagation) solver.propagate();
		}
	}


	public final void destructiveLowerBound(final IObjectiveManager objM) throws ContradictionException {
		objM.initBounds();
		try {
			while(shaveObjective(objM)) {
				objM.incrementFloorBound();
			}
			assert ! objM.isTargetInfeasible();
			if( ! objM.getObjectiveValue().equals(objM.getObjectiveFloor())) {
				try {
					objM.postFloorBound();
				} catch (ContradictionException e) {
					throw new SolverException("Destructive Lower Bound: Invalid bounds");
				}
				solver.propagate();
				if(shaveFinalLowerBound) shaving();
			}
		} catch (LuckySolutionException e) {}
	}


	protected final boolean shaveObjective(final IObjectiveManager objM) throws ContradictionException, LuckySolutionException {
		boolean shave = false;
		solver.worldPush();
		objM.postIncFloorBound();
		try {
			solver.propagate();
			detectLuckySolution();
			if(shaveLowerBound) shaveVars();
		} catch (ContradictionException e) {
			shave = true;
		} 
		solver.worldPop();
		return shave;
	}

	public Boolean nextBottomUp(IObjectiveManager objM) {
		try {
			objM.postIncFloorBound();
		} catch (ContradictionException e) {
			throw new SolverException("Destructive Lower Bound: Invalid bounds");
		}
		try {
			solver.propagate();
			if(shaveLowerBound) shaving();
		} catch (ContradictionException e) {
			return Boolean.FALSE;
		}
		final AbstractGlobalSearchStrategy strategy = solver.getSearchStrategy();
		final int oldBaseWorld = strategy.baseWorld;
		strategy.baseWorld = solver.getWorldIndex();
		solver.worldPush();
		Boolean b = strategy.nextSolution();
		strategy.baseWorld = oldBaseWorld;
		return b;
	}


	//TODO optimize by keeping the last not instantiated variables
	protected void detectLuckySolution() throws LuckySolutionException {
		int n = solver.getNbIntVars();
		for (int i = 0; i < n; i++) {
			if( ! solver.getIntVarQuick(i).isInstantiated()) return;
		}
		n = solver.getNbSetVars();
		for (int i = 0; i < n; i++) {
			if( ! solver.getSetVarQuick(i).isInstantiated()) return;
		}
		if(solver.getNbRealVars() > 0) return; //FIXME what about real
		throw LuckySolutionException.SINGLOTON;
	}

	final static class LuckySolutionException extends Exception {

		private static final long serialVersionUID = -1476316199858738423L;

		public final static LuckySolutionException SINGLOTON = new LuckySolutionException();

		private LuckySolutionException() {
			super("Shaving lead to a solution");
		}


	}

}