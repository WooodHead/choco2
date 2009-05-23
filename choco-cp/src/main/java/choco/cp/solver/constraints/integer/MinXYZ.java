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
package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.common.util.DisposableIntIterator;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomain;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: Richaud
 * Date: 8 janv. 2007
 * Since : Choco 2.0.0
 *
 */
public class MinXYZ extends AbstractTernIntSConstraint {
    public MinXYZ(IntDomainVar x, IntDomainVar y, IntDomainVar min) {
        super(min, x, y);
    }

    public boolean isSatisfied(int[] tuple) {
        return (Math.min(tuple[2], tuple[1]) == tuple[0]);
    }

  public String pretty() {
    return "min(" + v2.pretty() + "," + v1.pretty() + ") = " + v0.pretty();
  }

    @Override
    public int getFilteredEventMask(int idx) {
        if(idx == 0){
            if(v0.hasEnumeratedDomain()){
                return IntVarEvent.INSTINTbitvector + IntVarEvent.REMVALbitvector;
            }else{
                return IntVarEvent.INSTINTbitvector + IntVarEvent.BOUNDSbitvector;
            }
        }else if(idx == 1){
            if(v1.hasEnumeratedDomain()){
                return IntVarEvent.INSTINTbitvector + IntVarEvent.REMVALbitvector;
            }else{
                return IntVarEvent.INSTINTbitvector + IntVarEvent.BOUNDSbitvector;
            }
        }else{
            if(v2.hasEnumeratedDomain()){
                return IntVarEvent.INSTINTbitvector + IntVarEvent.REMVALbitvector;
            }else{
                return IntVarEvent.INSTINTbitvector + IntVarEvent.BOUNDSbitvector;
            }
        }
    }


    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0) {
            v1.updateInf(v0.getInf(), cIdx0);
            v2.updateInf(v0.getInf(), cIdx0);
        } else {
            v0.updateInf(Math.min(v1.getInf(), v2.getInf()), cIdx0);
        }
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0) {
            if (v1.getInf() > v2.getSup()) {
                v2.updateSup(v0.getSup(), cIdx2);
            }
            if (v2.getInf() > v1.getSup()) {
                v1.updateSup(v0.getSup(), cIdx1);
            }
        } else {
            v0.updateSup(Math.min(v1.getSup(), v2.getSup()), cIdx0);
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0) {
            if (v1.getInf() > x) {
                v2.removeVal(x, cIdx2);
            }
            if (v2.getInf() > x) {
                v1.removeVal(x, cIdx1);
            }
        } else {
            if (!v1.canBeInstantiatedTo(x) && !v2.canBeInstantiatedTo(x)) {
                v0.removeVal(x, cIdx0);
            }
        }
    }

    /**
     * Propagation for the constraint awake var.
     *
     * @throws choco.kernel.solver.ContradictionException
     *          if a domain becomes empty or the
     *          filtering algorithm infers a contradiction
     */
    public void propagate() throws ContradictionException {
        filter(0);
        filter(1);
        filter(2);
    }

    public void filter(int idx) throws ContradictionException {
        if (idx == 0) {
            v0.updateInf(Math.min(v1.getInf(), v2.getInf()), cIdx0);
            v0.updateSup(Math.min(v1.getSup(), v2.getSup()), cIdx0);

            if (v0.hasEnumeratedDomain()) {
                IntDomain dom0 = v0.getDomain();
                DisposableIntIterator it = dom0.getIterator();
                while (it.hasNext()) {
                    int valeur = it.next();
                    if (!v1.canBeInstantiatedTo(valeur) && !v2.canBeInstantiatedTo(valeur)) {
                        v0.removeVal(valeur, cIdx0);
                    }
                }
                it.dispose();
            }
        } else if (idx == 1) {
            v1.updateInf(v0.getInf(), cIdx1);

            if (v1.getSup() < v2.getInf()) {
                v1.updateSup(v0.getSup(), cIdx1);
            }
            if (v1.hasEnumeratedDomain()) {
                IntDomain dom1 = v1.getDomain();
                DisposableIntIterator it = dom1.getIterator();
                while (it.hasNext()) {
                    int valeur = it.next();
                    if (!v0.canBeInstantiatedTo(valeur) && (v2.getInf() > valeur)) {
                        v1.removeVal(valeur, cIdx1);
                    }
                }
                it.dispose();
            }

        } else if (idx == 2) {
            v2.updateInf(v0.getInf(), cIdx2);
            if (v2.getSup() < v1.getInf()) {
                v2.updateSup(v0.getSup(), cIdx2);
            }
            if (v2.hasEnumeratedDomain()) {
                IntDomain dom2 = v2.getDomain();
                DisposableIntIterator it = dom2.getIterator();
                while (it.hasNext()) {
                    int valeur = it.next();
                    if (!v0.canBeInstantiatedTo(valeur) && (v1.getInf() > valeur)) {
                        v2.removeVal(valeur, cIdx2);
                    }
                }
                it.dispose();
            }

        }
    }

    public void awakeOnInst(int idx, int val) throws ContradictionException {
        if (idx == 0) {
            v1.updateInf(val, cIdx1);
            v2.updateInf(val, cIdx2);
            if (!v1.canBeInstantiatedTo(val)) v2.instantiate(val, cIdx2);
            if (!v2.canBeInstantiatedTo(val)) v1.instantiate(val, cIdx1);
        } else if (idx == 1) {
            //TODO ??
            if (val < v2.getInf()) v0.instantiate(val, cIdx0);
        } else if (idx == 2) {
            //TODO ??
            if (val < v1.getInf()) v0.instantiate(val, cIdx0);
        }
    }


}

