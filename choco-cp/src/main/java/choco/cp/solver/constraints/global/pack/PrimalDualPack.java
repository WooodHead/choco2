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
package choco.cp.solver.constraints.global.pack;

import choco.cp.solver.constraints.BitFlags;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.util.IntIterator;
import choco.kernel.common.util.UtilAlgo;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.Solver;
import choco.kernel.solver.SolverException;
import choco.kernel.solver.constraints.set.AbstractLargeSetIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import choco.kernel.solver.variables.set.SetVar;

import java.awt.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * <b>{@link PrimalDualPack} which maintains a primal-dual packing domain.</b><br>
 * The primal model consists of {@link bins} variables. {@link bins}[item] = bin means that item is packed into bin.<br>
 * The dual model consists of {@link svars} variables. item is in {@link svars}[bin] also means that item is packed into bin.
 * @author Arnaud Malapert</br>
 * @since 5 déc. 2008 version 2.0.1</br>
 * @version 2.0.3</br>
 */
public class PrimalDualPack extends AbstractLargeSetIntSConstraint implements IPackSConstraint {

	public final PackFiltering filtering;

	protected final BinStatus status;

	/** The sizes of the items. */
	protected final IntDomainVar[] sizes;

	/** The loads of the bins. */
	protected final IntDomainVar[] loads;

	/** The bin of each item. */
	protected final IntDomainVar[] bins;


	public PrimalDualPack(SetVar[] itemSets, IntDomainVar[] loads, IntDomainVar[] sizes,
			IntDomainVar[] bins,IntDomainVar nbNonEmpty, BitFlags  flags) {
		super(UtilAlgo.append(loads,sizes,bins,new IntDomainVar[]{nbNonEmpty}),itemSets);
		this.loads=loads;
		this.sizes=sizes;
		this.bins =bins;
		filtering = new PackFiltering(this,new PackBound(), flags);
		status = new BinStatus(this.sizes);
	}

	public final boolean isEmpty(int bin) {
		return svars[bin].getKernelDomainSize()==0;
	}
	public final int getRequiredSpace(int bin) {
		IntIterator iter= svars[bin].getDomain().getKernelIterator();
		int load = 0;
		while(iter.hasNext()) {
			load+= sizes[iter.next()].getVal();
		}
		return load;
	}


	public final int getRemainingSpace(int bin) {
		return loads[bin].getSup() - getRequiredSpace(bin);
	}

	protected boolean isSetEvent(final int varIdx) {
		return varIdx<svars.length;
	}

	protected int getItemIndex(final int varIdx) {
		int idx = varIdx-(2*loads.length+sizes.length);
		return idx<0 || idx == sizes.length ? -1 : idx;
	}

	protected int getItemCindice(final int item) {
		return int_cIndices[loads.length+sizes.length+item];
	}

	@Override
	public void setSolver(Solver solver) {
		super.setSolver(solver);
		filtering.setSolver(solver);
	}

	public final IntDomainVar[] getBins() {
		return bins;
	}


	//****************************************************************//
	//********* Filtering interface **********************************//
	//****************************************************************//


	@Override
	public int getNbBins() {
		return sizes.length;
	}

	@Override
	public int getNbItems() {
		return loads.length;
	}
	

	@Override
	public IntDomainVar[] getLoads() {
		return loads;
	}


	@Override
	public IntDomainVar[] getSizes() {
		return sizes;
	}

	@Override
	public BinStatus getStatus(int bin) {
		status.set(bin, svars[bin]);
		return status;
	}

	@Override
	public boolean isFilled(int bin) {
		return svars[bin].isInstantiated();
	}

	@Override
	public boolean pack(int item, int bin) throws ContradictionException {
		boolean res = svars[bin].addToKernel(item, set_cIndices[bin]);
		if(bins[item].isInstantiated()) {
			ChocoLogging.flushLogs();
			throw new SolverException("Internal error : "+bins[item].pretty()+" should not be instantiated");
		}
		final IntIterator iter = bins[item].getDomain().getIterator();
		//remove from other env
		while(iter.hasNext()) {
			final int b= iter.next();
			if(bin!=b) {
				res |= svars[b].remFromEnveloppe(item, set_cIndices[b]);
			}
		}

		return bins[item].instantiate(bin, getItemCindice(item)) || res;
	}

	@Override
	public boolean remove(int item, int bin) throws ContradictionException {
		boolean res = svars[bin].remFromEnveloppe(item, set_cIndices[bin]);
		res |= bins[item].removeVal(bin, getItemCindice(item));
		if(bins[item].isInstantiated()) {
			final int b = bins[item].getVal();
			svars[b].addToKernel(item, set_cIndices[b]);
		}
		return res;
	}


	@Override
	public boolean updateInfLoad(int bin, int load) throws ContradictionException {
		return loads[bin].updateInf(load, int_cIndices[bin]);

	}

	@Override
	public void updateNbNonEmpty(Point bounds) throws ContradictionException {
		final int idx = ivars.length-1;
		//		if(bounds.x>ivars[idx].getSup()) {
		//			LOGGER.severe("fail");
		//		}
		ivars[idx].updateInf(bounds.x, int_cIndices[idx]);
		ivars[idx].updateSup(bounds.y, int_cIndices[idx]);
	}

	@Override
	public boolean updateSupLoad(int bin, int load) throws ContradictionException {
		return loads[bin].updateSup(load, int_cIndices[bin]);
	}

	//****************************************************************//
	//********* Events *******************************************//
	//****************************************************************//



	@Override
	public boolean isConsistent() {
		// really no idea. wait and propagate
		return false;
	}

	protected void checkBounds(int item) throws ContradictionException {
		bins[item].updateInf(0, getItemCindice(item));
		bins[item].updateSup(svars.length-1, getItemCindice(item));
	}

	protected void checkEnveloppes() throws ContradictionException {
		for (int bin = 0; bin < svars.length; bin++) {
			int inf;
			while( (inf = svars[bin].getEnveloppeInf())<0) {
				svars[bin].remFromEnveloppe(inf,set_cIndices[bin]);
			}
			int sup;
			while( (sup = svars[bin].getEnveloppeSup()) > bins.length-1) {
				svars[bin].remFromEnveloppe(sup,set_cIndices[bin]);
			}
		}
	}
	@Override
	public void awake() throws ContradictionException {
		//initial channeling, I would prefer be warned by events, but it is not possible
		checkEnveloppes();
		for (int item = 0; item < bins.length; item++) {
			checkBounds(item);
			if(bins[item].isInstantiated()) {
				//the item is packed
				final int b0 = bins[item].getVal();
				svars[b0].addToKernel(item,set_cIndices[b0]);
				for (int b = 0; b < b0; b++) {
					svars[b].remFromEnveloppe(item,set_cIndices[b]);
				}
				for (int b = b0+1; b < svars.length; b++) {
					svars[b].remFromEnveloppe(item,set_cIndices[b]);
				}
			}else {
				for (int bin = 0; bin < svars.length; bin++) {
					if(svars[bin].isInDomainEnveloppe(item)) {
						//item could be packed here
						if(svars[bin].isInDomainKernel(item)) {
							//item is packed
							bins[item].instantiate(bin, getItemCindice(item));
						}else if(! bins[item].getDomain().contains(bin)) {
							//in fact, channeling fails
							svars[bin].remFromEnveloppe(item,set_cIndices[bin]);
						}
						//channeling ok enveloppe-domain
					}else {
						//otherwise remove from domain
						bins[item].removeVal(bin, getItemCindice(item));
					}
				}
			}
		}
		super.awake();
	}

	@Override
	public void awakeOnEnv(int varIdx, int x) throws ContradictionException {
		bins[x].removeVal(varIdx, getItemCindice(x));
		//if the item is packed, update variables
		if(bins[x].isInstantiated()) {
			final int b = bins[x].getVal();
			svars[b].addToKernel(x, set_cIndices[b]);
		}
		this.constAwake(false);
	}


	protected void checkDeltaDomain(int item) throws ContradictionException {
		final IntIterator iter=bins[item].getDomain().getDeltaIterator();
		if(iter.hasNext()) {
			while(iter.hasNext()) {
				final int b=iter.next();
				svars[b].remFromEnveloppe(item, set_cIndices[b]);
			}
		}else {
			throw new SolverException("empty delta domain: "+bins[item].pretty());
		}
	}

	@Override
	public void awakeOnBounds(int varIndex) throws ContradictionException {
		final int item = getItemIndex(varIndex);
		if(item>=0) {
			//the item is not packed
			//so, we can safely remove from other enveloppes
			checkDeltaDomain(item);
		}
		this.constAwake(false);
	}


	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		awakeOnBounds(varIdx);
	}

	@Override
	public void awakeOnInst(int varIdx) throws ContradictionException {
		if(isSetEvent(varIdx)) {
			IntIterator iter= svars[varIdx].getDomain().getKernelIterator();
			while(iter.hasNext()) {
				final int item=iter.next();
				if(! bins[item].isInstantiated()) {
					pack(item,varIdx);
				}
			}
			iter= svars[varIdx].getDomain().getEnveloppeDomain().getDeltaIterator();
			while(iter.hasNext()) {
				final int item=iter.next();
				if(bins[item].getDomain().contains(varIdx)) {
					remove(item, varIdx);
				}
			}

		}else {
			final int item=getItemIndex(varIdx);
			if(item>=0) {
				final int b = bins[item].getVal();
				svars[b].addToKernel(item, set_cIndices[b]);
				checkDeltaDomain(item);
			}
		}
		constAwake(false);
	}


	@Override
	public void awakeOnKer(int varIdx, int x) throws ContradictionException {
		pack(x,varIdx);
		this.constAwake(false);
	}

	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
		final int item = getItemIndex(varIdx);
		//remove from associated enveloppe
		if(item>=0) {
			svars[val].remFromEnveloppe(item, set_cIndices[val]);
		}
		this.constAwake(false);
	}

	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		awakeOnBounds(varIdx);
	}

	@Override
	public void propagate() throws ContradictionException {
		filtering.propagate();

	}

	@Override
	public boolean isSatisfied() {
		return filtering.availableBins.isEmpty();
	}



	class PackBound extends AbstractNbNonEmptyBound {

		List<Integer> binsLB = new LinkedList<Integer>();

		int[] remainingSpace;

		//Map<Integer,>
		public PackBound() {
			super(loads.length, sizes.length);
			remainingSpace = new int[loads.length];
		}


		@Override
		protected void reset() {
			Arrays.fill(remainingSpace, 0);
			binsLB.clear();
			super.reset();
		}


		@Override
		protected void initialize() {
			for (int b = 0; b < nbBins; b++) {
				if(svars[b].isInstantiated()) {
					if(loads[b].isInstantiatedTo(0)) {nbEmpty++;}
					else {nbFull++;}
				}else {
					binsLB.add(b);
					remainingSpace[b] += loads[b].getSup();
					capacityLB = Math.max(capacityLB, remainingSpace[b]);
					if(svars[b].getKernelDomainSize()>0) {
						nbSome++;
					}
				}
			}
			nbBinsLB = binsLB.size();
		}

		@Override
		protected int setBinItems(int[] dest, int begin) {
			int idx=begin;
			for (int b : binsLB) {
				final int s = capacityLB - remainingSpace[b];
				if(s > 0) {
					dest[idx++] = s;
				}
			}
			return idx;
		}

		@Override
		protected int setUnpackedItems(int[] dest, int begin) {
			int idx=begin;
			for (int i = 0; i < bins.length; i++) {
				if(bins[i].isInstantiated()) {
					if( ! svars[bins[i].getVal()].isInstantiated()) {
						remainingSpace[bins[i].getVal()] -= sizes[i].getVal();
					}
				}else {
					dest[idx++] = sizes[i].getVal();
				}
			}
			return idx;
		}
	}

}
