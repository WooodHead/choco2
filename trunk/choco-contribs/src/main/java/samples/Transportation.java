package samples;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import samples.Examples.PatternExample;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.constraints.integer.IntLinComb;
import choco.cp.solver.constraints.integer.IntLinComb2;
import choco.cp.solver.constraints.integer.IntSum;
import choco.cp.solver.search.integer.valselector.MaxVal;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.comparator.IPermutation;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.common.util.tools.MathUtils;
import choco.kernel.common.util.tools.PermutationUtils;
import choco.kernel.memory.IEnvironment;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * Assume that a product is to be shipped in the amounts u1, ... , um, from each of m shipping origins, and received in amounts v1, ... , vn, by each of n shipping destinations. 
 * The problem consists of determining the amounts xij, to be shipped from origins i to destinations j, to minimize the cost of transportation.
 * @author Arnaud Malapert</br> 
 * @since 7 oct. 2009 version 2.1.1</br>
 * @version 2.1.1</br>
 */
public class Transportation extends PatternExample {


	public final static int MAX_DEMAND_OR_STOCK = 10;
	protected Random rnd;
	/** number of origins */
	public int n;

	/** Amount of product available at the origins. */
	public int[] stocks;

	/** number of destinations */
	public int m;

	/** Amount of products received by each destination. */
	public int[] demands;

	/** total amount of products available at the origin or  received by the destination*/
	public int totalStockOrDemand;

	/** Transportation cost from the origin i to the destination j*/
	public int[][] costs;

	public IntegerVariable[][] shipping;

	public IntegerVariable objective;

	public Transportation() {}

	@Override
	public void setUp(Object parameters) {
		super.setUp(parameters);
		int[] params = (int[]) parameters;
		n = params[0];
		m = params[1];
		rnd = params.length == 3 ? new Random(params[2]) : new Random();
		//stocks and demands are generated by randomly solving a CP problem
		Model model = new CPModel();
		IntegerVariable[] d = Choco.makeIntVarArray("d", m, 1, MAX_DEMAND_OR_STOCK);
		IntegerVariable[] s = Choco.makeIntVarArray("d", n, 1, MAX_DEMAND_OR_STOCK);
		IntegerVariable t = Choco.makeIntVar("total", 0, Math.min(m, n) * MAX_DEMAND_OR_STOCK);
		model.addConstraints(
				Choco.eq(Choco.sum(d), t),
				Choco.eq(Choco.sum(s), t)
		);
		CPSolver solver = new CPSolver();
		solver.read(model);
		solver.setRandomSelectors(rnd.nextInt(100));
		solver.solve();
		stocks = new int[n];
		for (int i = 0; i < n; i++) {
			stocks[i] = solver.getVar( s[i]).getVal();
		}
		demands = new int[m];
		for (int i = 0; i < m; i++) {
			demands[i] = solver.getVar( d[i]).getVal();
		}
		totalStockOrDemand = solver.getVar(t).getVal();
		//costs
		costs = new int[n][m];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				costs[i][j] = rnd.nextInt(15)+1;
			}
		}
	}



	@Override
	public void buildModel() {
		_m = new CPModel();
		shipping = Choco.makeIntVarArray("s", n, m, 0, MathUtils.max(stocks), "cp:bound","cp:decision");
		for (int i = 0; i < n; i++) {
			//The total amount shipped from origin i must be equals to the sum of amount going from it to all destinations
			_m.addConstraint( Choco.eq( Choco.sum(shipping[i]), stocks[i]));
		}
		for (int i = 0; i < m; i++) {
			//The total amount received at destination i must be equals to the sum of amount shipped to it from all origins.
			_m.addConstraint( Choco.eq( Choco.sum( ArrayUtils.getColumn(shipping, i)), demands[i]));
		}
		//objective function
		objective = Choco.makeIntVar("total_cost", 0, MathUtils.max(costs) * totalStockOrDemand, "cp:objective cp:bound");
		IntegerExpressionVariable objExp = Choco.constant(0);
		for (int i = 0; i < n; i++) {
			objExp = Choco.plus(objExp, Choco.scalar(shipping[i], costs[i]));
		}
		_m.addConstraint( Choco.eq(objective, objExp));
	}

	@Override
	public void buildSolver() {
		CPSolver solver = new CPSolver2();
		_s = solver;
		_s.read(_m);
		//System.out.println(_s.pretty());
		//Naive Search Strategy: sort the variables shipping_ij accroding to their increasing costs
		//Select shipping with minimal cost and assign its maximal value
		final List<IntDomainVar> dvars = solver.getIntDecisionVars();
		final IntDomainVar[] sdvars = new IntDomainVar[dvars.size()];
		final IPermutation permutation = PermutationUtils.getSortingPermuation( ArrayUtils.flatten(costs));
		permutation.applyPermutation(dvars, sdvars);
		_s.setVarIntSelector(new StaticVarOrder(sdvars));
		_s.setValIntSelector(new MaxVal());
	}

	@Override
	public void prettyOut() {
		if(LOGGER.isLoggable(Level.INFO)) {
			StringBuilder b = new StringBuilder();
			for (int i = 0; i < shipping.length; i++) {
				for (int j = 0; j < shipping[i].length; j++) {
					final int v = _s.getVar(shipping[i][j]).getVal();
					if(v > 0) {
						b.append(i).append(" -> ").append(j);
						b.append(": ").append(v).append('\n');
					}
				}
			}
			LOGGER.info(new String(b));
		}
	}

	@Override
	public void solve() {
		//System.out.println(_s.pretty());
		_s.minimize(false);

	}

	public static void main(String[] args) {
		//ChocoLogging.setVerbosity(Verbosity.VERBOSE);
		//new Transportation().execute(new int[]{4, 4, 1});
		//new Transportation().execute(new int[]{12, 4, 1});
		//new Transportation().execute(new int[]{13, 4, 0});
		final Transportation tr = new Transportation();
		int n = 5;
		int s = 0;
		for (int i = 1; i < 4; i++) {
				tr.execute(new int[]{13, 3, i});
				s += tr._s.getTimeCount();
			}
			System.out.println(s+" ms");
	}

} 
//37138 ms

class CPSolver2 extends CPSolver {

	public CPSolver2() {
		super();
	}

	public CPSolver2(IEnvironment env) {
		super(env);
	}

	public final static boolean isIntSum(int[] sortedCoeffs, int nbPositiveCoeffs) {
		for (int i = 0; i < nbPositiveCoeffs; i++) {
			if( sortedCoeffs[i] != 1) return false;
		}
		for (int i = nbPositiveCoeffs; i < sortedCoeffs.length; i++) {
			if(sortedCoeffs[i] != -1) return false;
		}
		return true;
	}
	
	@Override
	protected SConstraint createIntLinComb(IntDomainVar[] sortedVars,
			int[] sortedCoeffs, int nbPositiveCoeffs, int c, int linOperator) {
		if (isBoolLinComb(sortedVars, sortedCoeffs, linOperator)) {
			return createBoolLinComb(sortedVars, sortedCoeffs, c, linOperator);
		} else if ( isIntSum(sortedCoeffs, nbPositiveCoeffs)) {
			return new IntSum(sortedVars, sortedCoeffs, nbPositiveCoeffs, c);
		} else {
			return new IntLinComb2(sortedVars, sortedCoeffs, nbPositiveCoeffs, c);
		}
	}



}
