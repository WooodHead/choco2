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

package choco.kernel.solver.constraints.global.automata.fast_multicostregular.structure;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.global.automata.common.StoredIndexedBipartiteSetWithOffset;
import choco.kernel.solver.constraints.global.automata.fast_multicostregular.algo.SoftPathFinder;
import choco.kernel.solver.constraints.integer.AbstractIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntStack;
import org.jgrapht.graph.DirectedMultigraph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 28, 2010
 * Time: 3:17:28 PM
 */
public class SoftStoredMultiValuedDirectedMultiGraph
{

public AbstractIntSConstraint constraint;


int[] starts;
public int[] offsets;

public int sourceIndex;
public int tinIndex;
public int nbR;



StoredIndexedBipartiteSetWithOffset[] supports;
public StoredIndexedBipartiteSetWithOffset[] layers;
public SoftPathFinder pf;
public BitSet inStack;
private IntDomainVar[] y;


public class Nodes
{
        public int[] states;
        public int[] layers;
        public StoredIndexedBipartiteSetWithOffset[] outArcs;
        public StoredIndexedBipartiteSetWithOffset[] inArcs;

        public int[] nextSP;
        public int[] prevSP;
        public int[] nextLP;
        public int[] prevLP;

        public double[] spfs;
        public double[] spft;
        public double[] lpfs;
        public double[] lpft;

        public int[][] nextSPI;
        public int[][] prevSPI;
        public int[][] nextLPI;
        public int[][] prevLPI;

        public double[][] spfsI;
        public double[][] spftI;
        public double[][] lpfsI;
        public double[][] lpftI;

}


public class Arcs
{
        public int[] values;
        public int[] dests;
        public int[] origs;
        public double[][] originalCost;
        public double[] temporaryCost;


}



public Nodes GNodes;
public Arcs GArcs;






public SoftStoredMultiValuedDirectedMultiGraph(IEnvironment environment, AbstractIntSConstraint constraint, DirectedMultigraph<Node, Arc> graph, int[][] layers, int[] starts, int[] offsets, int supportLength,int[][][][] costs,IntDomainVar[] y)
{
        this.nbR = costs[0][0].length;
        this.y = y;
        this.constraint = constraint;
        this.starts = starts;
        this.offsets =offsets;
        this.layers = new StoredIndexedBipartiteSetWithOffset[layers.length];
        for (int i = 0 ; i < layers.length ; i++)
        {
                this.layers[i] = new StoredIndexedBipartiteSetWithOffset(environment,layers[i]);
        }
        this.sourceIndex = layers[0][0];
        this.tinIndex = layers[layers.length-1][0];

        this.GNodes = new Nodes();
        this.GArcs = new Arcs();

        TIntHashSet[] sups = new TIntHashSet[supportLength];
        this.supports = new StoredIndexedBipartiteSetWithOffset[supportLength];


        Set<Arc> arcs = graph.edgeSet();

        this.inStack = new BitSet(arcs.size());//environment.makeBitSet(arcs.size());

        GArcs.values = new int[arcs.size()];
        GArcs.dests = new int[arcs.size()];
        GArcs.origs = new int[arcs.size()];
        GArcs.originalCost = new double[arcs.size()][nbR];
        GArcs.temporaryCost = new double[arcs.size()];



        for (Arc a : arcs)
        {

                GArcs.values[a.id] = a.value;
                GArcs.dests[a.id] = a.dest.id;
                GArcs.origs[a.id] = a.orig.id;
                int state = a.orig.state;
                int layer = a.orig.layer;
                for (int r = 0 ; r < nbR ; r++)
                {
                        GArcs.originalCost[a.id][r] = layer < layers.length -2 ?costs[layer][a.value][r][state]:0.0;
                }



                if (a.orig.layer < starts.length)
                {
                        int idx = starts[a.orig.layer]+a.value-offsets[a.orig.layer];
                        if (sups[idx] == null)
                                sups[idx] = new TIntHashSet();
                        sups[idx].add(a.id);
                }

        }

        for (int i =0 ;i < sups.length ;i++)
        {
                if (sups[i] != null)
                        supports[i] = new StoredIndexedBipartiteSetWithOffset(environment,sups[i].toArray());
        }

        Set<Node> nodes = graph.vertexSet();


        System.err.println("NB ARCS IN LAYERED GRAPH : "+arcs.size());
        System.err.println("NB NODES IN LAYERED GRAPH : "+nodes.size());

        GNodes.outArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.inArcs = new StoredIndexedBipartiteSetWithOffset[nodes.size()];
        GNodes.layers = new int[nodes.size()];
        GNodes.states = new int[nodes.size()];

        GNodes.prevLP = new int[nodes.size()];
        Arrays.fill(GNodes.prevLP,Integer.MIN_VALUE);
        GNodes.nextLP = new int[nodes.size()];
        Arrays.fill(GNodes.nextLP,Integer.MIN_VALUE);
        GNodes.prevSP = new int[nodes.size()];
        Arrays.fill(GNodes.prevSP,Integer.MIN_VALUE);
        GNodes.nextSP = new int[nodes.size()];
        Arrays.fill(GNodes.nextSP,Integer.MIN_VALUE);


        GNodes.lpfs = new double[nodes.size()];
        GNodes.lpft = new double[nodes.size()];
        GNodes.spfs = new double[nodes.size()];
        GNodes.spft = new double[nodes.size()];


        GNodes.lpfsI = new double[nodes.size()][nbR];
        GNodes.lpftI = new double[nodes.size()][nbR];
        GNodes.spfsI = new double[nodes.size()][nbR];
        GNodes.spftI = new double[nodes.size()][nbR];

        GNodes.prevLPI = new int[nodes.size()][nbR];
        GNodes.nextLPI = new int[nodes.size()][nbR];
        GNodes.prevSPI = new int[nodes.size()][nbR];
        GNodes.nextSPI = new int[nodes.size()][nbR];
        for (int k = 0 ;k < nbR ; k++)
        {
                Arrays.fill(GNodes.prevLPI[k],Integer.MIN_VALUE);
                Arrays.fill(GNodes.nextLPI[k],Integer.MIN_VALUE);
                Arrays.fill(GNodes.prevSPI[k],Integer.MIN_VALUE);
                Arrays.fill(GNodes.nextSPI[k],Integer.MIN_VALUE);
        }




        for (Node n : nodes)
        {
                GNodes.layers[n.id] = n.layer;
                GNodes.states[n.id] = n.state;
                int i;
                Set<Arc> outarc = graph.outgoingEdgesOf(n);
                if (!outarc.isEmpty())
                {
                        int[] out = new int[outarc.size()];
                        i = 0;
                        for (Arc a : outarc)
                        {
                                out[i++] = a.id;
                        }
                        GNodes.outArcs[n.id] = new StoredIndexedBipartiteSetWithOffset(environment,out);
                }

                Set<Arc> inarc = graph.incomingEdgesOf(n);
                if (!inarc.isEmpty())
                {
                        int[] in = new int[inarc.size()];
                        i = 0;
                        for (Arc a : inarc)
                        {
                                in[i++] = a.id;
                        }
                        GNodes.inArcs[n.id] = new StoredIndexedBipartiteSetWithOffset(environment,in);
                }
        }




}

public final StoredIndexedBipartiteSetWithOffset getSupport(int i, int j)
{
        int idx = starts[i]+j-offsets[i];
        return supports[idx];
}


public void delayedBoundUpdate(TIntStack toRemove, IntDomainVar[] z, int... dim)
{
        for (int i = 0 ; i < offsets.length ; i++)
        {
                DisposableIntIterator iter = this.layers[i].getIterator();
                while (iter.hasNext())
                {
                        int n = iter.next();
                        DisposableIntIterator it = this.GNodes.outArcs[n].getIterator();
                        while(it.hasNext())
                        {
                                int arc = it.next();
                                int val = this.GArcs.values[arc];
                                int orig = this.GArcs.origs[arc];
                                int dest = this.GArcs.dests[arc];
                                for (int k : dim)
                                {
                                        if (GNodes.spfsI[orig][k] + GArcs.originalCost[arc][k] + GNodes.spftI[dest][k] > z[k].getSup() ||
                                             GNodes.lpfsI[orig][k] + GArcs.originalCost[arc][k] + GNodes.lpftI[dest][k] < z[k].getInf())
                                        {
                                                if (!isInStack(arc))
                                                {
                                                        setInStack(arc);
                                                        toRemove.push(arc);
                                                }
                                        }
                                }
                        }
                        it.dispose();
                }
                iter.dispose();
        }
}


public boolean removeArc(int arcId, TIntStack toRemove, TIntStack[] updateLeft, TIntStack[] updateRight) throws ContradictionException
{
        inStack.clear(arcId);
        boolean needUpdate = false;

        int orig = GArcs.origs[arcId];
        int dest = GArcs.dests[arcId];

        int layer = GNodes.layers[orig];
        int value = GArcs.values[arcId];

        if (layer < starts.length)
        {
                StoredIndexedBipartiteSetWithOffset support = getSupport(layer,value);
                support.remove(arcId);

                if (support.isEmpty())
                {
                        IntDomainVar var = this.constraint.getVar(layer);
                        var.removeVal(value, this.constraint, false);
                }
        }

        int[] list;
        int size;
        StoredIndexedBipartiteSetWithOffset out = GNodes.outArcs[orig];
        StoredIndexedBipartiteSetWithOffset in;

        out.remove(arcId);


        if (out.isEmpty())
        {    layers[layer].remove(orig);
                if (layer > 0 )
                {
                        in = GNodes.inArcs[orig];
                        list = in._getStructure();
                        size = in.size();
                        for (int i = 0 ; i < size ; i++)//while(it.hasNext())
                        {
                                int id = list[i];//it.next();
                                if(!isInStack(id))
                                {
                                        setInStack(id);
                                        toRemove.push(id);
                                }
                        }
                        //it.dispose();
                }
        }
        else
        {
                for (int k = 0 ; k < nbR ; k++)
                {
                        //   System.out.println(GNodes.nextSPI[orig][k] + " " + arcId);
                        if (GNodes.nextSPI[orig][k] ==  arcId || GNodes.nextLPI[orig][k] == arcId)
                        {
                                updateRight[k].push(orig);
                                needUpdate = true;
                                //  updateRight(orig,toRemove,k,modBound);
                        }
                }
        }


        in = GNodes.inArcs[dest];
        in.remove(arcId);


        if (in.isEmpty())
        {
                layers[layer+1].remove(dest);
                if (layer+1 < starts.length)
                {
                        out = GNodes.outArcs[dest];
                        list = out._getStructure();
                        size = out.size();
                        for (int i = 0 ; i < size ; i++)//while(it.hasNext())
                        {
                                int id = list[i];//it.next();
                                if (!isInStack(id))
                                {
                                        setInStack(id);
                                        toRemove.push(id);
                                }
                        }
                }
        }
        else
        {
                for (int k = 0 ; k < nbR ; k++)
                {
                        if (GNodes.prevSPI[dest][k] == arcId || GNodes.prevLPI[dest][k] == arcId)
                        {
                                updateLeft[k].push(dest);
                                needUpdate = true;
                        }
                }
        }
        return needUpdate;

}


public boolean updateRight(TIntStack updateRight, TIntStack toRemove, int dim, AbstractIntSConstraint table) throws ContradictionException
{

        boolean mod = false;
        int nid = updateRight.pop();
        double tempPval = Double.POSITIVE_INFINITY;
        double tempPval2 = Double.NEGATIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;
        int temp2 = Integer.MIN_VALUE;

        int[] list = GNodes.outArcs[nid]._getStructure();
        int size = GNodes.outArcs[nid].size();


        for (int i = 0 ; i < size ; i++)// while(it.hasNext())
        {
                int arcId = list[i];//it.next();
                int dest = GArcs.dests[arcId];
                double spft = GNodes.spftI[dest][dim] + GArcs.originalCost[arcId][dim];
                if (tempPval > spft)
                {
                        tempPval = spft;
                        tempP = arcId;
                }

                double lpft = GNodes.lpftI[dest][dim] + GArcs.originalCost[arcId][dim];
                if (tempPval2 < lpft)
                {
                        tempPval2 = lpft;
                        temp2 = arcId;
                }


        }
        //it.dispose();
        double old = GNodes.spftI[nid][dim];
        GNodes.spftI[nid][dim] = tempPval ;
        GNodes.nextSPI[nid][dim] = tempP;

        double old2 = GNodes.lpftI[nid][dim];
        GNodes.lpftI[nid][dim] = tempPval2;
        GNodes.nextLPI[nid][dim] = temp2;

        if (nid == sourceIndex)
        {
                if (y[dim].updateInf((int)Math.ceil(tempPval),constraint,false))
                {
                        mod = true;
                        table.awakeOnInf(0);
                }
                if (y[dim].updateSup((int)Math.floor(tempPval2),constraint,false))
                {
                        mod = true;
                        table.awakeOnSup(0);
                }
        }

        if (nid != sourceIndex && (old != tempPval || old2 != tempPval2))
        {
                list = GNodes.inArcs[nid]._getStructure();
                size = GNodes.inArcs[nid].size();

                for (int i = 0 ; i < size ; i++) //while(it.hasNext())
                {
                        int arcId = list[i];//it.next();
                        int orig =  GArcs.origs[arcId];
                        if ((GNodes.nextSPI[orig][dim] == arcId &&old!=tempPval) || (old2!=tempPval2 && GNodes.nextLPI[orig][dim]== arcId))
                        {
                                updateRight.push(orig);
                                //updateRight(orig,toRemove,dim,modBound);
                        }
                        double spfs = GNodes.spfsI[orig][dim];//.quickGet(orig);
                        double lpfs = GNodes.lpfsI[orig][dim];

                        double acost = GArcs.originalCost[arcId][dim];
                        if (!isInStack(arcId) && (tempPval + spfs+acost > y[dim].getSup()
                                || tempPval2 + lpfs+acost < y[dim].getInf()))
                        {
                                setInStack(arcId);
                                toRemove.push(arcId);
                        }
                }
                //   it.dispose();
        }

        return mod;

}


public boolean updateLeft(TIntStack updateLeft, TIntStack toRemove, int dim, AbstractIntSConstraint table) throws ContradictionException
{
        boolean mod = false;
        int nid = updateLeft.pop();
        double tempPval = Double.POSITIVE_INFINITY;
        int tempP = Integer.MIN_VALUE;

        double tempPval2 = Double.NEGATIVE_INFINITY;
        int tempP2 = Integer.MIN_VALUE;

        int[] list = GNodes.inArcs[nid]._getStructure();
        int size = GNodes.inArcs[nid].size();


        for (int i = 0 ; i < size ; i++) //while(it.hasNext())
        {
                int arcId = list[i];//it.next();
                int orig = GArcs.origs[arcId];
                double spfs = GNodes.spfsI[orig][dim] + GArcs.originalCost[arcId][dim];
                if (tempPval > spfs)
                {
                        tempPval = spfs;
                        tempP = arcId;
                }
                double lpfs = GNodes.lpfsI[orig][dim] + GArcs.originalCost[arcId][dim];
                if (tempPval2 < lpfs)
                {
                        tempPval2 = lpfs;
                        tempP2 = arcId;
                }

        }

        //it.dispose();
        double old = GNodes.spfsI[nid][dim] ;
        GNodes.spfsI[nid][dim] = tempPval;
        GNodes.prevSPI[nid][dim] = tempP;
        double old2 = GNodes.lpfsI[nid][dim];
        GNodes.lpfsI[nid][dim] = tempPval2;
        GNodes.prevLPI[nid][dim] = tempP2;

        if (nid == tinIndex)
        {

                        if (y[dim].updateInf((int)Math.ceil(tempPval),constraint,false))
                        {
                                mod = true;
                                table.awakeOnInf(0);
                        }
                        if (y[dim].updateSup((int)Math.floor(tempPval2),constraint,false))
                        {
                                mod = true;
                                table.awakeOnSup(0);
                        }


        }

        if (nid != tinIndex && (old != tempPval || old2 != tempPval2))
        {
                list = GNodes.outArcs[nid]._getStructure();
                size = GNodes.outArcs[nid].size();

                for (int i = 0 ; i < size ; i++)//while(it.hasNext())
                {
                        int arcId = list[i];//it.next();
                        int dest =  GArcs.dests[arcId];
                        if ((old != tempPval && GNodes.prevSPI[dest][dim] == arcId) || (old2!=tempPval2 && GNodes.prevLPI[dest][dim] == arcId))
                        {
                                // updateLeft(dest,toRemove,dim,modBound);
                                updateLeft.push(dest);
                        }
                        double spft = GNodes.spftI[dest][dim];
                        double acost = GArcs.originalCost[arcId][dim];
                        double lpft = GNodes.lpftI[dest][dim];
                        if (!isInStack(arcId) && (tempPval + spft+acost > y[dim].getSup()
                                ||tempPval2 + lpft+acost < y[dim].getInf()))
                        {
                                setInStack(arcId);
                                toRemove.push(arcId);
                        }
                }
                //it.dispose();
        }

        return mod;

}




/**
 * Getter to the is arc in to be removed stack bitSet
 * @return an instance of a storable bitset
 */
public final BitSet getInStack()
{
        return inStack;
}

/**
 *  Getter, the idx th bit of the inStack bitSet
 * @param idx the index of the arc
 * @return true if a given arc is to be deleted
 */
public final boolean isInStack(int idx)
{
        return inStack.get(idx);
}

/**
 * Set the idx th bit of the to be removed bitset
 * @param idx the index of the bit
 */
public final void setInStack(int idx)
{
        inStack.set(idx);
}

/**
 * Clear the idx th bit of the to be removed bitset
 * @param idx the index of the bit
 */
public final void clearInStack(int idx)
{
        inStack.clear(idx);
}

public int getRegret(int layer, int value, int... resources)
{
        int result  = Integer.MAX_VALUE;
        StoredIndexedBipartiteSetWithOffset arcs = this.getSupport(layer,value);
        DisposableIntIterator it = arcs.getIterator();

        while (it.hasNext())
        {
                int arcId = it.next();
                int origId = GArcs.origs[arcId];
                int destId = GArcs.dests[arcId];
                int cost = 0;
                for (int r : resources)
                {
                        cost+=pf.spfs[origId][r] + GArcs.originalCost[arcId][r] + pf.spft[destId][r];
                }
                if (cost < result)
                        result = cost;



        }
        it.dispose();
        for (int r : resources)
        {
                result -= pf.spft[sourceIndex][r];
        }
        if (result < 0)
                ;//   System.err.println("STR");

        return result;
}

public int getMinPathCostForAssignment(int layer, int value, int... resources)
{
        int result  = Integer.MAX_VALUE;
        StoredIndexedBipartiteSetWithOffset arcs = this.getSupport(layer,value);

        DisposableIntIterator it = arcs.getIterator();
        int[] list = arcs._getStructure();
        int size = arcs.size();
        for (int i = 0 ; i < size ; i ++) {//while (it.hasNext()) {
                int arcId = list[i];//it.next();
                int origId = GArcs.origs[arcId];
                int destId = GArcs.dests[arcId];
                int cost = 0;
                for (int r : resources) {
                        cost+=pf.spfs[origId][r] + GArcs.originalCost[arcId][r] + pf.spft[destId][r];
                }
                if (cost < result)
                        result = cost;
        }
        it.dispose();
        return result;
}

private int[] minmax = new int[2];
public int[] getMinMaxPathCostForAssignment(int layer, int value, int... resources)
{
        minmax[0] = Integer.MAX_VALUE;
        minmax[1] = Integer.MIN_VALUE;
        StoredIndexedBipartiteSetWithOffset arcs = this.getSupport(layer,value);
        DisposableIntIterator it = arcs.getIterator();

        while (it.hasNext()) {
                int arcId = it.next();
                int origId = GArcs.origs[arcId];
                int destId = GArcs.dests[arcId];
                int cost = 0;
                for (int r : resources) {
                        cost+=pf.spfs[origId][r] + GArcs.originalCost[arcId][r] + pf.spft[destId][r];
                }
                if (cost < minmax[0])
                        minmax[0] = cost;
                if (cost > minmax[1])
                        minmax[1] = cost;
        }
        it.dispose();
        return minmax;
}

public int getMinPathCost(int... resources)
{
        int result  = 0;
        for (int r : resources) {
                result += pf.spft[sourceIndex][r];
        }
        return result;
}

public double[] getInstantiatedLayerCosts(int layer)
{
        StoredIndexedBipartiteSetWithOffset couche = layers[layer];
        DisposableIntIterator it = couche.getIterator();
        int node = it.next();
        it.dispose();
        it = GNodes.outArcs[node].getIterator();
        int arcId = it.next();
        it.dispose();
        return GArcs.originalCost[arcId];
}


}
