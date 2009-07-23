package i_want_to_use_this_old_version_of_choco.global;

import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.integer.IntDomainVar;
import i_want_to_use_this_old_version_of_choco.integer.constraints.AbstractLargeIntConstraint;
import i_want_to_use_this_old_version_of_choco.integer.var.IntDomain;
import i_want_to_use_this_old_version_of_choco.util.IntIterator;
import i_want_to_use_this_old_version_of_choco.util.IntList;

import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Enforce the number of identical values wihtin a list of variables to
 * be at most a given variable.
 * Created by IntelliJ IDEA.
 * User: hcambaza
 * Date: 08-Jan-2007
 * Time: 09:13:13
 */
public class AtMostNValue extends AbstractLargeIntConstraint {

    protected boolean debug = false;
    // temporary data structure
    // The set of instantiated values
    protected BitSet gval;

    // number of ground variables : |gVal|
    protected int nGval;

    // list of non instantiated variables
    protected IntList freeVars;
    // list of variables defining the nodes of the intersection graph
    protected IntList dVar;
    // Intersection graph encoded by the neigbhoors of each variable
    protected BitSet[] ngraph;
    protected int[] ngraphSize;

    protected int maxDSize;

    protected int nVars;

    public static IntDomainVar[] makeVarTable(IntDomainVar[] vars, IntDomainVar Nvalue) {
        IntDomainVar[] vs = new IntDomainVar[vars.length + 1];
        System.arraycopy(vars, 0, vs, 0, vars.length);
        vs[vars.length] = Nvalue;
        return vs;
    }

    public AtMostNValue(IntDomainVar[] vars, IntDomainVar Nvalue) {
        super(makeVarTable(vars, Nvalue));
        this.cste = Integer.MAX_VALUE;
        this.maxDSize = 0;
        for (IntDomainVar v : vars) {
            if (cste > v.getInf()) cste = v.getInf();
            if (maxDSize > (v.getSup() - v.getInf() + 1))
                maxDSize = v.getSup() - v.getInf() + 1;
        }
        // An offset to deal with negative domains and positive domains whose minimum is not 0
        this.cste = -this.cste;
        gval = new BitSet(maxDSize);
        dVar = new IntList(vars.length);
        freeVars = new IntList(vars.length);
        ngraph = new BitSet[vars.length];
        ngraphSize = new int[vars.length];
        for (int i = 0; i < ngraph.length; i++) {
            ngraph[i] = new BitSet(vars.length);
        }
        nVars = vars.length;
    }

    // *****************************************************
    // Restricting efficiently a domain to a set of values
    // *****************************************************

    // calculer l'ensemble des valeurs interdites ?
    // forbiddenvalues = (domain ^ allowedvalues) xor allowedvalues
    //TODO : improve it with IStateBitSet api on intersection , union ....
    public void restrict(int db, IntDomainVar v, BitSet allowvalues) throws ContradictionException {
        //if (!v.intersect(BitSet val, cste)) this.fail()!
        int offset = v.getInf();
        BitSet allowedDomain = allowvalues.get(v.getInf() + cste, v.getSup() + cste + 1);
        int newInf = allowedDomain.nextSetBit(0) + offset;
        int newSup = allowedDomain.length() + offset;
        if (v.getInf() < newInf) {
               if (debug) System.out.println(db + " updateInf " + newInf + " of " + v);
        }
        v.updateInf(newInf, -1);
        v.updateSup(newSup, -1);
        if (v.hasEnumeratedDomain()) {
            IntDomain dom = v.getDomain();
            for (int val = dom.getNextValue(newInf); val <= v.getSup(); val=dom.getNextValue(val)) {
              if (!allowedDomain.get(val - offset)) {
                  if (debug) System.out.println("2 remove value " + val + " from " + v);
                  v.removeVal(val, -1);
              }
            }
        }
    }



    // *****************************************************
    // *****************************************************



    /**
     * @param v : Test if the intersection of the domain of v and gval is empty
     * @return false if v has at least one value in gval.
     */
    public boolean emptyIntersectionWithGval(IntDomainVar v) {
        IntIterator vdom = v.getDomain().getIterator();
        while (vdom.hasNext()) {
            if (gval.get(vdom.next() + cste)) return false;
        }
        return true;
    }

    /**
     * @return the intersection of all domains included in dVar as
     *         a BitSet
     */
    public BitSet intersectionDomains() {
        if (dVar.getSize() > 0) {
            List<Integer> inter = new LinkedList<Integer>();
            IntIterator vdom = vars[dVar.getFirst()].getDomain().getIterator();
            while (vdom.hasNext()) {
                inter.add(vdom.next());
            }
            IntIterator intIt = dVar.iterator();
            while (intIt.hasNext()) {
                IntDomainVar v = vars[intIt.next()];
                for (Iterator it = inter.iterator(); it.hasNext();) {
                    if (!v.canBeInstantiatedTo((Integer) it.next()))
                        it.remove();
                }
            }
            BitSet interDvar = new BitSet(maxDSize);
            for (Integer i : inter) {
                interDvar.set(i + cste);
            }
            return interDvar;
        } else return new BitSet();
    }

    /**
     * Build the reduced intersection graph and check for special cases where the complete
     * algorithm do not need to be called
     *
     * @return true if the the complete pruning has to be done
     * @throws i_want_to_use_this_old_version_of_choco.ContradictionException contradiction on the nvalue variable
     */
    public boolean basicPruning() throws ContradictionException {
        nGval = 0;
        gval.clear();
        dVar.reInit();
        freeVars.reInit();

        // basic lower bound based on instantiated variables
        for (int i = 0; i < nVars; i++) {
            IntDomainVar v = vars[i];
            if (v.isInstantiated()) {
                if (!gval.get(v.getVal() + cste)) {
                    nGval++;
                }
                gval.set(v.getVal() + cste);
            } else {
                freeVars.add(i);
            }
        }
        vars[nVars].updateInf(nGval, cIndices[nVars]);
        int K = vars[vars.length - 1].getSup() - nGval;

        if (K == 0) {
            pruningK0();
            return false;
        }

        // build dVar so that the variables are sorted according to their indexes
        // this fact is used later by the computeNeighborsGraph method
        if (nGval == 0) {
            dVar = freeVars.copy();
        } else {
             IntIterator intIt = freeVars.iterator();
             while (intIt.hasNext()) {
                int i = intIt.next();
                if(emptyIntersectionWithGval(vars[i])) dVar.add(i);
            }
        }

        // update the lower bound of the nvalue variable
        // pruning for special cases where the bound is reached or almost reached
        if (K == 1) {
            if (dVar.getSize() > 0)
                pruningK1();
            return false;
        }
        return true;
    }


    // case of Nvalue instantiated to nGval
    public void pruningK0() throws ContradictionException {
        IntIterator intIt = freeVars.iterator();
        while (intIt.hasNext()) {
            int i = intIt.next();
            restrict(1,vars[i],gval);
        }
    }

    // case of Nvalue reduced to [nGval,nGval + 1]
    public void pruningK1() throws ContradictionException {
        BitSet interDvar = intersectionDomains();
        interDvar.or(gval);
        IntIterator intIt = freeVars.iterator();
        while (intIt.hasNext()) {
            int i = intIt.next();
            restrict(2,vars[i],interDvar);
        }
    }

    // a greedy approach to compute an independent set of the intersection graph
    public void MDPruning() throws ContradictionException {
        if (basicPruning() && (nGval + dVar.getSize() >= vars[nVars].getSup())) {
            LinkedList<IntDomainVar> A = new LinkedList<IntDomainVar>();
            computeNeighborsGraph();
            while (dVar.getSize() > 0) {
                int min = Integer.MAX_VALUE;
                int y = -1;
                IntIterator intIt = dVar.iterator();
                while (intIt.hasNext()) {
                    int next = intIt.next();
                    if (min >= ngraph[next].size()) {
                        min = ngraph[next].size();
                        y = next;
                    }
                }
                intIt = dVar.iterator();
                while (intIt.hasNext()) {
                    int next = intIt.next();
                    if (next == y || ngraph[y].get(next))
                        intIt.remove();
                }
                A.add(vars[y]);
            }
            int lb = A.size() + nGval;
            vars[nVars].updateInf(lb, cIndices[nVars]);
            if (lb == vars[nVars].getSup())
                pruning(A);
        }

    }

    public void computeNeighborsGraph() {
        for (int i = 0; i < ngraph.length; i++) {
            ngraph[i].clear();
            ngraphSize[i] = 0;
        }
        IntIterator intIt = dVar.iterator();
        while (intIt.hasNext()) {
            int i = intIt.next();
            IntIterator intIt2 = dVar.iterator();
            while (intIt2.hasNext()) {
                int j = intIt2.next();
                if (i <= j) break;
                else if (vars[i].canBeEqualTo(vars[j])) {
                    ngraph[i].set(j);
                    ngraph[j].set(i);
                    ngraphSize[i] ++;
                    ngraphSize[j] ++;
                }
            }
        }
    }

    public void pruning(LinkedList<IntDomainVar> A) throws ContradictionException {
        BitSet unionOfAllowedValue = gval;
        for (IntDomainVar x : A) {
            IntIterator it = x.getDomain().getIterator();
            while (it.hasNext()) {
                int value = it.next();
                unionOfAllowedValue.set(value + cste);
            }
        }
        IntIterator intIt = freeVars.iterator();
        while (intIt.hasNext()) {
            int i = intIt.next();
            restrict(3,vars[i],unionOfAllowedValue);

        }
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (debug) System.out.println("inf " + vars[idx] + " to " + vars[idx].getInf());
        constAwake(false);
    }

    public void awakeOnInst(int idx) throws ContradictionException {
        if (debug) System.out.println("instantiate " + vars[idx] + " to " + vars[idx].getVal());
        constAwake(false);
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (debug) System.out.println("sup " + vars[idx] + " to " + vars[idx].getSup());
        constAwake(false);
    }

    public void awakeOnRemovals(int idx, IntIterator deltaDomain) throws ContradictionException {
        if (debug) {
            while (deltaDomain.hasNext()) {
                System.out.println("remove from " + vars[idx] + " value " + deltaDomain.next());
            }
        }
        constAwake(false);
    }

    public void awake() throws ContradictionException {
        propagate();
    }

    public void propagate() throws ContradictionException {
        MDPruning();
    }

  /**
   * This method assumes that all variables are instantiated and checks if the values are consistent with the
   * constraint.
   * Here it couns how many values are in the set, thanks to a BitSet in order to know if a value has already been
   * counted.
   * @return true if the number of different values in all variables except the last one is less (or equal) than
   * the value of the last variable
   */
    public boolean isSatisfied(int[] tuple) {
       BitSet b = new BitSet();
       int nval = 0;
       for (int i = 0; i < nVars; i++) {
           int val = tuple[i];
           if (!b.get(val)) {
               nval ++;
               b.set(val);
           }
       }
       return nval <= tuple[nVars];
    }

    public String pretty() {
      StringBuilder sb = new StringBuilder();
      sb.append("AtMostNValue({");
      for (int i = 0; i < vars.length - 1; i++) {
        if (i > 0) sb.append(", ");
        IntDomainVar var = vars[i];
        sb.append(var.pretty());
      }
      sb.append("}, ").append(vars[vars.length-1]).append(")");
      return sb.toString();
    }
}
