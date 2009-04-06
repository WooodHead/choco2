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
package choco.model.constraints.global;

import static choco.Choco.*;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 5 avr. 2006
 * Time: 08:42:43
 * To change this template use File | Settings | File Templates.
 */
public class LexTest extends TestCase {

	@Test
	public void testLessLexq() {
		for (int seed = 0; seed < 5; seed++) {
			Model pb = new CPModel();
			int n1 = 8;
			int k = 2;
			IntegerVariable[] vs1 = new IntegerVariable[n1 / 2];
			IntegerVariable[] vs2 = new IntegerVariable[n1 / 2];
			for (int i = 0; i < n1 / 2; i++) {
				vs1[i] = makeIntVar("" + i, 0, k);
				vs2[i] = makeIntVar("" + i, 0, k);
			}
			pb.addConstraint(lexeq(vs1, vs2));
			CPSolver s = new CPSolver();
			s.read(pb);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed));
			s.solve();
			do {
//		        System.out.print("[");
//		        for (int i = 0; i < vs1.length; i++) {
//			        System.out.print("" + s.getVar(vs1[i]).getVal() + ((i == (vs1.length - 1)) ? "" : " "));
//		        }
//		        System.out.print("]");
//		        System.out.print(" <=leq [");
//		        for (int i = 0; i < vs1.length; i++) {
//			        System.out.print("" + s.getVar(vs2[i]).getVal() + ((i == (vs2.length - 1)) ? "" : " "));
//		        }
//		        System.out.println("]");
			} while (s.nextSolution() == Boolean.TRUE);
			//s.solveAll();
			int kpn = (int) Math.pow(k + 1, n1 / 2);
			assertEquals(s.getNbSolutions(), (kpn * (kpn + 1) / 2));
			//if (s.getNbSolutions() != kpn*(kpn + 1)/2 )
			//throw new Error("nbSol " + s.getNbSolutions() + " should be " + (kpn*(kpn + 1)/2));
			System.out.println("NbSol : " + s.getNbSolutions() + " =? " + (kpn * (kpn + 1) / 2));
		}
	}

	@Test
	public void testLex() {
		for (int seed = 0; seed < 5; seed++) {
			Model pb = new CPModel();
			int n1 = 8;
			int k = 2;
			IntegerVariable[] vs1 = new IntegerVariable[n1 / 2];
			IntegerVariable[] vs2 = new IntegerVariable[n1 / 2];
			for (int i = 0; i < n1 / 2; i++) {
				vs1[i] = makeIntVar("" + i, 0, k);
				vs2[i] = makeIntVar("" + i, 0, k);
			}
			pb.addConstraint(lex(vs1, vs2));
			CPSolver s = new CPSolver();
			s.read(pb);
			s.setVarIntSelector(new RandomIntVarSelector(s, seed));
			s.setValIntSelector(new RandomIntValSelector(seed));
			//s.setValIterator(new IncreasingTrace());
			s.solveAll();
			assertEquals(3240, s.getNbSolutions());
			System.out.println("NbSol : " + s.getNbSolutions() + " =? 3240");
		}
	}

	@Test
	public void testLexiMin() {
		for (int seed = 0; seed < 10; seed++) {
			CPModel p = new CPModel();
			IntegerVariable[] u = makeIntVarArray("u", 3, 2, 5);
			IntegerVariable[] v = makeIntVarArray("v", 3, 2, 4);
			p.addConstraint(leximin(u, v));
			p.addConstraint(allDifferent(v));
			CPSolver s = new CPSolver();
			s.read(p);
			s.setValIntSelector(new RandomIntValSelector(seed));
			s.setVarIntSelector(new RandomIntVarSelector(s, seed + 2));
			s.solve();

			do {
				System.out.print("u = [ " + s.getVar(u[0]).getVal() + " " + s.getVar(u[1]).getVal() + " " + s.getVar(u[2]).getVal() + " ] - ");
				System.out.println("v = [ " + s.getVar(v[0]).getVal() + " " + s.getVar(v[1]).getVal() + " " + s.getVar(v[2]).getVal() + " ]");
			} while (s.nextSolution() == Boolean.TRUE);
			assertEquals(78, s.getNbSolutions());
		}
	}

	@Test
	public void testLexiSatisfied() {
		Model pb = new CPModel();
		IntegerVariable v1 = makeIntVar("v1", 1, 1);
		IntegerVariable v2 = makeIntVar("v2", 2, 2);
		IntegerVariable v3 = makeIntVar("v3", 3, 3);
		Constraint c1 = lex(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v3});
		Constraint c2 = lex(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v2});
		Constraint c3 = lex(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v1});
		Constraint c4 = lexeq(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v3});
		Constraint c5 = lexeq(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v2});
		Constraint c6 = lexeq(new IntegerVariable[]{v1, v2}, new IntegerVariable[]{v1, v1});
		pb.addConstraints(c1, c2, c3, c4, c5, c6);
		CPSolver s = new CPSolver();
		s.read(pb);
		System.out.println(c2.pretty());
		System.out.println(c5.pretty());
		assertTrue(s.getCstr(c1).isSatisfied());
		assertFalse(s.getCstr(c2).isSatisfied());
		assertFalse(s.getCstr(c3).isSatisfied());
		assertTrue(s.getCstr(c4).isSatisfied());
		assertTrue(s.getCstr(c5).isSatisfied());
		assertFalse(s.getCstr(c6).isSatisfied());
	}


	    @Test
    public void testAshish(){
		Model m = new CPModel();
		Solver s = new CPSolver();
		IntegerVariable[] a = new IntegerVariable[2];
		IntegerVariable[] b = new IntegerVariable[2];

		a[0] = makeIntVar("a1", 5, 7);
		a[1] = makeIntVar("a2", 1, 1);

		b[0] = makeIntVar("b1", 5, 8);
		b[1] = makeIntVar("b2", 0, 0);


		m.addConstraint(lex(a, b));
		s.read(m);
		
		try {
			s.propagate();

		} catch (ContradictionException e) {
			 assertFalse(false);
		}
		s.solve();
		do {
		        System.out.print("[");
		        for (int i = 0; i < a.length; i++) {
			        System.out.print("" + s.getVar(a[i]).getVal() + ((i == (a.length - 1)) ? "" : " "));
		        }
		        System.out.print("]");
		        System.out.print(" < [");
		        for (int i = 0; i < b.length; i++) {
			        System.out.print("" + s.getVar(b[i]).getVal() + ((i == (b.length - 1)) ? "" : " "));
		        }
		        System.out.println("]");
		} while (s.nextSolution() == Boolean.TRUE);

		//System.out.println("" + s.getNbSolutions());
		assertTrue(s.getNbSolutions() > 0);
	}
}
