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

package parser.absconparseur.components;

import gnu.trove.TIntIntHashMap;


public class PGlobalCardinality extends PGlobalConstraint {

    public Object[] table;
    private final int[] tablePositionsInScope;
    public int offset; // offset of variables and values/noccurrences

    public PGlobalCardinality(String name, PVariable[] scope, Object[] table, int offset) {
		super(name, scope);
        this.table = table;
        tablePositionsInScope = computeObjectPositionsInScope(table);
        this.offset = offset;
	}

	public long computeCostOf(int[] tuple) {
        TIntIntHashMap count = new TIntIntHashMap();
        for(int v = 0;  v < offset; v++){
            Object object = table[v];
            int result = (object instanceof Integer ? (Integer) object : tuple[tablePositionsInScope[v]]);
            int nb = 1;
            if(count.containsKey(result)){
                nb += count.get(result);
            }
            count.put(result, nb);
        }

        for(int i = offset; i < table.length; i+=2){
            int value = (Integer)table[i];
            Object object = table[i+1];
            int result = (object instanceof Integer ? (Integer) object : tuple[tablePositionsInScope[i+1]]);
            if(count.get(value)!=result){
                return 1;
            }
        }
		return 0;
	}

	public String toString() {
		return super.toString() + " : globalCardinality";
	}
}