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

package choco.kernel.memory.structure.iterators;

import choco.kernel.common.util.disposable.PoolManager;
import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.common.util.iterators.DisposableIterator;
import choco.kernel.memory.structure.Couple;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;
import choco.kernel.solver.constraints.AbstractSConstraint;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 mars 2010<br/>
 * Since : Choco 2.1.1<br/>
 */
public final class PSCLEIterator<C extends AbstractSConstraint> extends DisposableIterator<Couple<C>> {

    private static final ThreadLocal<PoolManager<PSCLEIterator>> manager = new ThreadLocal<PoolManager<PSCLEIterator>>();

    private C cstrCause;

    private DisposableIntIterator cit;

    private PartiallyStoredIntVector event;

    private PartiallyStoredVector<C> elements;

    private PartiallyStoredIntVector indices;

    private final Couple<C> cc = new Couple<C>();

    @SuppressWarnings({"unchecked"})
    public static <C extends AbstractSConstraint> PSCLEIterator getIterator(
            final PartiallyStoredIntVector event, final C cstrCause,
            final PartiallyStoredVector<C> elements,
            final PartiallyStoredIntVector indices) {
        PoolManager<PSCLEIterator> tmanager = manager.get();
        if (tmanager == null) {
            tmanager = new PoolManager<PSCLEIterator>();
            manager.set(tmanager);
        }
        PSCLEIterator it = tmanager.getE();
        if (it == null) {
            it = new PSCLEIterator();
        }
        it.init(cstrCause, event, elements, indices);
        return it;
    }

    private void init(final C aCause, final PartiallyStoredIntVector anEvent,
                      final PartiallyStoredVector<C> someElements, final PartiallyStoredIntVector someIndices) {
        this.event = anEvent;
        this.cit = this.event.getIndexIterator();
        this.cstrCause = aCause;
        this.elements = someElements;
        this.indices = someIndices;
    }

    /**
     * Returns <tt>true</tt> if the iteration has more elements. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return an element
     * rather than throwing an exception.)
     *
     * @return <tt>true</tt> if the iterator has more elements.
     */
    @Override
    public boolean hasNext() {
        while (cit.hasNext()) {
            final int idx = event.get(cit.next());
            final C cstr = elements.get(idx);
            if (cstr != cstrCause && cstr.isActive()) {
                cc.init(cstr, indices.get(idx));
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws java.util.NoSuchElementException
     *          iteration has no more elements.
     */
    @Override
    public Couple<C> next() {
        return cc;
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation).  This method can be called only once per
     * call to <tt>next</tt>.  The behavior of an iterator is unspecified if
     * the underlying collection is modified while the iteration is in
     * progress in any way other than by calling this method.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *                                       operation is not supported by this Iterator.
     * @throws IllegalStateException         if the <tt>next</tt> method has not
     *                                       yet been called, or the <tt>remove</tt> method has already
     *                                       been called after the last call to the <tt>next</tt>
     *                                       method.
     */
    @Override
    public void remove() {
        cit.remove();
    }


    @Override
    public void dispose() {
        cit.dispose();
        manager.get().returnE(this);
    }
}