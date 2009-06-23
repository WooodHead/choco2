package i_want_to_use_this_old_version_of_choco.global.matching;

import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.integer.IntDomainVar;

/**
 * Simple implementation of global cardinality constraint with occurrence constrained by
 * variables and not only integer bounds.
 */
public class GlobalCardinalityVar extends GlobalCardinality {
  //protected StoredIntVector minOccurence;
  //protected StoredIntVector maxOccurence;

  public GlobalCardinalityVar(IntDomainVar[] values,
                              IntDomainVar[] occurences) {
    this(values, 1, occurences.length, occurences);
  }

  public GlobalCardinalityVar(IntDomainVar[] values,
                           int minValue, int maxValue,
                           IntDomainVar[] occurences) {
    super(values, minValue, maxValue, new int[occurences.length], new int[occurences.length]);
    int nbVarsTotal = values.length + occurences.length;
    vars = new IntDomainVar[nbVarsTotal];
    System.arraycopy(values, 0, vars, 0, values.length);
    System.arraycopy(occurences, 0, vars, values.length, occurences.length);
    cIndices = new int[nbVarsTotal];
    //minOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
    //maxOccurence = getProblem().getEnvironment().makeIntVector(occurencesMin.length, -1);
  }


  public void awakeOnInf(int idx) throws ContradictionException {
    if (idx < nbLeftVertices) super.awakeOnInf(idx);
    else {
      checkSumInfs();
      deleteSupport();
      this.constAwake(false);
    }
  }

  private void checkSumInfs() throws ContradictionException {
    int sum = 0;
    for(int j = 0; j < nbRightVertices; j++) {
      sum += vars[j + nbLeftVertices].getInf();
    }
    if (sum > nbLeftVertices) this.fail();
  }

  public void awakeOnRem(int idx, int x) throws ContradictionException {
    if (idx < nbLeftVertices) super.awakeOnRem(idx, x);
    else {
      deleteSupport();
      this.constAwake(false);
    }
  }

  public void awakeOnSup(int idx) throws ContradictionException {
    if (idx < nbLeftVertices) super.awakeOnSup(idx);
    else {
      deleteSupport();
      this.constAwake(false);
    }
  }

  public void awakeOnInst(int idx) throws ContradictionException {
    if (idx < nbLeftVertices) super.awakeOnInst(idx);
    else {
      checkSumInfs();
      deleteSupport();
      this.constAwake(false);
    }
  }

  public void awake() throws ContradictionException {
    super.awake();
  }

  public void deleteSupport() {
    for(int i = 0; i < nbLeftVertices; i++) {
      refMatch.set(i, -1);
    }
    for(int j = 0; j < nbRightVertices; j++) {
      flow.set(j, 0);
    }
    matchingSize.set(0);
  }

  protected int getMinFlow(int j) {
    return vars[nbLeftVertices + j].getInf();
  }

  protected int getMaxFlow(int j) {
    return vars[nbLeftVertices + j].getSup();
  }

 public boolean isSatisfied(int[] tuple) {
    int[] occurrences = new int[this.maxValue - this.minValue + 1];
    int nbvar = tuple.length - occurrences.length;
	for (int i = 0; i < nbvar; i++) {
      occurrences[tuple[i]-this.minValue]++;
    }
    for (int i = 0; i < occurrences.length; i++) {
      int occurrence = occurrences[i];
      if (tuple[i + nbvar] != occurrence)
        return false;
    }
    return true;
  }
}