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
package choco.cp.model.managers.constraints.global;

import choco.cp.solver.CPSolver;
import choco.cp.solver.SettingType;
import choco.cp.solver.constraints.global.tree.TreeSConstraint;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.Node;
import choco.cp.solver.constraints.global.tree.structure.inputStructure.TreeParameters;
import choco.cp.solver.constraints.reified.leaves.ConstraintLeaf;
import choco.kernel.model.ModelException;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintManager;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.tree.TreeNodeObject;
import choco.kernel.model.variables.tree.TreeParametersObject;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.reified.INode;

import java.util.List;
import java.util.Set;

/**
 * User:    charles
 * Date:    26 août 2008
 */
public final class TreeManager extends ConstraintManager<Variable> {

    /**
     * Build a constraint for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters : a "hook" to attach any kind of parameters to constraints
     * @param options
     * @return
     */
    public SConstraint makeConstraint(Solver solver, Variable[] variables, Object parameters, List<String> options) {
        if(solver instanceof CPSolver){

            if(parameters instanceof TreeParametersObject){
                TreeParametersObject tpo = (TreeParametersObject)parameters;
                int nbNodes = tpo.getNbNodes();
                TreeNodeObject[] tnodes = tpo.getNodes();
                Node[] nodes = new Node[tnodes.length];
                TreeParameters params = null;
                for(int i =0; i < tnodes.length; i++){
                    TreeNodeObject tn = tnodes[i];
                    nodes[i] = new Node(solver, nbNodes, tn.getIdx(), solver.getVar(tn.getSuccessors()),
                            solver.getVar(tn.getInDegree()), solver.getVar(tn.getTimeWindow()), tpo.getGraphs());
                }

                params  = new TreeParameters(solver, nbNodes, solver.getVar(tpo.getNTree()),
                        solver.getVar(tpo.getNproper()), solver.getVar(tpo.getObjective()), nodes, tpo.getTravel());
                return new TreeSConstraint(params.getAllVars(),params);
            }
        }
        throw new ModelException("Could not found a constraint manager in " + this.getClass() + " !");
    }

    /**
     * @param options : the set of options on the constraint (Typically the level of consistency)
     * @return a list of domains accepted by the constraint and sorted
     *         by order of preference
     */
    public int[] getFavoriteDomains(List<String> options) {
        return getACFavoriteIntDomains();
    }


    public static void readSetting(Set<String> source, Set<SettingType> dest, SettingType setting) {
		if(source.contains(setting.getOptionName()) ) {dest.add(setting);}
	}

    /**
     * Build arithm node from a IntegerExpressionVariable
     *
     * @param solver
     * @param cstrs  constraints (can be null)
     * @param vars   variables
     * @return
     */
    public INode makeNode(Solver solver, Constraint[] cstrs, Variable[] vars) {
        return new ConstraintLeaf(((CPSolver)solver).makeSConstraint(cstrs[0]), null);
    }

    /**
     * Build a constraint and its opposite for the given solver and "model variables"
     *
     * @param solver
     * @param variables
     * @param parameters
     * @param options
     * @return array of 2 SConstraint object, the constraint and its opposite
     */
    @Override
    public SConstraint[] makeConstraintAndOpposite(Solver solver, Variable[] variables, Object parameters, List<String> options) {
        throw new UnsupportedOperationException();
    }

}
