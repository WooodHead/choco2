//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.constraints.real;

import choco.ecp.solver.PalmSolver;
import choco.ecp.solver.constraints.real.exp.PalmRealIntervalConstant;
import choco.ecp.solver.explanations.Explanation;
import choco.ecp.solver.explanations.dbt.PalmConstraintPlugin;
import choco.ecp.solver.explanations.dbt.PalmExplanation;
import choco.ecp.solver.search.dbt.DecisionSConstraint;
import choco.ecp.solver.variables.real.PalmRealDomain;
import choco.ecp.solver.variables.real.PalmRealVar;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.real.RealInterval;
import choco.kernel.solver.variables.real.RealVar;

/**
 * An abstract implementation of a split constraint used for search algorithm.
 * It is specialized for left split or right split (that lower half of the current
 * interval or upper half).
 */
public abstract class AbstractPalmSplit extends AbstractPalmUnRealConstraint
    implements DecisionSConstraint {
  /**
   * The previous value of the variable.
   */
  protected RealInterval previous;

  /**
   * The propagated interval value (instantiated on creation on the specialized
   * class).
   */
  protected RealInterval current;

  /**
   * Asbtract constructor: stores the variable, the previous value of this variable,
   * and creates the PaLM plug-in.
   */
  public AbstractPalmSplit(RealVar var, RealInterval interval) {
      super(var);
    this.v0 = var;
    previous = interval;
    this.hook = new PalmConstraintPlugin(this);
    ((PalmConstraintPlugin) this.hook).setEphemeral(true);
  }

  public Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  public void addListener(boolean dynamicAddition) {
    super.addListener(dynamicAddition);
    ((PalmRealDomain) v0.getDomain()).addDecisionConstraint(this);
  }

  /**
   * Returns this constraint as string: the name of the variable and the
   * affected value.
   */
  public String toString() {
    return "Split Constraint: " + v0 + " in " + current + ".";
  }

  /**
   * First propagation of the constraint. Since we are now sure that the constraint
   * is posted, the constraint is added to the explanation of the interval the
   * variable must be included in. It allows to include this constraint in all
   * withdrawals deduced by this constraint.
   *
   * @throws ContradictionException
   */
  public void awake() throws ContradictionException {
    Explanation expl =
        ((PalmSolver) this.getProblem()).makeExplanation();
    ((PalmConstraintPlugin) this.getPlugIn()).self_explain(expl);
    current = new PalmRealIntervalConstant(current.getInf(), current.getSup(),
        (PalmExplanation) expl.copy(), expl);
    propagate();
  }

  /**
   * Generic propagation of the constraint. The variable is reduced to the
   * intersection with the interval it should be included in.
   *
   * @throws ContradictionException
   */
  public void propagate() throws ContradictionException {
    ((PalmRealVar) v0).intersect(current, cIdx0);
  }

  /**
   * Awakes on lower bound (does nothing).
   *
   * @throws ContradictionException
   */
  public void awakeOnInf(int idx) throws ContradictionException {
  }

  /**
   * Awakes on upper bound (does nothing).
   *
   * @throws ContradictionException
   */
  public void awakeOnSup(int idx) throws ContradictionException {
  }

  /**
   * On lower bound restoration, launches the generic propagation.
   *
   * @throws ContradictionException
   */
  public void awakeOnRestoreInf(int idx) throws ContradictionException {
    this.propagate();
  }

  /**
   * On upper bound restoration, launches the generic propagation.
   *
   * @throws ContradictionException
   */
  public void awakeOnRestoreSup(int idx) throws ContradictionException {
    this.propagate();
  }

  public void takeIntoAccountStatusChange(int index) { // TODO
  }

  /**
   * Checks if the constraint is satisfied (should be called when completely
   * instantiated).
   */
  public boolean isSatisfied() {
    return isConsistent();
  }

  /**
   * Checks if the constraint is satisfied.
   */
  public boolean isConsistent() {
    return v0.getInf() > current.getInf() && v0.getSup() < current.getSup();
  }
}