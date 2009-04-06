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
package choco.cp.solver.constraints.global.tree.deduction;


import choco.kernel.memory.trailing.StoredBitSet;

import java.util.BitSet;

public class OrderedGraphDeduction extends AbstractDeduction {

    public OrderedGraphDeduction(Object[] params) {
        super(params);
    }

    /**
     * the main method that update the precedence constraints according to the different parts of the tree constraint
     */
    public void updateOrderedGraphWithDeductions() {
        update = false;
        compatible = true;
        addFromSureGraph();
        updatePrecs();
        addPrecsFromDoms();
        addPrecsFromGraph();
        updatePrecsWithCondPrecs();
        updatePrecsWithIncs();
    }

    /**
     * add each required arc of the graph in the precedence constraint structure
     */
    private void addFromSureGraph() {
        for (int i = 0; i < nbVertices; i++) {
            if (inputGraph.getSure().getSuccessors(i).cardinality() == 1) {
                int j = inputGraph.getSure().getSuccessors(i).nextSetBit(0);
                if (i != j && !precs.getSuccessors(i).get(j)) {
                    int[] arc = {i, j};
                    if (isCompatible(arc)) {
                        if (affiche)
                            System.out.println("0- ajout de l'arc: (" + i + "," + j + ") dans Gprec");
                        addInStruct(arc);
                    }
                }
            }
        }
    }

    /**
     * add new precedence constraint according to the required paths in the graph
     */
    private void addPrecsFromGraph() {
        for (int i = 0; i < nbVertices; i++) {
            StoredBitSet prec = precs.getSuccessors(i);
            // node i is not yet fixed and cannot be instantiated to a loop on itself
            if (!inputGraph.isFixedSucc(i) && !inputGraph.getPotentialRoots().get(i)) {
                BitSet common = new BitSet(nbVertices);
                common.set(0, nbVertices, true);
                StoredBitSet maybeSucc = inputGraph.getMaybe().getSuccessors(i);
                for (int j = maybeSucc.nextSetBit(0); j >= 0; j = maybeSucc.nextSetBit(j + 1)) {
                    if (j != i) common.and(precs.getDescendants(j));
                }
                if (common.cardinality() == 1 && !prec.get(common.nextSetBit(0))) {
                    int dest = common.nextSetBit(0);
                    int[] arc = {i, dest};
                    if (isCompatible(arc)) {
                        if (i != dest) {
                            if (affiche)
                                System.out.println("1- ajout de l'arc: (" + i + "," + dest + ") dans Gp");
                            addInStruct(arc);
                        }
                    }
                }
                if (common.cardinality() > 1) {
                    for (int dest = common.nextSetBit(0); dest >= 0; dest = common.nextSetBit(dest + 1)) {
                        common.xor(precs.getDescendants(dest));
                        if (dest != i && common.cardinality() == 0 && !prec.get(dest)) {
                            int[] arc = {i, dest};
                            if (isCompatible(arc)) {
                                if (affiche)
                                    System.out.println("2- ajout de l'arc: (" + i + "," + dest + ") dans Gp");
                                addInStruct(arc);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * move the existing precedence constraints starting from a given node i to the last node involved in a path
     * of required arcs.
     *
     */
    private void updatePrecs() {
        for (int i = 0; i < nbVertices; i++) {
            StoredBitSet mandSucc = precs.getSuccessors(i);
            if (mandSucc.cardinality() > 1) {
                // the descendants of node i restricted to the required arcs: the last node reached is recorded
                BitSet iToLfi = new BitSet(nbVertices);
                iToLfi.set(i, true);
                int lfi = i;
                Boolean testLoop = true;
                while (inputGraph.isFixedSucc(lfi) && testLoop) {
                    int slfi = inputGraph.getSure().getSuccessors(lfi).nextSetBit(0);
                    if (!iToLfi.get(slfi) && slfi != lfi) {
                        lfi = slfi;
                        iToLfi.set(lfi, true);
                    } else testLoop = false;
                }
                if (lfi != i) {
                    for (int j = mandSucc.nextSetBit(0); j >= 0; j = mandSucc.nextSetBit(j + 1)) {
                        int[] arc = {lfi, j};
                        if (lfi != j && !iToLfi.get(j)) {
                            if (isCompatible(arc)) {
                                if (affiche)
                                    System.out.println("Struct[updatePred()]: (" + i + "," + j + ") ==> (" + lfi + "," + j + ")");
                                addInStruct(arc);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * update the precedence constraints according to the dominator nodes involved in the graph
     */
    private void addPrecsFromDoms() {
        BitSet[][] dominators = doms.getDominators();
        for (int i = 0; i < nbVertices; i++) {
            BitSet idesc = precs.getDescendants(i);
            for (int j = idesc.nextSetBit(0); j >= 0; j = idesc.nextSetBit(j + 1)) {
                if (j != i) {
                    for (int k = dominators[i][j].nextSetBit(0); k >= 0; k = dominators[i][j].nextSetBit(k + 1)) {
                        if (i != k && !precs.getDescendants(i).get(k)) {
                            if (affiche) System.out.println("(" + i + " -> " + k + ") -> " + j);
                            int[] arc1 = {i, k};
                            if (affiche) System.out.println("\ttry (" + i + "," + k + ") in Gp");
                            if (isCompatible(arc1)) {
                                addInStruct(arc1);
                                if (affiche)
                                    System.out.println("\t\t1- ajoute: (" + i + "," + k + ") dans Gp");
                            }
                        }
                        if (k != j && !precs.getDescendants(k).get(j)) {
                            if (affiche) System.out.println("" + i + " -> (" + k + " -> " + j + ")");
                            int[] arc2 = {k, j};
                            if (affiche) System.out.println("\ttry (" + k + "," + j + ") in Gp");
                            if (isCompatible(arc2)) {
                                addInStruct(arc2);
                                if (affiche)
                                    System.out.println("\t\t2- ajoute: (" + k + "," + j + ") dans Gp");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * update precedence constraints according to the interaction between the current precedence constraints and
     * the incomparability constraints
     */
    private void updatePrecsWithIncs() {
        for (int u = 0; u < nbVertices; u++) {
            BitSet A_u = precs.getAncestors(u);
            A_u.set(u, true);
            for (int v = precs.getSuccessors(u).nextSetBit(0); v >= 0; v = precs.getSuccessors(u).nextSetBit(v + 1)) {
                if (u != v) {
                    BitSet A_v = precs.getAncestors(v);
                    A_v.set(v, false);
                    for (int u_a = A_u.nextSetBit(0); u_a >= 0; u_a = A_u.nextSetBit(u_a + 1)) {
                        StoredBitSet inc_ua = incomp.getSuccessors(u_a);
                        for (int w = inc_ua.nextSetBit(0); w >= 0; w = inc_ua.nextSetBit(w + 1)) {
                            if (!precs.getSuccessors(w).get(v)) {
                                BitSet A_w = precs.getAncestors(w);
                                A_w.set(w, false);
                                A_w.and(A_v);
                                int[] arc = {w, v};
                                if (isCompatible(arc) && w != v) {
                                    if (A_w.cardinality() > 0) {
                                        if (affiche)
                                            System.out.println("Structure[updatePrecsWithIncs()]: (" + w + "," + v + ") dans Gp");
                                        addInStruct(arc);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if an arc, among the potentials, is compatible with the precedence constraints
     *
     * @param arc
     * @return  <code> true </code> iff the arc is compatible with the precedence constraints
     */
    private boolean isCompatible(int[] arc) {
        boolean cycle = false;
        boolean transitive = false;
        int src = arc[0];
        int dest = arc[1];
        if (src == dest) {
            compatible = false;
            if (affiche)
                System.out.println("\t\t1- (" + src + "," + dest + ") incompatible dans Gp");
            return false;
        }
        // A first dfs that checks the arc to add does not create a circuit in the precedence graph
        BitSet desc_1 = precs.getDescendants(dest);
        if (desc_1.get(src)) cycle = true;
        if (!cycle) {
            // A second dfs that checks the arc to add is not a transitive arc in the precedence graph
            BitSet desc_2 = precs.getDescendants(src);
            if (desc_2.get(dest) && !precs.getSuccessors(src).get(dest)) {
                if (affiche)
                    System.out.println("\t\t(" + src + "," + dest + ") incompatible dans Gp => transitif");
                transitive = true;
            }
            return !transitive;
        } else {
            if (affiche) {
                System.out.println("\t\t----------------------------");
                System.out.println("\t\tprec(" + src + "," + dest + ") => cycle");
                precs.showAllDesc();
                System.out.println("\t\tD_" + dest + "(Gp) = " + desc_1.toString());
                System.out.println("\t\t(" + src + "," + dest + ") incompatible dans Gp => cycle");
                System.out.println("\t\t----------------------------");
            }
            compatible = false;
            return false;
        }
    }

    /**
     *
     * @param arc   An arc which is added in the structures related to the precedence constraints
     */
    private void addInStruct(int[] arc) {
        int src = arc[0];
        int dest = arc[1];
        update = precs.addPrec(src,dest);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////// Partie relative au maintient du pickUp ////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * update the precedence graph according to the current graph and the conditionnal precedence constraints
     */
    private void updatePrecsWithCondPrecs() {
        for (int src = 0; src < nbVertices; src++) {
            StoredBitSet prec = precs.getSuccessors(src);
            StoredBitSet pickup = condPrecs.getSuccessors(src);
            // if (src,k) belongs to the precedences then, (src,dest) is added in the precedence constraints
            if (prec.cardinality() > 0) {
                for (int dest = pickup.nextSetBit(0); dest >= 0; dest = pickup.nextSetBit(dest + 1)) {
                    int[] arc = {src, dest};
                    if (isCompatible(arc)) {
                        addInStruct(arc);
                        if (affiche)
                            System.out.println("2- Pickup: ajout de l'arc (" + src + "," + dest + ") dans Gp");
                        pickup.set(dest, false);
                    }
                }
            }
            // if src node is fixed to node dest and src != dest then, for any (src,k) in the conditional precedences
            // we add this arc in the precedence constraints
            if (inputGraph.isFixedSucc(src)) {
                int dest = inputGraph.getFixedSucc(src);
                if (src != dest && pickup.cardinality() > 0) {
                    for (int succ = pickup.nextSetBit(0); succ >= 0; succ = pickup.nextSetBit(succ + 1)) {
                        if (!prec.get(succ)) {
                            int[] arc = {src, succ};
                            if (isCompatible(arc)) {
                                addInStruct(arc);
                                if (affiche)
                                    System.out.println("4- Pickup: ajout de l'arc (" + src + "," + succ + ") dans Gp");
                                pickup.set(succ, false);
                            }
                        }
                    }
                }
            }
            // if node src is not yet fixed, is not a potential root and is the origin of a conditional precedence
            // constraint then (src,dest) is added in the precedence constraints 
            if (!inputGraph.isFixedSucc(src) && !inputGraph.getPotentialRoots().get(src)) {
                for (int dest = pickup.nextSetBit(0); dest >= 0; dest = pickup.nextSetBit(dest + 1)) {
                    int[] arc = {src, dest};
                    if (isCompatible(arc)) {
                        addInStruct(arc);
                        if (affiche)
                            System.out.println("6- Pickup: ajout de l'arc (" + src + "," + dest + ") dans Gp");
                        pickup.set(dest, false);
                    }
                }
            }
        }
    }
}
