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
package choco.cp.solver.constraints.global.tree.filtering.structuralFiltering;


import choco.cp.solver.constraints.global.tree.filtering.AbstractPropagator;
import choco.kernel.common.util.IntIterator;
import choco.kernel.memory.trailing.StoredBitSet;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.Vector;


public class Nproper extends AbstractPropagator {

    /**
     * current lower bound on the number of proper trees
     */
    protected int lowerBd;

    /**
     * current upper bound on the number of proper trees
     */
    protected int upperBd;

    /**
     * the set of nodes that belong to the required graph and that reach a potential root by a path of required arcs
     */
    protected StoredBitSet tmp;

    public Nproper(Object[] params) {
        super(params);
        this.lowerBd = getLowerBound();
        StoredBitSet potentialRoots = inputGraph.getPotentialRoots();
        this.upperBd = getUpperBound(potentialRoots);
        this.tmp = new StoredBitSet(solver.getEnvironment(),nbVertices);
    }

    public String getTypePropag() {
        return "Nproper progagation";
    }

    public boolean feasibility() throws ContradictionException {
        // update the upper bound of nproper according to ntree
        if (tree.getNtree().getSup() < tree.getNproper().getSup()) {
            if (affiche) System.out.println("1-NProper: updateSup nProper = " + tree.getNproper().getSup() + " ==> " + tree.getNtree().getSup());
            propagateStruct.setMaxNProper(tree.getNtree().getSup());
        }
        // update the lower bound of nproper according to ntree
        if (tree.getNtree().getInf() < tree.getNproper().getInf()) {
            if (affiche) System.out.println("1-NProper: updateInf nProper = " + tree.getNproper().getInf() + " ==> " + tree.getNtree().getInf());
            propagateStruct.setMinNProper(tree.getNtree().getInf());
        }
        // update the lower bound of nproper according to the evaluated lower bound
        lowerBd = getLowerBound();
        if (lowerBd > tree.getNproper().getInf()) {
            if (affiche) System.out.println("2-NProper: updateInf nProper = " + tree.getNproper().getInf() + " ==> " + lowerBd);
            propagateStruct.setMinNProper(lowerBd);
        }

        // update the upper bound of nproper according to the evaluated upper bound
        StoredBitSet potentialRoots = inputGraph.getPotentialRoots();
        upperBd = getUpperBound(potentialRoots);
        if (upperBd < tree.getNproper().getSup()) {
            if (affiche) System.out.println("2-NProper: updateSup nProper = " + tree.getNproper().getSup() + " ==> " + upperBd);
            propagateStruct.setMaxNProper(upperBd);
        }
        Vector<StoredBitSet> setcc = inputGraph.getSure().getSetCC();
        int nprop_build = 0;
        for (StoredBitSet aSetcc : setcc) {
            if (aSetcc.cardinality() > 1) nprop_build++;
        }
        if (nprop_build < lowerBd) {
            if (affiche) System.out.println("nprop_build = " + nprop_build + " VS nProper.getSup = " + lowerBd);
            return false;
        } else {
            return true;
        }
    }

    /**
     * <p> the filtering rule is decomposed in two steps: </p>
     *
     * <blockquote> - max(nproper) <= lowerBd: enforce the potential root of each node which does not belong
     * to <code> tmp </code>. </blockquote>
     * <blockquote> - min(nProper) >= upperBd: enforce the potential root of each isolated node. </blockquote>
     */
    public void filter() throws ContradictionException {
        // filtering if max(nProper) <= lowerBd
        if (tree.getNproper().getSup() <= lowerBd) {
            tmp.clear();
            // connect component (cc) of size at least 2 in the required graph
            Vector<StoredBitSet> ccOfGt = inputGraph.getSure().getSetCC();
            for (StoredBitSet aCcOfGt : ccOfGt) {
                if (aCcOfGt.cardinality() >= 2) {
                    for (int j = aCcOfGt.nextSetBit(0); j >= 0; j = aCcOfGt.nextSetBit(j + 1)) tmp.set(j, true);
                }
            }
            // connect component (cc) of size at least 2 in the precedence graph
            Vector<StoredBitSet> ccOfGp = precs.getPrecs().getSetCC();
            for (StoredBitSet aCcOfGp : ccOfGp) {
                if (aCcOfGp.cardinality() >= 2) {
                    for (int j = aCcOfGp.nextSetBit(0); j >= 0; j = aCcOfGp.nextSetBit(j + 1)) tmp.set(j, true);
                }
            }
            // tmp contains the set of nodes that belong to the required graph and that reach a
            // potential root by a path of required arcs
            if (tmp.cardinality() >= 2) {
                // for each node which is not in tmp and which has no successors in tmp,
                // we want to enforce the potential root
                for (int i = 0; i < nbVertices; i++) {
                    IntDomainVar var = nodes[i].getSuccessors();
                    boolean reachable = false;
                    for (int j = tmp.nextSetBit(0); j >= 0; j = tmp.nextSetBit(j + 1)) {
                        if (inputGraph.getMaybe().getSuccessors(i).get(j)) reachable = true;
                    }
                    if (!tmp.get(i) && !reachable) {
                        IntIterator it = var.getDomain().getIterator();
                        while (it.hasNext()) {
                            int j = it.next();
                            if (j != i && var.canBeInstantiatedTo(j)) {
                                if (affiche) System.out.println("1-1 NProper: suppression arc (" + i + "," + j + ")");
                                int[] arc = {i, j};
                                propagateStruct.addRemoval(arc);
                            }
                        }
                        // no node reach the node i
                        for (int j = 0; j < nbVertices; j++) {
                            if (j != i && nodes[j].getSuccessors().canBeInstantiatedTo(i)) {
                                if (affiche) System.out.println("1-2 NProper: suppression arc (" + j + "," + i + ")");
                                int[] arc = {j, i};
                                propagateStruct.addRemoval(arc);
                            }
                        }
                    }
                }
            }
        }
        StoredBitSet potentialRoots = inputGraph.getPotentialRoots();
        // filtering if min(nProper) >= upperBd
        if (tree.getNproper().getInf() >= upperBd) {
            // node i is isolated and has a loop on itself
            for (int i = potentialRoots.nextSetBit(0); i >= 0; i = potentialRoots.nextSetBit(i + 1)) {
                // enforce the loop for node i
                IntDomainVar var = nodes[i].getSuccessors();
                IntIterator it = var.getDomain().getIterator();
                while (it.hasNext()) {
                    int j = it.next();
                    if (j != i && var.canBeInstantiatedTo(j)) {
                        if (affiche) System.out.println("2- NProper: suppression arc (" + i + "," + j + ")");
                        int[] arc = {i, j};
                        propagateStruct.addRemoval(arc);
                    }
                }
            }
        }
    }

    private int getUpperBound(StoredBitSet potentialRoots) {
        // compute #potentialRoots
        int nbPotRoots = potentialRoots.cardinality();
        // compute #isolatedVertexWithLoops
        int nbIsolatedRoots = 0;
        for (int i = 0; i < nbVertices; i++) {
            int nbReached = 0;
            for (int j = 0; j < nbVertices; j++) {
                if (nodes[j].getSuccessors().canBeInstantiatedTo(i) && i != j) nbReached++;
            }
            if (nbReached == 0 && nodes[i].getSuccessors().isInstantiatedTo(i)) nbIsolatedRoots++;
        }
        // compute #potentialRoots - #isolatedVertexWithRoots
        return nbPotRoots - nbIsolatedRoots;
    }

    private int getLowerBound() {
        // compute the number of cc of the required graph with at least two nodes and a potential root
        int val = 0;
        Vector<StoredBitSet> ccOfGt = inputGraph.getSure().getSetCC();
        for (StoredBitSet aCcOfGt : ccOfGt) {
            // a connected compenant of size at least 2
            if (aCcOfGt.cardinality() >= 2) {
                boolean root = false;
                // is there any potential root in such the current cc ?
                for (int j = aCcOfGt.nextSetBit(0); j >= 0; j = aCcOfGt.nextSetBit(j + 1)) {
                    if (nodes[j].getSuccessors().isInstantiatedTo(j)) root = true;
                }
                if (root) val++;
            }
        }
        return val;
    }
}
