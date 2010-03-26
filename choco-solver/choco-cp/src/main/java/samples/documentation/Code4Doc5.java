/* ************************************************
 *           _       _                            *
 *          |  �(..)  |                           *
 *          |_  J||L _|        CHOCO solver       *
 *                                                *
 *     Choco is a java library for constraint     *
 *     satisfaction problems (CSP), constraint    *
 *     programming (CP) and explanation-based     *
 *     constraint solving (e-CP). It is built     *
 *     on a event-based propagation mechanism     *
 *     with backtrackable structures.             *
 *                                                *
 *     Choco is an open-source software,          *
 *     distributed under a BSD licence            *
 *     and hosted by sourceforge.net              *
 *                                                *
 *     + website : http://choco.emn.fr            *
 *     + support : choco@emn.fr                   *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                   N. Jussien    1999-2009      *
 **************************************************/
package samples.documentation;

import choco.Choco;
import static choco.Choco.*;
import choco.cp.CPOptions;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.SettingType;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.automaton.DFA;
import choco.kernel.model.constraints.automaton.Transition;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.model.variables.tree.TreeParametersObject;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.extension.CouplesTest;
import choco.kernel.solver.constraints.integer.extension.TuplesTest;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;


/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 29 oct. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*
* See Code4Doc1.java for more informations.
*/
public class Code4Doc5 {

    public void cor1(){
        //totex cor1
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 1);
        IntegerVariable v2 = makeIntVar("v2", 0, 1);
        m.addConstraint(or(eq(v1, 1), eq(v2, 1)));
        s.read(m);
        s.solve();
        //totex
    }

    public void cor2(){
        //totex cor2
       Model m = new CPModel();
       Solver s = new CPSolver();
       IntegerVariable[] vars = makeBooleanVarArray("b", 10);
       m.addConstraint(or(vars));
       s.read(m);
       s.solve();
       //totex
    }

    public void cpack(){
        //totex cpack
        Model m = new CPModel();
        m.addConstraint(pack(new int[]{5,3,2,6,8,5},5,10, SettingType.ADDITIONAL_RULES.
            getOptionName()));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void cprecedencereified(){
        //totex cprecedencereified
        int k1 = 5;
        Model m = new CPModel();
		IntegerVariable x = makeIntVar("x", 1, 10);
		IntegerVariable y = makeIntVar("y", 1, 10);
		m.addVariables(CPOptions.V_BOUND, x, y);
		IntegerVariable z = makeIntVar("z", 0, 1);
		m.addConstraint(precedenceReified(x,k1,y,z));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void cregular1(){
        //totex cregular1
        //1- Create the model
        Model m = new CPModel();
        int n = 6;
        IntegerVariable[] vars = new IntegerVariable[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = makeIntVar("v" + i, 0, 5);
        }
        //2- Build the list of transitions of the DFA
        List<Transition> t = new LinkedList<Transition>();
        t.add(new Transition(0, 1, 1));
        t.add(new Transition(1, 1, 2));
        // transition with label 1 from state 2 to state 3
        t.add(new Transition(2, 1, 3));
        t.add(new Transition(3, 3, 0));
        t.add(new Transition(0, 3, 0));
        //3- Two final states: 0, 3
        List<Integer> fs = new LinkedList<Integer>();
        fs.add(0); fs.add(3);
        //4- Build the DFA
        DFA auto = new DFA(t, fs, n);
        //5- add the constraint
        m.addConstraint(regular(auto, vars));
        //6- create the solver, read the model and solve it
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        do {
            for (int i = 0; i < n; i++)
            System.out.print(s.getVar(vars[i]).getVal());
            System.out.println("");
        } while (s.nextSolution());
        //7- Print the number of solution found
        System.out.println("Nb_sol : " + s.getNbSolutions());
        //totex
    }

    public void cregular2(){
        //totex cregular2
        //1- Create the model
        Model m = new CPModel();
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 1, 4);
        //2- add some allowed tuples (here, the tuples define a all_equal constraint)
        List<int[]> tuples = new LinkedList<int[]>();
        tuples.add(new int[]{1, 1, 1});
        tuples.add(new int[]{2, 2, 2});
        tuples.add(new int[]{3, 3, 3});
        tuples.add(new int[]{4, 4, 4});
        //3- add the constraint
        m.addConstraint(regular(new IntegerVariable[]{v1, v2, v3}, tuples));
        //4- Create the solver, read the model and solve it
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        do {
            System.out.println("("+s.getVar(v1)+","+s.getVar(v2)+","+s.getVar(v3)+")");
        } while (s.nextSolution());
        //5- Print the number of solution found
        System.out.println("Nb_sol : " + s.getNbSolutions());

        //totex
    }


    public void cregular3(){
        //totex cregular3
        //1- Create the model
        Model m = new CPModel();
        int n = 6;
        IntegerVariable[] vars = makeIntVarArray("v", n, 0, 5);
        //2- add the constraint
        String regexp = "(1|2)(3*)(4|5)";
        m.addConstraint(regular(regexp, vars));
        //3- Create the solver, read the model and solve it
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        do {
            for (int i = 0; i < n; i++)
                System.out.print(s.getVar(vars[i]).getVal());
            System.out.println("");
        } while (s.nextSolution());
        //4- Print the number of solution found
        System.out.println("Nb_sol : " + s.getNbSolutions());
        //totex
    }

    public void creifiedintconstraint(){
        //totex creifiedintconstraint
        CPModel m = new CPModel();
        CPSolver s = new CPSolver();
        IntegerVariable b = makeIntVar("b", 0, 1);
        IntegerVariable x = makeIntVar("x", 0, 10);
        IntegerVariable y = makeIntVar("y", 0, 10);
         // reified constraint (x<=y)
         m.addConstraint(reifiedConstraint(b, leq(x, y)));
        s.read(m);
        s.solveAll();        
        //totex
    }

    //totex ccoupletest
    public static class MyEquality extends CouplesTest {
        
        public boolean checkCouple(int x, int y) {
            return x == y;
        }
    }

    //totex
        
    public void crelationpairac(){
        //totex crelationpairac
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 1, 4);
        IntegerVariable v2 = makeIntVar("v2", 1, 4);
        IntegerVariable v3 = makeIntVar("v3", 3, 6);
        m.addConstraint(relationPairAC(CPOptions.C_EXT_AC32, v1, v2, new MyEquality()));
        m.addConstraint(relationPairAC(CPOptions.C_EXT_AC32, v2, v3, new MyEquality()));
        s.read(m);
        s.solveAll();
        //totex
    }

    //totex cnotallequal
    public static class NotAllEqual extends TuplesTest {

		public boolean checkTuple(int[] tuple) {
			for (int i = 1; i < tuple.length; i++) {
				if (tuple[i - 1] != tuple[i]) return true;
			}
			return false;
		}
	}
    //totex

    public void crelationtupleac(){
        //totex crelationtupleac
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 5);
        IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleAC(new IntegerVariable[]{x, y, z}, new NotAllEqual()));
        Solver s = new CPSolver();
		s.read(m);
		s.solveAll();
        //totex
    }


    public void crelationtuplefc(){
        //totex crelationtuplefc
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 5);
		IntegerVariable y = makeIntVar("y", 1, 5);
		IntegerVariable z = makeIntVar("z", 1, 5);
		m.addConstraint(relationTupleFC(new IntegerVariable[]{x, y, z}, new NotAllEqual()));
		Solver s = new CPSolver();
        s.read(m);
		s.solveAll();
        //totex
    }

    public void csamesign(){
        //totex csamesign
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable x = makeIntVar("x", -1, 1);
        IntegerVariable y = makeIntVar("y", -1, 1);
        IntegerVariable z = makeIntVar("z", 0, 1000);
        m.addConstraint(oppositeSign(x,y));
        m.addConstraint(eq(z, plus(mult(x, -425), mult(y, 391))));
        s.read(m);
        s.solve();
        System.out.println(s.getVar(z).getVal());
        //totex
    }

    public void csetdisjoint(){
        //totex csetdisjoint
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable x = makeSetVar("X", 1, 3);
        SetVariable y = makeSetVar("Y", 1, 3);
        SetVariable z = makeSetVar("Z", 1, 3);
        Constraint c1 = setDisjoint(x, y, z);
        m.addConstraint(c1);
        s.read(m);
        s.solveAll();
        //totex
    }

    public void csetinter(){
        //totex csetinter
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable x = makeSetVar("X", 1, 3);
        SetVariable y = makeSetVar("Y", 1, 3);
        SetVariable z = makeSetVar("Z", 2, 3);
        Constraint c1 = setInter(x, y, z);
        m.addConstraint(c1);
        s.read(m);
        s.solveAll();
        //totex
    }

    public void csetunion(){
        //totex csetunion
        Model m = new CPModel();
        Solver s = new CPSolver();
        SetVariable x = makeSetVar("X", 1, 3);
        SetVariable y = makeSetVar("Y", 3, 5);
        SetVariable z = makeSetVar("Z", 0, 6);
        Constraint c1 = setUnion(x, y, z);
        m.addConstraint(c1);
        s.read(m);
        s.solveAll();
        //totex
    }

    public void csorting(){
        //totex csorting
        CPModel m = new CPModel();
        int n = 3;
        IntegerVariable[] x = makeIntVarArray("x", n, 0, n);
        IntegerVariable[] y = makeIntVarArray("y", n, 0, n);
        m.addConstraint(sorting(x, y));
        m.addConstraint(allDifferent(x));
        CPSolver s = new CPSolver();
        s.read(m);
        s.solveAll();
        //totex
    }
    
    public void cstretchpath(){
        //totex cstretchpath
        Model m = new CPModel();
        int n = 7;
        IntegerVariable[] vars = makeIntVarArray("v", n, 0, 2);
        //define the stretches
        ArrayList<int[]> lgt = new ArrayList<int[]>();
        lgt.add(new int[]{0, 2, 2}); // stretches of value 0 are of length 2
        lgt.add(new int[]{1, 2, 3}); // stretches of value 1 are of length 2 or 3
        lgt.add(new int[]{2, 2, 2}); // stretches of value 2 are of length 2
        m.addConstraint(stretchPath(lgt, vars));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void ctimes(){
        //totex ctimes
        Model m = new CPModel();
        IntegerVariable x = makeIntVar("x", 1, 2);
        IntegerVariable y = makeIntVar("y", 3, 5);
        IntegerVariable z = makeIntVar("z", 3, 10);
        m.addConstraint(times(x, y, z));
        Solver s = new CPSolver();
        s.read(m);
        s.solve();
        //totex
    }

    public void ctree(){
        //totex ctree
        Model m = new CPModel();
        int nbNodes = 7;
        //1- create the  variables involved in the partitioning problem
        IntegerVariable  ntree = makeIntVar("ntree",1,5);
        IntegerVariable  nproper = makeIntVar("nproper",1,1);
        IntegerVariable  objective = makeIntVar("objective",1,100);
        //2- create the different graphs modeling restrictions
        List<BitSet[]> graphs = new ArrayList<BitSet[]>();
        BitSet[] succ = new BitSet[nbNodes];
        BitSet[] prec = new BitSet[nbNodes];
        BitSet[] condPrecs = new BitSet[nbNodes];
        BitSet[] inc = new BitSet[nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            succ[i] = new BitSet(nbNodes);
            prec[i] = new BitSet(nbNodes);
            condPrecs[i] = new BitSet(nbNodes);
            inc[i] = new BitSet(nbNodes);
        }
         // initial graph (encoded as successors variables)
        succ[0].set(0,true); succ[0].set(2,true); succ[0].set(4,true);
        succ[1].set(0,true); succ[1].set(1,true); succ[1].set(3,true);
        succ[2].set(0,true); succ[2].set(1,true); succ[2].set(3,true); succ[2].set(4,true);
        succ[3].set(2,true); succ[3].set(4,true); // successor of 3 is either 2 or 4
        succ[4].set(2,true); succ[4].set(3,true);
        succ[5].set(4,true); succ[5].set(5,true); succ[5].set(6,true);
        succ[6].set(3,true); succ[6].set(4,true); succ[6].set(5,true);
         // restriction on precedences
        prec[0].set(4,true); // 0 has to precede 4
        prec[4].set(3,true); prec[4].set(2,true);
        prec[6].set(4,true);
         // restriction on conditional precedences
        condPrecs[5].set(1,true); // 5 has to precede 1 if they belong to the same tree
         // restriction on incomparability:
        inc[0].set(6,true); inc[6].set(0,true); // 0 and 6 have to belong to distinct trees
        graphs.add(succ);
        graphs.add(prec);
        graphs.add(condPrecs);
        graphs.add(inc);
        //3- create the different matrix modeling restrictions
        List<int[][]> matrix = new ArrayList<int[][]>();
         // restriction on bounds on the indegree of each node
        int[][] degree = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            degree[i][0] = 0; degree[i][1] = 2; // 0 <= indegree[i] <= 2
        }
        matrix.add(degree);
         // restriction on bounds on the starting time at each node
        int[][] tw = new int[nbNodes][2];
        for (int i = 0; i < nbNodes; i++) {
            tw[i][0] = 0; tw[i][1] = 100; // 0 <= start[i] <= 100
        }
        tw[0][1] = 15;      // 0 <= start[0] <= 15
        tw[2][0] = 35; tw[2][1] = 40; // 35 <= start[2] <= 45
        tw[6][1] = 5;      // 0 <= start[6] <= 5
        matrix.add(tw);
        //4- matrix for the travel time between each pair of nodes
        int[][] travel = new int[nbNodes][nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) travel[i][j] = 100000;
        }
        travel[0][0] = 0; travel[0][2] = 10; travel[0][4] = 20;
        travel[1][0] = 20; travel[1][1] = 0; travel[1][3] = 20;
        travel[2][0] = 10; travel[2][1] = 10; travel[2][3] = 5; travel[2][4] = 5;
        travel[3][2] = 5; travel[3][4] = 2;
        travel[4][2] = 5; travel[4][3] = 2;
        travel[5][4] = 15; travel[5][5] = 0; travel[5][6] = 10;
        travel[6][3] = 5; travel[6][4] = 20; travel[6][5] = 10;
        //5- create the input structure and the tree constraint
        TreeParametersObject parameters = new TreeParametersObject(nbNodes, ntree, nproper, objective
             , graphs, matrix, travel);
        Constraint c = Choco.tree(parameters);
        m.addConstraint(c);
        Solver s = new CPSolver();
        s.read(m);
        //6- heuristic: choose successor variables as the only decision variables
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(parameters.getSuccVars())));
        s.solveAll();
        //totex
    }

    public void cxnor(){
        //totex cxnor
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 1);
        IntegerVariable v2 = makeIntVar("v2", 0, 1);
        m.addConstraint(xnor(v1,v2));
        s.read(m);
        s.solve();
        //totex
    }

    public void cxor(){
        //totex cxor
        Model m = new CPModel();
        Solver s = new CPSolver();
        IntegerVariable v1 = makeIntVar("v1", 0, 1);
        IntegerVariable v2 = makeIntVar("v2", 0, 1);
        m.addConstraint(xor(v1,v2));
        s.read(m);
        s.solve();
        //totex
    }

}