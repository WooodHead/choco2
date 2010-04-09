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
package choco.solver.search.set;

import static choco.Choco.eqCard;
import static choco.Choco.geqCard;
import static choco.Choco.isIncluded;
import static choco.Choco.leqCard;
import static choco.Choco.makeIntVar;
import static choco.Choco.makeSetVar;
import static choco.Choco.member;
import static choco.Choco.neq;
import static choco.Choco.notMember;
import static choco.Choco.setDisjoint;
import static choco.Choco.setInter;
import static choco.Choco.setUnion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import samples.Examples.MinimumEdgeDeletion;
import choco.cp.CPOptions;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.MinVal;
import choco.cp.solver.search.set.MinDomSet;
import choco.cp.solver.search.set.MinEnv;
import choco.cp.solver.search.set.RandomSetValSelector;
import choco.cp.solver.search.set.RandomSetVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.propagation.Propagator;

public class SearchTest {

	protected static final Logger LOGGER = ChocoLogging.getTestLogger();
	private Model m;
	private Solver s;
	private SetVariable x;
	private Constraint c1;
	private Constraint c2;
	private Constraint c3;

	@Before
	public void setUp() {
		LOGGER.fine("EqualXC Testing...");
		m = new CPModel();
		s = new CPSolver();
	}

	@After
	public void tearDown() {
		c1 = null;
		c2 = null;
		c3 = null;
		x = null;
		m = null;
		s = null;
	}

	/**
	 * A ternary Steiner system of order n is a set of triplets of distinct elements
	 * taking their values between 1 and n, such that all the pairs included in two different triplets are different.
	 * une solution pour n = 7 : [{1, 2, 3}, {2, 4, 5}, {3, 4, 6}, {1, 4, 7}, {1, 5, 6}, {2,6, 7}, {3, 5, 7}]
	 * Il faut que n % 6 = 1 ou n % 6 = 3 pour n soit une valeur valide pour le pb
	 * @param p size
	 */
	public void steinerSystem(int p) {
		int n = p * (p - 1) / 6;
		SetVariable[] vars = new SetVariable[n];
		SetVariable[] intersect = new SetVariable[n * n];
		for (int i = 0; i < n; i++)
			vars[i] = makeSetVar("set " + i, 1, n);
		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
				intersect[i * n + j] = makeSetVar("interSet " + i + " " + j, 1, n);

		for (int i = 0; i < n; i++)
			m.addConstraint(eqCard(vars[i], 3));
		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				m.addConstraint(setInter(vars[i], vars[j], intersect[i * n + j]));
				m.addConstraint(leqCard(intersect[i * n + j], 1));
			}
		}
		s.read(m);
		s.setVarSetSelector(new MinDomSet(s, s.getVar(vars)));
		s.setValSetSelector(new MinEnv());
		s.solve();
		LOGGER.log(Level.INFO, "NbSolution {0}", s.getNbSolutions());
		for (int i = 0; i < n; i++) {
			LOGGER.info("set[" + i + "]:" + s.getVar(vars[i]).pretty());
		}
		assertTrue(s.isFeasible());
	}

	@Test
	public void test2() {
		steinerSystem(7);
	}

	@Test
	public void test1() {
		x = makeSetVar("X", 1, 5);
		c1 = member(x, 3);
		c2 = member(x, 5);
		c3 = notMember(x, 2);
		LOGGER.finer("test1");
		try {
			m.addConstraint(c1);
			m.addConstraint(c2);
			m.addConstraint(c3);
			s.read(m);
			s.propagate();
		} catch (ContradictionException e) {
			assertTrue(false);
		}
		assertTrue(s.getVar(x).isInDomainKernel(3));
		assertTrue(s.getVar(x).isInDomainKernel(5));
		assertTrue(!s.getVar(x).isInDomainKernel(2));
		s.setVarSetSelector(new MinDomSet(s));
		s.setValSetSelector(new MinEnv());
		s.solveAll();

		assertEquals(4, s.getNbSolutions());
	}

	@Test
	public void test3() {
		for (int i = 0; i < 10; i++) {
			m = new CPModel();
			s = new CPSolver();
			SetVariable s1 = makeSetVar("s1", 1, 3);
			SetVariable x = makeSetVar("x", 1, 3);
			SetVariable y = makeSetVar("y", 1, 3);
			m.addConstraint(setUnion(x, y, s1));
			m.addConstraint(setDisjoint(x, y));
			m.addConstraint(geqCard(x, 1));

			s.read(m);
			s.setVarSetSelector(new RandomSetVarSelector(s, i));
			s.setValSetSelector(new RandomSetValSelector(i + 1));
			s.solveAll();


			LOGGER.info("Nb solution: " + s.getNbSolutions());

			//			LOGGER.info("" + s1);
			//			LOGGER.info("" + x);
			//			LOGGER.info("" + y);

			assertTrue(19 == s.getNbSolutions());
			//pb.getSolver().setVarSetSelector(new StaticSetVarOrder(new SetVar[]{x, y , s1}));
			//pb.solve();
			//pb.getSolver().setVarSetSelector(new StaticSetVarOrder(new SetVar[]{x, y , s1}));
		}
	}

	@Test
	public void test4() {

		Model m = new CPModel();
		SetVariable s1 = makeSetVar("s1", 1, 10);
		m.addConstraint(geqCard(s1, 1));
		m.addConstraint(eqCard(s1, 0));
		Solver s = new CPSolver();
		s.read(m);
		s.setVarSetSelector(new RandomSetVarSelector(s));
		s.setValSetSelector(new RandomSetValSelector());
		boolean contr = false;
		try {
			s.propagate();
		} catch(ContradictionException e) {
			contr = true;
		}
		assertTrue(contr);

	}

	@Test
	public void test5() {
		Model m = new CPModel();
		SetVariable s1 = makeSetVar("s1", 1, 10);
		SetVariable s2 = makeSetVar("s2", 1, 10);
		SetVariable s3 = makeSetVar("s3", 1, 10);

		m.addConstraint(setInter(s1, s2, s3));
		m.addConstraint(eqCard(s1, 0));
		m.addConstraint(geqCard(s3, 1));
		Solver s = new CPSolver();
		s.read(m);
		try {
			s.propagate();
		} catch (ContradictionException e) {
			LOGGER.info("contradiction");
		}

		DisposableIterator it = s.getConstraintIterator();
		while(it.hasNext()) {
			Propagator prop = (Propagator) it.next();
			prop.constAwake(true);
		}
        it.dispose();
		s.solve();
		assertFalse(s.isFeasible());
	}

	@Test
	public void testPerformance1() {
		Model m = new CPModel();
		SetVariable object = makeSetVar("object", 1, 6);
		SetVariable a = makeSetVar("a", 1, 6);
		SetVariable b = makeSetVar("b", 1, 6);
		m.addVariables(CPOptions.V_BOUND, object, a, b);

		// Fix tthe IntVar to value 1.
		IntegerVariable object1 = makeIntVar("object1", 1, 1);
		m.addVariable(CPOptions.V_BOUND, object1);

		// Make constraints on the sets
		// setInter(s1, s2, s1) <=> s1 \subseteq s2

		// notype \subseteq and any other type is empty
		// m.addConstraint(pb.setInter(notype, a, notype));
		// m.addConstraint(pb.setInter(notype, b, notype));
		// m.addConstraint(pb.setInter(notype, object, notype));

		// a \subseteq object
		m.addConstraint(setInter(a, object, a));
		// b \subseteq a
		m.addConstraint(setInter(b, a, b));
		// b \subseteq object (redundant)
		// m.addConstraint(pb.setInter(b, object, b));

		// a should be unequal to b
		SetVariable x = makeSetVar("_eqHelperX", 1, 6);
		SetVariable y = makeSetVar("_eqHelperY", 1, 6);
		SetVariable z = makeSetVar("_eqHelperZ", 1, 6);
		SetVariable yz = makeSetVar("_eqHelperYZ", 1, 6);

		m.addConstraint(setUnion(x, y, a));
		m.addConstraint(setUnion(x, z, b));

		m.addConstraint(setDisjoint(x, y));
		m.addConstraint(setDisjoint(x, z));
		m.addConstraint(setDisjoint(y, z));

		m.addConstraint(setUnion(y, z, yz));
		m.addConstraint(geqCard(yz, 1));

		Solver s = new CPSolver();
		s.read(m);

		try {
			s.propagate();
			if (s.solve()) {
				LOGGER.info(s.solutionToString());
			}
			LOGGER.info("isFeas: " + s.isFeasible());
			s.printRuntimeStatistics();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testPerformance2() {
		//		pb.setCardReasoning(false);
		Model m = new CPModel();
		SetVariable object = makeSetVar("object", 1, 7);
		SetVariable a = makeSetVar("a", 1, 7);
		SetVariable b = makeSetVar("b", 1, 7);
		SetVariable c = makeSetVar("c", 1, 7);
		m.addVariables(CPOptions.V_BOUND, object, a, b, c);

		// Fix tthe IntVar to value 1.
		IntegerVariable object1 = makeIntVar("object1", 1, 1);
		m.addVariable(CPOptions.V_BOUND, object1);

		// a \subseteq object
		m.addConstraint(isIncluded(a, object));
		// b \subseteq a
		m.addConstraint(isIncluded(b, a));
		// b \subseteq object (redundant)
		m.addConstraint(isIncluded(b, object));
		// c \subseteq b
		m.addConstraint(isIncluded(c, b));
		// c \subseteq object (redundant)
		m.addConstraint(isIncluded(c, object));


		// a should be unequal to b
		m.addConstraint(neq(a,b));
		Solver s = new CPSolver();
		s.read(m);
		try {
			s.propagate();
			if (s.solve()) {
				LOGGER.info(s.solutionToString());
			}
			LOGGER.info("isFeas: " + s.isFeasible());
			s.printRuntimeStatistics();
		} catch (ContradictionException e) {
			e.printStackTrace();
		}
	}

	private int capa = 0;
	 
	class PoolSwitcher extends MinimumEdgeDeletion {

		@Override
		public void buildSolver() {
			_s = new CPSolver();
			_s.read(_m);
			_s.setFirstSolution(false);
			_s.setDoMaximize(false);
			_s.setSolutionPoolCapacity(capa);
			_s.setValIntSelector(new MinVal());
			//_s.generateSearchStrategy();
			
		}

		@Override
		public void execute() {
			super.execute();
			assertEquals(Math.min( capa, _s.getNbSolutions()),  _s.getSearchStrategy().getSolutionPool().size());
		}
						
	}
	
	@Test
	public void testSolutionPool() {
		//ChocoLogging.setVerbosity(Verbosity.SEARCH);
		PoolSwitcher pl = new PoolSwitcher();
		for (capa = 0; capa  < 7; capa++) {
			pl.execute();
		}
		capa = Integer.MAX_VALUE;
		pl.execute();
	}



}

