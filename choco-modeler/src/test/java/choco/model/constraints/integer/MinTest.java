/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.model.constraints.integer;

import choco.Choco;
import choco.Options;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.RandomIntVarSelector;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.ContradictionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.logging.Logger;

import static choco.Choco.*;
import static choco.model.constraints.integer.MaxTest.testAll;
import static java.text.MessageFormat.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 30 janv. 2007
 * Time: 10:49:34
 * To change this template use File | Settings | File Templates.
 */
public class MinTest{

    protected final static Logger LOGGER = ChocoLogging.getTestLogger();

    private CPModel m;
    private CPSolver s;

    @Before
    public void before(){
        m = new CPModel();
        s = new CPSolver();
    }

    @After
    public void after(){
        m = null;
        s = null;
    }

    @Test
    public void test1() {
        for (int i = 0; i <= 10; i++) {
            m = new CPModel();
            s = new CPSolver();
            IntegerVariable x = makeIntVar("x", 1, 5);
            IntegerVariable y = makeIntVar("y", 1, 5);
            IntegerVariable z = makeIntVar("z", 1, 5);
            IntegerVariable w = makeIntVar("w", 1, 5);
            m.addConstraint(min(new IntegerVariable[]{x, y, z},w));
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i + 1));
            s.solve();
            do {
                /*LOGGER.info("" + x.getVal() + "=max(" + y.getVal() + "," +
                z.getVal()+")");*/
            } while (s.nextSolution() == Boolean.TRUE);
            LOGGER.info("" + s.getSearchStrategy().getNodeCount());
            assertEquals(125, s.getNbSolutions());
            //LOGGER.info("Nb solution : " + s.getNbSolutions());
        }
    }

    @Test
    public void test2() {
        for (int i = 0; i <= 10; i++) {
            m = new CPModel();
            s = new CPSolver();
            IntegerVariable x = makeIntVar("x", 1, 5);
            IntegerVariable y = makeIntVar("y", 1, 5);
            IntegerVariable z = makeIntVar("z", 1, 5);
            m.addVariables(Options.V_BOUND, x, y, z);
            IntegerVariable w = makeIntVar("w", 1, 5);
            m.addConstraint(min(new IntegerVariable[]{x, y, z},w));
            s.read(m);
            s.setVarIntSelector(new RandomIntVarSelector(s, i));
            s.setValIntSelector(new RandomIntValSelector(i + 1));
            s.solve();
            do {
                //LOGGER.info("" + x.getVal() + "=max(" + y.getVal() + "," +
                //�    z.getVal()+")");
            } while (s.nextSolution() == Boolean.TRUE);
            LOGGER.info("" + s.getSearchStrategy().getNodeCount());
            assertEquals(125, s.getNbSolutions());
            //LOGGER.info("Nb solution : " + s.getNbSolutions());
        }
    }

    @Test
    public void testPropagMinTern1() {

        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 4, 5);
        IntegerVariable min = makeIntVar("min", 1, 5);
        m.addConstraint(min(z, y, min));
        s.read(m);
        try {
            s.getVar(min).remVal(3);
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
        LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
        LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
        LOGGER.info(format("{0}", !s.getVar(y).canBeInstantiatedTo(3)));
        assertTrue(!s.getVar(y).canBeInstantiatedTo(3));
    }

    @Test
    public void testPropagMinTern2() {

        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        IntegerVariable min = makeIntVar("min", 1, 5);
        m.addConstraint(min(z, y, min));
        s.read(m);
        try {
            s.getVar(y).remVal(3);
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
        LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
        LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
        LOGGER.info(format("{0}", s.getVar(z).canBeInstantiatedTo(3) && s.getVar(min).canBeInstantiatedTo(3)));
        assertTrue(s.getVar(z).canBeInstantiatedTo(3) && s.getVar(min).canBeInstantiatedTo(3));
    }

    @Test
    public void testPropagMinTern3() {
        IntegerVariable y = makeIntVar("y", 1, 5);
        IntegerVariable z = makeIntVar("z", 1, 5);
        IntegerVariable min = makeIntVar("min", 1, 5);
        m.addConstraint(min(z, y, min));
        s.read(m);
        try {
            s.getVar(min).remVal(3);
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
        LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
        LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
        LOGGER.info(format("{0}", s.getVar(y).canBeInstantiatedTo(3) && s.getVar(z).canBeInstantiatedTo(3)));
        assertTrue(s.getVar(y).canBeInstantiatedTo(3) && s.getVar(z).canBeInstantiatedTo(3));
    }

    @Test
    public void testPropagMinTern4() {
        IntegerVariable y = makeIntVar("y", 1, 3);
        IntegerVariable z = makeIntVar("z", 4, 6);
        IntegerVariable min = makeIntVar("min", 1, 6);
        m.addConstraint(min(z, y, min));
        s.read(m);
        try {
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        LOGGER.info(format("min {0}", s.getVar(min).getDomain().pretty()));
        LOGGER.info(format("y {0}", s.getVar(y).getDomain().pretty()));
        LOGGER.info(format("z {0}", s.getVar(z).getDomain().pretty()));
        LOGGER.info(format("{0}", s.getVar(min).getDomain().getSize() == 3));
        assertTrue(s.getVar(min).getDomain().getSize() == 3);
    }


    @Test
    public void testRandom() {

        for (int i = 0; i < 10; i++) {

            m = new CPModel();
            s = new CPSolver();
            IntegerVariable varA = makeIntVar("varA", 0, 3);
            IntegerVariable varB = makeIntVar("varB", 0, 3);
            IntegerVariable varC = makeIntVar("varC", 0, 3);
            m.addConstraint(min(varA, varB, varC));
            s.read(m);

            //-----Now get solutions
            s.setFirstSolution(true);
            s.generateSearchStrategy();
            s.setValIntSelector(new RandomIntValSelector(100 + i));
            s.setVarIntSelector(new RandomIntVarSelector(s, 101 + i));

            //LOGGER.info("Choco Solutions");
            int nbSolution = 0;
            if (s.solve() == Boolean.TRUE) {
                do {
                    //LOGGER.info("Min(" + ((IntegerVariable) chocoCSP.getIntVar(0)).getVal() + ", " + ((IntegerVariable) chocoCSP.getIntVar(1)).getVal() + ") = " + ((IntegerVariable) chocoCSP.getIntVar(2)).getVal());
                    nbSolution++;
                } while (s.nextSolution() == Boolean.TRUE);
            }
            assertEquals(nbSolution, 16);
        }
    }

    @Test
    public void testConstant(){
        for (int i = 0; i < 10; i++) {

            m = new CPModel();
            s= new CPSolver();


            IntegerVariable x = makeIntVar("x", 0, 3);
            IntegerVariable y = makeIntVar("y", 0, 3);
            m.addConstraint(eq(y, min(x, 1)));

            //-----Now get solutions

            s.read(m);
            s.setFirstSolution(true);
            s.generateSearchStrategy();
            s.setValIntSelector(new RandomIntValSelector(100 + i));
            s.setVarIntSelector(new RandomIntVarSelector(s, i));


            //LOGGER.info("Choco Solutions");
            int nbSolution = 0;
            if (s.solve() == Boolean.TRUE) {
                do {
                    //LOGGER.info("Max(" + ((IntegerVariable) chocoCSP.getIntVar(0)).getVal() + ", " + ((IntegerVariable) chocoCSP.getIntVar(1)).getVal() + ") = " + ((IntegerVariable) chocoCSP.getIntVar(2)).getVal());
                    nbSolution++;
                } while (s.nextSolution() == Boolean.TRUE);
            }

            assertEquals(nbSolution, 4);
        }
    }
    @Test
	public void testSet1() {
		testAll(true,true);
	}

	@Test
	public void testSet2() {
		testAll(true,false);
	}
	
	@Test
	public void testEmptySetDefValue() {
		IntegerVariable[] vars = Choco.constantArray(new int[]{1,2,3});
        IntegerVariable min = makeIntVar("min", 0, 3);
        SetVariable svar = makeSetVar("sv", 0, 2);
        m.addConstraint(min(svar, vars, min, 0));
        s.read(m);
        s.solveAll();
        assertEquals("nb-sols", 8, s.getNbSolutions());        
	}

    @Test
    public void testOneVarMin() {
        IntegerVariable[] vars = makeIntVarArray("vars", 1, 3, 5);
        IntegerVariable min = makeIntVar("min", 1, 6);
        m.addConstraint(eq(min, min(vars)));
        s.read(m);
        try {
            s.propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
        }
        LOGGER.info("min " + s.getVar(min).getDomain().pretty());
        assertTrue(s.getVar(min).getDomain().getSize() == 3);
    }



}