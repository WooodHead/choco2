package choco.cp.solver.search.task;

import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author Arnaud Malapert</br> 
 * @since 18 juin 2009 version 2.1.0</br>
 * @version 2.1.0</br>
 */
public final class StoredPrecedence {

	public final ITask t1;

	public final ITask t2;

	public final IntDomainVar direction;

	public StoredPrecedence(ITask t1, ITask t2, IntDomainVar direction) {
		super();
		this.t1 = t1;
		this.t2 = t2;
		this.direction = direction;
	}

	public final ITask getLeftTask() {
		return t1;
	}

	public final ITask getRightTask() {
		return t2;
	}

	public final IntDomainVar getDirection() {
		return direction;
	}
	
	public final int getDomMesure() {
		return t1.getSlack() + t2.getSlack() + 2;
	}
	
	@Override
	public String toString() {
		return "("+t1.getName()+","+t2.getName()+")";
	}
	
}