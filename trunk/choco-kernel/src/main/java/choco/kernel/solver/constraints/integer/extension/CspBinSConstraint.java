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

package choco.kernel.solver.constraints.integer.extension;

import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

public abstract class CspBinSConstraint extends AbstractBinIntSConstraint {


    protected BinRelation relation;


    protected CspBinSConstraint(IntDomainVar x, IntDomainVar y, BinRelation relation) {
        super(x, y);
        this.relation = relation;
    }

    /**
     * Checks if the constraint is satisfied when the variables are instantiated.
     *
     * @return true if the constraint is satisfied
     */

    public boolean isSatisfied(int[] tuple) {
        return relation.isConsistent(tuple[0], tuple[1]); //table.get((v1.getVal() - offset) * n + (v0.getVal() - offset));
    }

    public BinRelation getRelation() {
        return relation;
    }


    public Boolean isEntailed() {
        int nbCons = 0;

        int ub0 = v0.getSup();
        for (int val0 = v0.getInf(); val0 <= ub0; val0 = v0.getNextDomainValue(val0)) {
            int nbS = 0;
            int ub1 = v1.getSup();
            for (int val1 = v1.getInf(); val1 <= ub1; val1 = v1.getNextDomainValue(val1)) {
                if (relation.isConsistent(val0, val1)) {
                    nbS++;
                }
            }
            if (nbS > 0 && nbS < v1.getDomainSize()) {
                return null;
            }
            nbCons += nbS;
        }
        if (nbCons == 0) {
            return Boolean.FALSE;
        } else if (nbCons == v0.getDomainSize() * v1.getDomainSize()) {
            return Boolean.TRUE;
        }
        return null;
    }
}
