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
package choco.cp.solver.constraints.global.scheduling.trees;

import java.util.List;

import choco.cp.solver.constraints.global.scheduling.trees.status.ThetaOmegaStatus;
import choco.kernel.common.opres.graph.IBinaryNode;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.variables.scheduling.IRTask;
import choco.kernel.solver.variables.scheduling.ITask;

/**
 * @author abadr
 *
 */
public class AltDisjTreeTLTO extends AbstractThetaTree implements IThetaLambdaTree, IThetaOmegaTree{

	private int nbOmegaTasks = 0;
	public AltDisjTreeTLTO(List<? extends ITask> tasks) {
		super(tasks);
	}
	@Override
	public void insert(final ITask task) {
		insertTask(task, new AltDisjStatusTLTO(NodeType.NIL), new AltDisjStatusTLTO(NodeType.INTERNAL));
	}
	/**
	 * Checks whether a regular task is not in Theta tree.
	 * @param rtask
	 * @return true if task is not in Theta, false otherwise.
	 */
	public boolean isTaskRegularAndNotInTheta(IRTask rtask){
		if(rtask.isRegular()){
			//If the task is regular, then check whether it is in Theta or not
			int tType=this.getTaskType(rtask);
			if(tType == 2)
				throw new SolverException("Regular tasks cannot be found in Omega Tree");
			else{
				return (tType != 1);
			}
		}else
			return false;
	}
	
	public int getTaskType(IRTask rtask){
		final IBinaryNode leaf = getLeaf(rtask.getTaskVar());
		final AltDisjStatusTLTO status = (AltDisjStatusTLTO) leaf.getNodeStatus();
		switch(status.getType()){
		case THETA:
			return 1;
		case OMEGA:
			return 2;
		case LAMBDA:
			return 3;
		case NIL:
			return 4;
		default:
			throw new SolverException("Leaf node has an invalid node type");
		}
	}
	public void initializeEdgeFinding(final TreeMode mode, final Iterable<IRTask> rtasks) {
		this.setMode(mode);
		//After the previous step, List of leaf nodes in the tree should be sorted according to there
		//Earliest Starting times
		for (IRTask rtask : rtasks) {
			if(rtask.isRegular()) {
				//Task is Regular, Add to Theta
				final IBinaryNode leaf = getLeaf(rtask.getTaskVar());
				final AltDisjStatusTLTO status =  (AltDisjStatusTLTO) leaf.getNodeStatus();
				if(status.getType() == AbstractVilimTree.NodeType.NIL) {
					status.insertInTheta();
				}else {
					throw new SolverException("can't initialize Theta Node in Alternative Edge Finding TLTO Tree");
				}
			}
			else if(rtask.isOptional()){
				//Task is optional,Add to Omega.
				final IBinaryNode leaf=getLeaf(rtask.getTaskVar());
				final AltDisjStatusTLTO status = (AltDisjStatusTLTO) leaf.getNodeStatus();
				if(status.getType() == AbstractVilimTree.NodeType.NIL){
					status.insertInOmega(rtask);
					nbOmegaTasks++;
				}else{
					throw new SolverException("can't initialize Omega Node in Alternative Edge Finding TLTO Tree");
				}
			}
		}
		fireTreeChanged();
	}
	protected ThetaOmegaStatus getRootStatus(){
		return getNodeStatus(getRoot()).getStatus();
	}
	protected final AltDisjStatusTLTO getNodeStatus(IBinaryNode node) {
		return (AltDisjStatusTLTO) node.getNodeStatus();
	}
	@Override
	public int getTime() {
		return getRootStatus().getTime();
	}
	@Override
	public int getGrayTime() {
		return getRootStatus().getGrayTime();
	}
	@Override
	public Object getResponsibleTask() {
		return getRootStatus().getRespGrayTime();
	}
	@Override
	public int getTOTime() {
		return getRootStatus().getTOTime();
	} 
	
	@Override
	public Object getResponsibleTOTask() {
		return getRootStatus().getRespTOTime();
	}
	
	@Override
	public boolean insertInLambda(IRTask rtask) {
		final IBinaryNode leaf = getLeaf(rtask.getTaskVar());
		final AltDisjStatusTLTO status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.NIL) {
			status.insertInLambda(rtask);
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	@Override
	public boolean removeFromLambda(ITask task) {
		final IBinaryNode leaf = getLeaf(task);
		final AltDisjStatusTLTO status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.LAMBDA) {
			status.removeFromLambda();
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	private boolean removeFromThetaAndInsertInLambda(ITask task, Object resp) {
		final IBinaryNode leaf = getLeaf(task);
		final AltDisjStatusTLTO status =  getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.THETA) {
			status.insertInLambda(resp);
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	private boolean removeFromOmegaAndInsertInLambda(IRTask task,Object resp){
			
			final IBinaryNode leaf = getLeaf(task.getTaskVar());
			final AltDisjStatusTLTO status =  getNodeStatus(leaf);
			if(status.getType() == AbstractVilimTree.NodeType.OMEGA) {
				//Remove task first from Omega
				status.removeFromOmega();
				//leaf.fireStatusChanged();
				nbOmegaTasks --;
				//Then add the Task to Lambda 
				status.insertInLambda(resp);
				leaf.fireStatusChanged();
				return true;
			}
			return false;
	}
	@Override
	public boolean removeFromThetaAndInsertInLambda(ITask task) {
		return this.removeFromThetaAndInsertInLambda(task, task);
	}
	@Override
	public boolean removeFromThetaAndInsertInLambda(IRTask task) {
		return this.removeFromThetaAndInsertInLambda(task.getTaskVar(), task);
	}
	@Override
	public boolean insertInOmega(IRTask rtask) {
		final IBinaryNode leaf = getLeaf(rtask.getTaskVar());
		final AltDisjStatusTLTO status = getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.NIL){
			//Task is not assigned to any tree.
			//Add task to OMEGA
			status.insertInOmega(rtask);
			nbOmegaTasks++;
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	@Override
	public boolean removeFromOmega(IRTask task) {
		final IBinaryNode leaf = getLeaf(task.getTaskVar());
		final AltDisjStatusTLTO status = getNodeStatus(leaf);
		if(status.getType() == AbstractVilimTree.NodeType.OMEGA){
			//Task already exists in OMEGA
			status.removeFromOmega();
			nbOmegaTasks--;
			leaf.fireStatusChanged();
			return true;
		}
		return false;
	}
	@Override
	public boolean removeFromOmegaAndInsertInLambda(IRTask task) {
		return this.removeFromOmegaAndInsertInLambda(task, task);
	}
	
	
	public int getNbOmegaTasks() {
		return nbOmegaTasks;
	}


	protected class AltDisjStatusTLTO extends AbstractVilimStatus<ThetaOmegaStatus> implements ThetaTreeLeaf  {

		public AltDisjStatusTLTO(NodeType type) {
			super(type, new ThetaOmegaStatus());
		}

		protected final int getLeafTime() {
			return getMode().value() ? task.getEST()+task.getMinDuration()  : task.getLCT()-task.getMinDuration();
		}
		@Override
		public void reset() {
			removeFromTheta();
			status.setRespGrayTime(null);
			status.setRespGrayDuration(null);
			status.setRespTOTime(null);
			status.setRespTODuration(null);
		}

		@Override
		protected void writeDotStatus(StringBuilder buffer) {
			writeRow(buffer, getMode().label(), format(status.getTime()),"P", String.valueOf(status.getDuration()));
			buffer.append('|');
			writeRow(buffer,"Gr" + getMode().label(), format(status.getGrayTime()),"GrP", String.valueOf(status.getGrayDuration()));
			//Omega part
			buffer.append('|');
			writeRow(buffer,"Om" + getMode().label(), format(status.getTOTime()), "OmP", String.valueOf(status.getTODuration()));
			if(getType() == NodeType.INTERNAL) {
				buffer.append('|');
				final String t1 =  status.getRespGrayTime() == null ? "?": status.getRespGrayTime().toString();
				final String t2 =  status.getRespGrayDuration() == null ? "?": status.getRespGrayDuration().toString();
				writeRow(buffer,"Gr" + getMode().label(), t1,"GrP", t2);
				buffer.append('|');
				final String o1 =  status.getRespTOTime() == null ? "?": status.getRespTOTime().toString();
				final String o2 =  status.getRespTODuration() == null ? "?": status.getRespTODuration().toString();
				writeRow(buffer,"Om" + getMode().label(), o1,"OmP", o2);
			}
		}

		@Override
		public void insertInTheta() {
			setType(NodeType.THETA);
			//Theta
			status.setTime( getLeafTime());
			status.setDuration(task.getMinDuration());
			//Lambda
			status.setGrayTime( getStatus().getTime());
			status.setGrayDuration( getStatus().getDuration());
			//Omega
			status.setTOTime(getStatus().getTime());
			status.setTODuration(getStatus().getDuration());
		}

		@Override
		public void insertInTheta(IRTask rtask) {
			this.insertInTheta();	
		}

		public void insertInOmega(Object resp){
			setType(NodeType.OMEGA);
			//Theta
			status.setTime( getResetIntValue(getMode()));
			status.setDuration(0);
			//Omega
			status.setTOTime( getLeafTime());
			status.setTODuration( task.getMinDuration());
			status.setRespTOTime(resp);
			status.setRespTODuration(resp);
			//Lambda
			status.setGrayTime( getResetIntValue(getMode()));
			status.setDuration(0);
		}
		
		public void removeFromOmega(){
			setType(NodeType.NIL);
			status.setTOTime( getResetIntValue(getMode()));
			status.setTODuration(0);
			status.setRespTOTime(null);
			status.setRespTODuration(null);
		}
		
		public void insertInLambda(Object resp){
			setType(NodeType.LAMBDA);
			//Theta
			status.setTime( getResetIntValue(getMode()));
			status.setDuration(0);
			//Lambda
			status.setGrayTime( getLeafTime());
			status.setGrayDuration( task.getMinDuration());
			status.setRespGrayTime(resp);
			status.setRespGrayDuration(resp);
			//Omega
			status.setTOTime( getResetIntValue(getMode()));
			status.setTODuration(0);
		}
		
		public void removeFromLambda(){
			setType(NodeType.NIL);
			status.setGrayTime( getResetIntValue(getMode()));
			status.setGrayDuration(0);
			status.setRespGrayTime(null);
			status.setRespGrayDuration(null);
		}
		
		@Override
		public void removeFromTheta() {
			setType(NodeType.NIL);
			//Theta
			status.setTime( getResetIntValue(getMode()));
			status.setDuration(0);
			//Lambda
			status.setGrayTime( getResetIntValue(getMode()));
			status.setGrayDuration(0);
			//Omega
			status.setTOTime( getResetIntValue(getMode()));
			status.setTODuration(0);
		}

		@Override
		public void updateInternalNode(IBinaryNode node) {
			if(node.getLeftChild().getNodeStatus() instanceof AltDisjStatusTLTO) {
				final ThetaOmegaStatus left = ( (AltDisjStatusTLTO) node.getLeftChild().getNodeStatus()).getStatus();
				if(node.getRightChild().getNodeStatus() instanceof AltDisjStatusTLTO) {
					final ThetaOmegaStatus right = ( (AltDisjStatusTLTO) node.getRightChild().getNodeStatus()).getStatus();
					this.status.update(getMode(), left, right);
					return;
				}
			}
			throw new SolverException("cant update node");
			
		}
	}
}