//     ___  ___         PaLM library for Choco system
//    /__ \/ __\        (c) 2001 - 2004 -- Narendra Jussien
//   ____\ |/_____
//  /_____\/_____ \     PalmExplanation based constraint programming tool
// |/   (_)(_)   \|
//       \ /            Version 0.1
//       \ /            January 2004
//       \ /
// ______\_/_______     Contibutors: Fran�ois Laburthe, Hadrien Cambazard, Guillaume Rochart...

package choco.ecp.solver.explanations.dbt;

import choco.kernel.solver.constraints.SConstraint;

public class PalmControlConstraint {
  /**
   * The controlling constraint.
   */

  SConstraint constraint;


  /**
   * The index of the controlled contraint in the controlling one.
   */

  int index;


  /**
   * Creates a control constraint whit the specified constraint as controlling one.
   *
   * @param constraint The controlling constraint.
   * @param index      The index of the controlled one.
   */

  public PalmControlConstraint(SConstraint constraint, int index) {
    this.constraint = constraint;
    this.index = index;
  }


  /**
   * Gets the controlling constraint.
   */

  public SConstraint getConstraint() {
    return constraint;
  }


  /**
   * Gets the index of the controlled constraint in the controlling one.
   */

  public int getIndex() {
    return index;
  }
}