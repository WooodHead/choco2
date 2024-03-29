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

package choco.kernel.common.util.tools;

import choco.kernel.common.util.comparator.ConstantPermutation;
import choco.kernel.common.util.comparator.IPermutation;
import choco.kernel.common.util.comparator.Identity;
import choco.kernel.common.util.comparator.IntPermutation;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerVariable;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 3 juil. 2009
* Since : Choco 2.1.0
* Update : Choco 2.1.0
*/
public final class PermutationUtils {
   
	
	
	private PermutationUtils() {
		super();
	}

	public static IPermutation getIdentity() {
        return Identity.SINGLETON;
    }

	public static IPermutation replaceByIdentity(IPermutation permutation) {
        return permutation.isIdentity() ? Identity.SINGLETON : permutation;
    }

    public static IPermutation getSortingPermuation(int[] criteria) {
        return getSortingPermuation(criteria, false);
    }

    public static IPermutation getSortingPermuation(int[] criteria,boolean reverse) {
        return new IntPermutation(criteria,reverse);
    }

    public static IPermutation getSortingPermuation(IntegerConstantVariable[] criteria,boolean reverse) {
        return new ConstantPermutation(criteria,reverse);
    }

    public static IntegerConstantVariable[] applyPermutation(IPermutation permutation, IntegerConstantVariable[] source) {
        if(permutation.isIdentity()) {return source;}
        else {
            final IntegerConstantVariable[] dest = new IntegerConstantVariable[source.length];
            permutation.applyPermutation(source,dest);
            return dest;
        }
    }
    
    public static IntegerVariable[] applyPermutation(IPermutation permutation, IntegerVariable[] source) {
        if(permutation.isIdentity()) {return source;}
        else {
            final IntegerVariable[] dest = new IntegerVariable[source.length];
            permutation.applyPermutation(source,dest);
            return dest;
        }
    }
}
