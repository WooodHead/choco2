/* ************************************************
*           _       _                            *
*          |  �(..)  |                           *
*          |_  J||L _|        CHOCO solver       *
*                                                *
*     Choco is a java library for constraint     *
*     satisfaction problems (CSP), constraint    *
*     programming (CP) and explanation-based     *
*     constraint solving (e-CP). It is built     *
*     on a event-based propagation mechanism     *
*     with backtrackable structures.             *
*                                                *
*     Choco is an open-source software,          *
*     distributed under a BSD licence            *
*     and hosted by sourceforge.net              *
*                                                *
*     + website : http://choco.emn.fr            *
*     + support : choco@emn.fr                   *
*                                                *
*     Copyright (C) F. Laburthe,                 *
*                   N. Jussien    1999-2009      *
**************************************************/
package choco.cp.solver.variables.delta;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.variables.delta.IDeltaDomain;

/*
* User : charles
* Mail : cprudhom(a)emn.fr
* Date : 11 d�c. 2009
* Since : Choco 2.1.1
* Update : Choco 2.1.1
*/
public class BipartiteDeltaDomain implements IDeltaDomain {

    /**
     * A pointer to the first removed value to be propagated.
     * its position in the list
     */
    private int beginningOfDeltaDomain;

    /**
     * A pointer on the last removed value propagated.
     * its position in the list
     */
    private int endOfDeltaDomain;

    /**
     * The number of values currently in the domain.
     */
    IStateInt valuesInDomainNumber;

    /**
     * The values (not ordered) contained in the domain.
     */
    protected int[] values;

    public BipartiteDeltaDomain(int size, int[] values, IStateInt valuesInDomainNumber) {
        this.endOfDeltaDomain = size;
        this.beginningOfDeltaDomain = size;
        this.values = values;
        this.valuesInDomainNumber = valuesInDomainNumber;
    }

    /**
     * The delta domain container is "frozen" (it can no longer accept new value removals)
     * so that this set of values can be iterated as such�
     */
    @Override
    public void freeze() {
        // freeze all data associated to bounds for the the event
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
    }

    /**
     * Update the delta domain
     *
     * @param value removed
     */
    @Override
    public void remove(int value) {
        if (endOfDeltaDomain <= value) {
            endOfDeltaDomain = value + 1;
        }
    }

    /**
     * cleans the data structure implementing the delta domain
     */
    @Override
    public void clear() {
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
        endOfDeltaDomain = beginningOfDeltaDomain;
    }

    /**
     * Check if the delta domain is released or frozen.
     *
     * @return true if release
     */
    @Override
    public boolean isReleased() {
        return beginningOfDeltaDomain == endOfDeltaDomain;
    }

    /**
     * after an iteration over the delta domain, the delta domain is reopened again.
     *
     * @return true iff the delta domain is reopened empty (no updates have been made to the domain
     *         while it was frozen, false iff the delta domain is reopened with pending value removals (updates
     *         were made to the domain, while the delta domain was frozen).
     */
    @Override
    public boolean release() {
        // release all data associated to bounds for the the event
        endOfDeltaDomain = beginningOfDeltaDomain;
        beginningOfDeltaDomain = valuesInDomainNumber.get() + 1;
        return beginningOfDeltaDomain == endOfDeltaDomain;
    }

    /**
     * Iterator over delta domain
     *
     * @return delta iterator
     */
    @Override
    public DisposableIntIterator iterator() {
        DeltaBipartiteIterator iter = (DeltaBipartiteIterator) _cachedDeltaIntDomainIterator;
        if (iter != null && iter.reusable) {
            iter.init();
            return iter;
        }
        _cachedDeltaIntDomainIterator = new DeltaBipartiteIterator();
        return _cachedDeltaIntDomainIterator;
    }

    private DisposableIntIterator _cachedDeltaIntDomainIterator = null;


    class DeltaBipartiteIterator extends DisposableIntIterator {
        protected int currentIndex = -1;

        private DeltaBipartiteIterator() {
            init();
        }

        public void init() {
            super.init();
            currentIndex = beginningOfDeltaDomain;
        }


        public boolean hasNext() {
            return currentIndex < endOfDeltaDomain;
        }

        public int next() {
            return values[currentIndex++];
        }

    }

    @Override
    public IDeltaDomain copy() {
        return new BipartiteDeltaDomain(this.endOfDeltaDomain, this.values, null);
    }

    /**
     * pretty printing of the object. This String is not constant and may depend on the context.
     *
     * @return a readable string representation of the object
     */
    @Override
    public String pretty() {
        return beginningOfDeltaDomain +" -> "+ endOfDeltaDomain;
    }
}