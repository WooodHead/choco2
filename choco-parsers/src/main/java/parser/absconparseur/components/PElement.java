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

import parser.absconparseur.Toolkit;

public class PElement extends PGlobalConstraint {
	private final PVariable index;

	private final Object[] table;

	private final Object value;

	private static final int offset=1; // by default, indexing starts from 1

	private final int indexPositionInScope;

	private final int[] tablePositionsInScope;
	
	private final int valuePositionInScope;

	public PElement(String name, PVariable[] scope, PVariable indexVariable, Object[] table, Object value) {
		super(name, scope);
		this.index = indexVariable;
		this.table = table;
		this.value = value;
		indexPositionInScope = Toolkit.searchFirstObjectOccurrenceIn(indexVariable, scope);
		tablePositionsInScope = computeObjectPositionsInScope(table);
		valuePositionInScope = Toolkit.searchFirstObjectOccurrenceIn(value, scope);
	}

	public long computeCostOf(int[] tuple) {
		int indexInTable = tuple[indexPositionInScope]-offset;
		Object object = table[indexInTable];
		int result = (object instanceof Integer ? (Integer) object : tuple[tablePositionsInScope[indexInTable]]);
		boolean satisfied = result == (value instanceof Integer ? (Integer) value : tuple[valuePositionInScope]);
		return satisfied ? 0 : 1;
	}

	public String toString() {
		StringBuilder s = new StringBuilder(128);
        s.append(super.toString()).append(" : element\n\t");
        s.append("index=").append(index.getName()).append("  table=");
		for (int i = 0; i < table.length; i++)
            s.append(computeStringRepresentationOf(table[i])).append(' ');
        s.append("  value=").append(computeStringRepresentationOf(value));
		return s.toString();
	}

    public PVariable getIndex() {
        return index;
    }

    public Object[] getTable() {
        return table;
    }

    public Object getValue() {
        return value;
    }
}
