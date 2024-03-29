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

package choco.cp.model.managers.constraints.global;

import choco.Options;
import choco.cp.model.managers.IntConstraintManager;
import choco.cp.solver.constraints.global.IncreasingNValue;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.util.List;

/**
 * User : xlorca
 * Mail : xlorca(a)emn.fr
 * Date : 29 janv. 2010
 * Since : Choco 2.1.1
 */
public final class IncreasingNValueManager extends IntConstraintManager {

    public SConstraint makeConstraint(Solver solver, IntegerVariable[] integerVariables, Object parameters, List<String> options) {
        IntDomainVar[] vars = new IntDomainVar[integerVariables.length-1];
        System.arraycopy(solver.getVar(integerVariables),1,vars,0,integerVariables.length-1);
        if(options.contains(Options.C_INCREASING_NVALUE_ATLEAST)){
            return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.ATLEAST);
        }
        if(options.contains(Options.C_INCREASING_NVALUE_ATMOST)){
            return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.ATMOST);
        }   
        return new IncreasingNValue(solver.getVar(integerVariables[0]),vars, IncreasingNValue.Mode.BOTH);
    }


}
