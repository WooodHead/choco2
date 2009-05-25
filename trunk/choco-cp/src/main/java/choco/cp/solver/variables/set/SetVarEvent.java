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
package choco.cp.solver.variables.set;

import choco.kernel.common.util.IntIterator;
import choco.kernel.memory.PartiallyStoredIntVector;
import choco.kernel.memory.PartiallyStoredVector;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.set.SetSConstraint;
import choco.kernel.solver.propagation.VarEvent;
import choco.kernel.solver.variables.set.SetVar;

import java.util.logging.Level;

/*
 * Created by IntelliJ IDEA.
 * User: Hadrien
 * Date: 6 juin 2004
 * Since : Choco 2.0.0
 *
 */
public class SetVarEvent extends VarEvent<SetVarImpl> {

  /**
   * Constants for the <i>eventType</i> bitvector: index of bit for events on SetVars
   */
  public static final int REMENV = 0;
  public static final int ADDKER = 1;
  public static final int INSTSET = 2;

  public static final int ENVEVENT = 1;
  public static final int KEREVENT = 2;
  public static final int BOUNDSEVENT = 3;
  public static final int INSTSETEVENT = 4;

  public SetVarEvent(SetVarImpl var) {
    super(var);
    eventType = EMPTYEVENT;
  }

  /**
   * useful for debugging
   */
  public String toString() {
    return ("VarEvt(" + modifiedVar.toString() + ")[" + eventType + ":"
        + ((eventType & ENVEVENT) != 0 ? "E" : "")
        + ((eventType & KEREVENT) != 0 ? "K" : "")
        + ((eventType & INSTSETEVENT) != 0 ? "X" : "")
        + "]");
  }

  /**
   * Clears the var: delegates to the basic events.
   */
  public void clear() {
    this.eventType = EMPTYEVENT;
    (modifiedVar.getDomain()).getEnveloppeDomain().clearDeltaDomain();
    (modifiedVar.getDomain()).getKernelDomain().clearDeltaDomain();
  }


  protected void freeze() {
    (modifiedVar.getDomain()).getEnveloppeDomain().freezeDeltaDomain();
    (modifiedVar.getDomain()).getKernelDomain().freezeDeltaDomain();
    cause = NOEVENT;
    eventType = 0;
  }

  protected boolean release() {
    return modifiedVar.getDomain().getEnveloppeDomain().releaseDeltaDomain() &&
        modifiedVar.getDomain().getKernelDomain().releaseDeltaDomain();
  }

  public boolean getReleased() {
    return (modifiedVar.getDomain()).getEnveloppeDomain().getReleasedDeltaDomain() &&
        (modifiedVar.getDomain()).getKernelDomain().getReleasedDeltaDomain();
  }

  public IntIterator getEnvEventIterator() {
    return ( modifiedVar.getDomain()).getEnveloppeDomain().getDeltaIterator();
  }

  public IntIterator getKerEventIterator() {
    return (modifiedVar.getDomain()).getKernelDomain().getDeltaIterator();
  }

  /**
   * Propagates the event through calls to the propagation engine.
   *
   * @return true if the event has been fully propagated (and can thus be discarded), false otherwise
   * @throws choco.kernel.solver.ContradictionException
   */
  @Override
public boolean propagateEvent() throws ContradictionException {
	  if(LOGGER.isLoggable(Level.FINER)) {LOGGER.log(Level.FINER, "propagate {0}", this);}
    // first, mark event
    int evtType = eventType;
    int evtCause = cause;
    freeze();

    if (evtType >= INSTSETEVENT)
      propagateInstEvent(evtCause);
    else if (evtType <= BOUNDSEVENT) {
      if (evtType == ENVEVENT)
        propagateEnveloppeEvents(evtCause);
      else if (evtType == KEREVENT)
        propagateKernelEvents(evtCause);
      else if (evtType == BOUNDSEVENT) {
        propagateKernelEvents(evtCause);
        propagateEnveloppeEvents(evtCause);
      }
    }

    // last, release event
    return release();
  }

  /**
   * Propagates the instantiation event
   */
  public void propagateInstEvent(int evtCause) throws ContradictionException {
    SetVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        SetSConstraint c = (SetSConstraint) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnInst(i);
        }
      }
    }
  }

  /**
   * Propagates a set of value removals
   */
  public void propagateKernelEvents(int evtCause) throws ContradictionException {
    SetVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        SetSConstraint c = (SetSConstraint) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnkerAdditions(i, this.getKerEventIterator());
        }
      }
    }
  }

  /**
   * Propagates a set of value removals
   */
  public void propagateEnveloppeEvents(int evtCause) throws ContradictionException {
    SetVar v = getModifiedVar();
    PartiallyStoredVector constraints = v.getConstraintVector();
    PartiallyStoredIntVector indices = v.getIndexVector();

    for (IntIterator cit = constraints.getIndexIterator(); cit.hasNext();) {
      int idx = cit.next();
      if (idx != evtCause) {
        SetSConstraint c = (SetSConstraint) constraints.get(idx);
        if (c.isActive()) {
          int i = indices.get(idx);
          c.awakeOnEnvRemovals(i, this.getEnvEventIterator());
        }
      }
    }
  }

}
