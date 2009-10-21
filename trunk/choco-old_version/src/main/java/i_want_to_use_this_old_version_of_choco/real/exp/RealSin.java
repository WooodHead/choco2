package i_want_to_use_this_old_version_of_choco.real.exp;

import i_want_to_use_this_old_version_of_choco.AbstractProblem;
import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.real.RealExp;
import i_want_to_use_this_old_version_of_choco.real.RealInterval;
import i_want_to_use_this_old_version_of_choco.real.RealMath;

/**
 * Expression evaluatiing the cos of its only one sub-expression.
 */
public class RealSin extends AbstractRealUnTerm {
  public RealSin(AbstractProblem pb, RealExp exp1) {
    super(pb, exp1);
  }

   public String pretty() {
    return "sin(" + exp1.pretty() + ")";
  }

  public void tighten() {
    RealInterval res = RealMath.sin(exp1);
    inf.set(res.getInf());
    sup.set(res.getSup());
  }

  public void project() throws ContradictionException {
    RealInterval res = RealMath.asin_wrt(this, exp1);
    if (res.getInf() > res.getSup()) {
      problem.getPropagationEngine().raiseContradiction();
    }
    exp1.intersect(res);
  }
}