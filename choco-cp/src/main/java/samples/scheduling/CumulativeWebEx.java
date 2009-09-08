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
package samples.scheduling;

import static choco.Choco.boolChanneling;
import static choco.Choco.constant;
import static choco.Choco.constantArray;
import static choco.Choco.cumulativeMax;
import static choco.Choco.eq;
import static choco.Choco.makeBooleanVarArray;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeTaskVarArray;
import static choco.Choco.minus;
import static choco.Choco.sum;

import java.util.logging.Level;

import samples.Examples.PatternExample;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.scheduling.TaskVariable;
import edu.emory.mathcs.backport.java.util.Arrays;

public class CumulativeWebEx extends PatternExample {

	protected final static int NT = 11, NF = 3, N =  NT + NF;

	protected final static int[] HEIGHTS_DATA =  new int[]{2, 1, 4, 2, 3, 1, 5, 6, 2, 1, 3, 1, 1, 2};
	                        
	protected final static int[] DURATIONS_DATA =  new int[]{1, 1, 1, 2, 1, 3, 1, 1, 3, 4, 2, 3, 1, 1};
	                        

	protected final static IntegerVariable CAPACITY = constant(7);
	
	protected IntegerVariable[] usages, heights;
	
	//the fake tasks to establish the profile capacity of the ressource are the NF firsts.
	protected final static TaskVariable[] TASKS = makeTaskVarArray("t", 0, 6, DURATIONS_DATA, "cp:bound");

	protected final static IntegerVariable OBJ = makeIntVar("obj", 0, NT, "cp:bound", "cp:objective");

	protected boolean useAlternativeResource;
	
	
	@Override
	public void setUp(Object parameters) {
		if (parameters instanceof Boolean) {
			useAlternativeResource = ((Boolean) parameters).booleanValue();
		}else {
			useAlternativeResource = true;
		}
		super.setUp(parameters);
	}

	@Override
	public void buildModel() {
		_m = new CPModel();
		if(useAlternativeResource) {
			usages = makeBooleanVarArray("U", NT);
			heights = constantArray(HEIGHTS_DATA);
			//post the cumulative
			_m.addConstraint(cumulativeMax("unique renewable resource", TASKS, heights, usages, CAPACITY, ""));
			//set fake tasks to establish the profile capacit
			_m.addConstraints(
					eq(TASKS[0].start(), 1),
					eq(TASKS[1].start(), 2),
					eq(TASKS[2].start(), 3)
			);
			//state the objective function
			_m.addConstraint(eq( sum(usages), OBJ));
		}else {
			usages = makeBooleanVarArray("U", N);
			heights = new IntegerVariable[N];

			//post the channeling to know if the task is scheduled or not
			for (int i = 0; i < N; i++) {
				heights[i] =  makeIntVar("H_" + i, new int[]{0, HEIGHTS_DATA[i]});
				_m.addConstraint(boolChanneling(usages[i], heights[i], HEIGHTS_DATA[i]));
			}
			//post the cumulative
			_m.addConstraint(cumulativeMax("unique renewable resource", TASKS, heights, CAPACITY, ""));
			//set fake tasks to establish the profile capacit
			_m.addConstraints(
					eq(usages[0], 1),
					eq(TASKS[0].start(), 1),
					eq(usages[1], 1),
					eq(TASKS[1].start(), 2),
					eq(usages[2], 1),
					eq(TASKS[2].start(), 3)
			);
			//state the objective function
			_m.addConstraint(eq(minus(sum(usages),NF), OBJ));
		}

	}

	@Override
	public void buildSolver() {
		_s = new CPSolver();
		_s.read(_m);
		//System.out.println(_s.pretty());
	}

	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO)) {
			final String str = ( 
					"model with "+ (useAlternativeResource ? "alternative resource" : "channeling constraints")+
					"\nobjective: "+_s.getVar(OBJ)+"\n"+ Arrays.toString(_s.getVar(usages))
					+"\n"+ Arrays.toString(_s.getVar(heights))+"\n"+ StringUtils.pretty(_s.getVar(TASKS))
			);
			LOGGER.info(str);			
		}
	}

	@Override
	public void solve() {
		_s.maximize(false);
	}
	
	public static void main(String[] args) {
		new CumulativeWebEx().execute(Boolean.FALSE);
		new CumulativeWebEx().execute(Boolean.TRUE);
	}

}
