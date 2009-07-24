package choco.cp.solver.search.objective;

import choco.kernel.solver.variables.real.RealIntervalConstant;
import choco.kernel.solver.variables.real.RealMath;
import choco.kernel.solver.variables.real.RealVar;

public final class MaxRealObjManager extends RealObjectiveManager {

	
	public MaxRealObjManager(RealVar objective) {
		super(objective);
	}

	@Override
	public double getObjectiveRealValue() {
		return objective.getSup();
	}

	private final void setBoundInterval() {
		boundInterval = new RealIntervalConstant(targetBound, Double.POSITIVE_INFINITY);
	}

	@Override
	public void initBounds() {
		bound = Double.NEGATIVE_INFINITY;
		oppositeBound = objective.getSup();
		targetBound = objective.getInf();
		setBoundInterval();
	}

	@Override
	public void setBound() {
		final double v = objective.getSup();
		if( v > bound) { bound = v;}
	}
	

	@Override
	public void setTargetBound() {
		targetBound = RealMath.nextFloat(objective.getInf());
		setBoundInterval();
		
	}
	
	@Override
	public boolean isTargetInfeasible() {
		return targetBound > oppositeBound;
	}
}