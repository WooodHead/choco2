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
package samples.Examples;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.memory.recomputation.EnvironmentRecomputation;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Arrays;

/**
 * @author Arnaud Malapert
 *
 */
public class GolombRuler extends PatternExample {

	public final static int[][] OPTIMAL_RULER =	{ 
		{5, 11}, {6, 17}, {7, 25}, {8, 34}, {9, 44}, {10, 55}, {11, 72}
	};
	
    IntegerVariable[] ticks, diff;
	public int m;
	public int length;
    private boolean alldiff = false;
    
    @Override
    public void setUp(Object paramaters) {

        if(paramaters instanceof Object[]){
            Object[] params = (Object[])paramaters;
            m = (Integer)params[0];
            length = (Integer)params[1];
            alldiff = (Boolean)params[2];
        }
    }

    @Override
    public void buildModel() {
        _m = new CPModel();
		ticks = makeIntVarArray("a", m, 0,length);
		diff = makeIntVarArray("d", ((m)*(m-1))/2, 0,length);
		breakSymmetries();
		setAuxVarConstraints();
		if(alldiff) {
			_m.addConstraint(allDifferent(diff));
		}else {
			// d_ij != d_kl
			for (int i = 0; i < diff.length; i++) {
				for (int j = i+1; j < diff.length; j++) {
					_m.addConstraint(neq(diff[i],diff[j]));
				}
			}
		}
    }

    @Override
    public void buildSolver() {
       _s = new CPSolver();
       _s.read(_m);
        _s.monitorBackTrackLimit(true);
    }

    @Override
    public void solve() {
    	//ChocoLogging.setVerbosity(Verbosity.SEARCH);
    	//ChocoLogging.setLoggingMaxDepth(3);
    	_s.solveAll();
    }

    @Override
    public void prettyOut() {
        LOGGER.info(Arrays.toString(_s.getVar(ticks)));
    }

    private void breakSymmetries() {
		//monotony
		for (int i = 2; i < m; i++) {
			_m.addConstraint(geq(ticks[i],plus(ticks[i-1],1)));
		}
		//translation
		_m.addConstraint(eq(ticks[0], 0));
		//reflexion
		_m.addConstraint(leq(plus(minus(ticks[1],ticks[0]),1),minus(ticks[m-1],ticks[m-2])));
	}

	private void setAuxVarConstraints() {
		//d_ij = x_j - x_i et d_ij< dik
		int cpt =0;
		for (int i = 0; i < m; i++) {
			for (int j = i+1; j < m; j++) {
				_m.addConstraint(eq(diff[cpt++],minus(ticks[j],ticks[i])));
				if(j>i+1) {_m.addConstraint(geq(diff[cpt-1],plus(diff[cpt-2],1)));}
			}
		}
	}

	

	@Override
	public void execute() {
		execute(new Object[]{OPTIMAL_RULER[4][0], OPTIMAL_RULER[4][1], true});
	}

	public static void main(String[] args) {
//		final GolombRuler ruler = new GolombRuler();
//		for (int i = 0; i < OPTIMAL_RULER.length - 2; i++) {
//			ruler.execute(new Object[]{OPTIMAL_RULER[i][0], OPTIMAL_RULER[i][1], true});
//		}
		new GolombRuler().execute();
		//new GolombRuler().execute(new Object[]{OPTIMAL_RULER[5][0], OPTIMAL_RULER[5][1], true});
	}

}