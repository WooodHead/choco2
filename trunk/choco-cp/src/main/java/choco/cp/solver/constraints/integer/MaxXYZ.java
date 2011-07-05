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

package choco.cp.solver.constraints.integer;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractTernIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/*
 * Created by IntelliJ IDEA.
 * User: Richaud
 * Date: 8 janv. 2007
 * Since : Choco 2.0.0
 *
 */
public final class MaxXYZ extends AbstractTernIntSConstraint {
    public MaxXYZ(IntDomainVar x, IntDomainVar y, IntDomainVar max) {
        super(max, x, y);
    }

    public boolean isSatisfied(int[] tuple) {
        return (Math.max(tuple[2], tuple[1]) == tuple[0]);
    }

    @Override
    public int getFilteredEventMask(int idx) {
        if (idx == 0) {
            if (v0.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        } else if (idx == 1) {
            if (v1.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        } else {
            if (v2.hasEnumeratedDomain()) {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.REMVAL_MASK;
            } else {
                return IntVarEvent.INSTINT_MASK + IntVarEvent.BOUNDS_MASK;
            }
        }
    }


    public String pretty() {
        return "max(" + v2.pretty() + "," + v1.pretty() + ") = " + v0.pretty();
    }

    public void awakeOnSup(int idx) throws ContradictionException {
        if (idx == 0) {
            v1.updateSup(v0.getSup(), this, false);
            v2.updateSup(v0.getSup(), this, false);
        } else {
            v0.updateSup(Math.max(v1.getSup(), v2.getSup()), this, false);
        }
    }

    public void awakeOnInf(int idx) throws ContradictionException {
        if (idx == 0) {
            if (v1.getInf() > v2.getSup()) {
                v1.updateInf(v0.getInf(), this, false);
            }

            if (v2.getInf() > v1.getSup()) {
                v2.updateInf(v0.getInf(), this, false);
            }
        } else {
            v0.updateInf(Math.max(v1.getInf(), v2.getInf()), this, false);
        }
    }

    public void awakeOnRem(int idx, int x) throws ContradictionException {
        if (idx == 0) {
            if (x > v2.getSup()) {
                v1.removeVal(x, this, false);
            }

            if (x > v1.getSup()) {
                v2.removeVal(x, this, false);
            }
        } else {
            if (!v1.canBeInstantiatedTo(x) && !v2.canBeInstantiatedTo(x)) {
                v0.removeVal(x, this, false);
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
            v0.updateSup(Math.max(v1.getSup(), v2.getSup()), this, false);
            v0.updateInf(Math.max(v1.getInf(), v2.getInf()), this, false);

            if (v0.hasEnumeratedDomain()) {
                int ub0 = v0.getSup();
                for (int valeur = v0.getInf(); valeur <= ub0; valeur = v0.getNextDomainValue(valeur)) {
                    if (!v1.canBeInstantiatedTo(valeur) && !v2.canBeInstantiatedTo(valeur)) {
                        v0.removeVal(valeur, this, false);
                    }
                }
            }
        } else if (idx == 1) {
            v1.updateSup(v0.getSup(), this, false);
            if (v1.getInf() > v2.getSup()) {
                v1.updateInf(v0.getInf(), this, false);
            }

            if (v1.hasEnumeratedDomain()) {
                int ub1 = v1.getSup();
                for (int valeur = v1.getInf(); valeur <= ub1; valeur = v1.getNextDomainValue(valeur)) {
                    if (!v0.canBeInstantiatedTo(valeur) && (valeur > v2.getSup())) {
                        v1.removeVal(valeur, this, false);
                    }
                }
            }


        } else if (idx == 2) {
            v2.updateSup(v0.getSup(), this, false);
            if (v2.getInf() > v1.getSup()) {
                v2.updateInf(v0.getInf(), this, false);
            }
            if (v2.hasEnumeratedDomain()) {

                int ub2 = v2.getSup();
                for (int valeur = v2.getInf(); valeur <= ub2; valeur = v2.getNextDomainValue(valeur)) {
                    if (!v0.canBeInstantiatedTo(valeur) && (valeur > v1.getSup())) {
                        v2.removeVal(valeur, this, false);
                    }
                }
            }
        }
    }

    public void awakeOnInst(int idx, int val) throws ContradictionException {
        if (idx == 0) {
            v1.updateSup(val, this, false);
            v2.updateSup(val, this, false);
            if (!v1.canBeInstantiatedTo(val)) v2.instantiate(val, this, false);
            if (!v2.canBeInstantiatedTo(val)) v1.instantiate(val, this, false);
        } else if (idx == 1) {
            if (val > v2.getSup()) v0.instantiate(val, this, false);
        } else if (idx == 2) {
            if (val > v1.getSup()) v0.instantiate(val, this, false);
        }
    }


}


