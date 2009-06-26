package samples.Examples;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.variables.integer.IntegerVariable;

import java.util.Arrays;
import java.util.Random;

/**
 * Let consider a set of N boolean variables and a binary constraint network (eq or neq).
 * The goal is to find an assignment minimizing the number of required edge, or constraint, deletion.
 * The problem is inspired from the Minimum Equivalence Deletion Problem
 * @author Arnaud Malapert</br> 
 * @since 22 mars 2009 version 2.0.3</br>
 * @version 2.0.3</br>
 */
public class MinimumEdgeDeletion extends PatternExample {

	protected IntegerVariable[] pairVars;

	protected IntegerVariable[] boolVars;

	protected IntegerVariable deletion;

	protected int nbBools;

	protected double pairProba;
	
	protected int nbPairs;

	protected int[] instantiated;
	
	
	protected Boolean[][] pairs;

	/**
	 * the function takes the following arguments: 
	 * the number of variable;
	 * the probability of an edge constraint;
	 * a seed (optional).
	 */
	@Override
	public void setUp(Object paramaters) {
		final Object[] params = (Object[]) paramaters;
		nbBools = (Integer) params[0];
		pairProba = (Double) params[1];
		nbPairs = 0;
		pairs = new Boolean[nbBools][nbBools];
		instantiated = new int[nbBools];
		final Random rnd = params.length == 3 ? new Random( (Integer) params[2]) : new Random();
		for (int i = 0; i < nbBools; i++) {
			for (int j = 0; j < i; j++) {
				//equivalence existence
				if( rnd.nextDouble() < pairProba) {
					pairs[i][j]= rnd.nextBoolean();
					nbPairs++;
				}
			}
		}	
	}






	@Override
	public void buildModel() {
		_m = new CPModel();
		boolVars = makeBooleanVarArray("b", nbBools);
		_m.addVariables(boolVars);
		pairVars = new IntegerVariable[nbPairs];
		deletion = makeIntVar("deletion", 0, nbPairs,"cp:objective");
		int cpt = 0;
		for (int i = 0; i < nbBools; i++) {
			for (int j = 0; j < nbBools; j++) {
				if(pairs[i][j] == Boolean.TRUE) {
					pairVars[cpt] = makeBooleanVar("eq_"+i+"_"+j);
					_m.addConstraint( reifiedIntConstraint(pairVars[cpt], eq(boolVars[i], boolVars[j])));	
					cpt++;
				}else if(pairs[i][j] == Boolean.FALSE) {
					pairVars[cpt] = makeBooleanVar("neq_"+i+"_"+j);
					_m.addConstraint( reifiedIntConstraint(pairVars[cpt], neq(boolVars[i], boolVars[j])));	
					cpt++;
				}
			}
		}
		_m.addConstraint( eq( minus(cpt, sum(pairVars)), deletion));
		//LOGGER.info(_m.pretty());
	}

	@Override
	public void buildSolver() {
		_s = new CPSolver();
		_s.read(_m);
		_s.setFirstSolution(false);
		_s.setDoMaximize(false);
		_s.generateSearchStrategy();
	}

	@Override
	public void prettyOut() {
		LOGGER.info("pairs: "+Arrays.toString(_s.getVar(pairVars)));
		LOGGER.info("nbDeletions= "+ _s.getOptimumValue());
		LOGGER.info("bool vars: "+Arrays.toString(_s.getVar(boolVars)));
	}

	@Override
	public void solve() {
		_s.launch();
	}


	public static void main(String[] args) {
		Object parameters = new Object[]{7,0.5,0};
		MinimumEdgeDeletion pb = new MinimumEdgeDeletion();
		pb.execute(parameters);
	}
}