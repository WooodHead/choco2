package i_want_to_use_this_old_version_of_choco.integer;
/* ************************************************
 *           _       _                            *
 *          |  °(..)  |                           *
 *          |_  J||L _|       Choco-Solver.net    *
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
 *     + website : http://choco-solver.net        *
 *     + support : support@chocosolver.net        *
 *                                                *
 *     Copyright (C) F. Laburthe,                 *
 *                    N. Jussien   1999-2008      *
 **************************************************/
import i_want_to_use_this_old_version_of_choco.AbstractProblem;
import i_want_to_use_this_old_version_of_choco.ContradictionException;
import i_want_to_use_this_old_version_of_choco.integer.var.IntDomain;
/** History:
 * 2007-12-07 : FR_1873619 CPRU: DomOverDeg+DomOverWDeg
 * */
/**
 * An interface for all implementations of search valued domain variables
 */
public interface IntDomainVar extends IntVar {

  /**
   * <b>Public user API:</b>
   * static constants associated to the encoding of the variable domain
   * these constants are passed as parameters to the constructor of IntVars
   * BITSET = a chained list of values
   */
  public static int BITSET = 0;

  /**
   * <b>Public user API:</b>
   * static constants associated to the encoding of the variable domain
   * these constants are passed as parameters to the constructor of IntVars
   * BOUNDS = an interval (keeping the lower and upper bounds)
   */
  public static int BOUNDS = 1;

  /**
   * Static constant to create integer variables domain implemented with
   * linked list of values in the domain
   */
  public static int LINKEDLIST = 2;

  public static int INTERVALLIST = 3;  

    /**
     * <b>Public user API:</b>
     * 2007-12-07 FR_1873619 CPRU: DomOverDeg+DomOverWDeg
     */
    public static int WEIGHT = 1;


  /**
   * <b>Public user API:</b>
   * <i>Propagation events</i> assigning a value to a variable
   * (ie: removing all other values from its domain).
   *
   * @param x the value that is assigned to the variable
   * @throws ContradictionException contradiction exception
   */

  public void setVal(int x) throws ContradictionException;


  /**
   * <b>Public user API:</b>
   * <i>Propagation events</i> removing a value from the domain of a variable.
   *
   * @param x the removed value
   * @throws ContradictionException contradiction exception
   */

  public void remVal(int x) throws ContradictionException;


  /**
   * <b>Public user API:</b>
   * <i>Propagation events</i> updating the lower bound of a variable
   * (ie: removing all value strictly below the new lower bound from the domain).
   *
   * @param x the new lower bound
   * @throws ContradictionException contradiction exception
   */

  public void setInf(int x) throws ContradictionException;

  /**
   * @deprecated replaced by setInf
   * @param x the new inf value
   * @throws ContradictionException contradiction exception
   */
  public void setMin(int x) throws ContradictionException;

  /**
   * <b>Public user API:</b>
   * <i>Propagation events</i> updating the upper bound of a variable
   * (ie: removing all value strictly above the new upper bound from the domain).
   *
   * @param x the new upper bound
   * @throws ContradictionException contradiction exception
   */

  public void setSup(int x) throws ContradictionException;

  /**
   * @deprecated replaced by setMax
   * @param x the new max value
   * @throws ContradictionException  contradiction exception
   */
  public void setMax(int x) throws ContradictionException;

  /**
   * <b>Public user API:</b>
   * <i>Propagation events</i> wiping out the domain of the variable (removing all values)
   * and throwing a contradiction
   *
   * @throws ContradictionException contradiction exception
   */

  public void wipeOut() throws ContradictionException;


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> whether an enumeration of values (in addition to the enclosing interval) is stored
   * @return wether an enumeration of values is stored
   */

  public boolean hasEnumeratedDomain();


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> whether the domain is a 0/1 domain
   * @return wether the domain is a 0/1 domain
   */

  public boolean hasBooleanDomain();

  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> returns the object responsible for storing the enumeration of values in the domain
   * @return the objects responsible for storing the enumeration of values in the domain
   */

  public IntDomain getDomain();

  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> testing whether the value of an instantiated variable
   * is equal to a specific value.
   * @param x the tested value
   * @return wether the value of an instantiated variables is equal to a x.
   */
  public boolean isInstantiatedTo(int x);

  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> testing whether a value is in the domain.
   *
   * @param x the tested value
   * @return wether a value is in the domain 
   */

  public boolean canBeInstantiatedTo(int x);


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> testing whether two variables have intersecting domains.
   *
   * @param x the other variable
   * @return wether two variables have intersecting domains
   */

  public boolean canBeEqualTo(IntDomainVar x);


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> testing whether one value among a list is present in
   * the domain.
   *
   * @param sortedValList the list of values. Must be sorted in increasing order.
   * @param nVals         the size of the list of values
   * @return wether one value among a list is present in the domain
   */

  public boolean canBeInstantiatedIn(int[] sortedValList, int nVals);


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> retrieves a value drawn at random (uniform distribution)
   * from the domain.
   * @return a value drawn at random from the domain
   */

  public int getRandomDomainValue();


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> retrieves the value immediately (but strictly) after
   * <i>i</i> in the domain
   *
   * @param i the pivot value. May or may not be in the domain
   * @return the value immediatly after the domain
   */

  public int getNextDomainValue(int i);


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> retrieves the value immediately (but strictly) before
   * <i>i</i> in the domain.
   *
   * @param i the pivot value. May or may not be in the domain
   * @return the value immediatly before the domain
   */

  public int getPrevDomainValue(int i);


  /**
   * <b>Public user API:</b>
   * <i>Domains :</i> retrieves the number of values in the domain.
   * @return the number of values in the domain
   */

  public int getDomainSize();


  /**
   * <b>Public user API:</b>
   * <i>Util :</i> retrieves the problem to which the variable belongs.
   * @return the problem to which the variable belongs
   */

  public AbstractProblem getProblem();


  /**
   * Returns the lower bound of the variable domain (e.g. the smallest value that the variable can be assigned).
   *
   * @return the domain lower bound
   */

  public int getInf();


  /**
   * Returns the upper bound of the variable domain (e.g. the greatest value that the variable can be assigned).
   *
   * @return the domain upper bound
   */

  public int getSup();


  /**
   * Returns the value of the variable if instantiated.
   *
   * @return the value of the variable
   */

  public int getVal();

  /**
   * @deprecated replaced by getVal
   * @return the value of the variable if known
   */
  public int getValue();

  /**
   * <i>Propagation events</i> updating the lower bound of a variable
   * (ie: removing all value strictly below the new lower bound from the domain).
   *
   * @param x   a lower bound of the domain (the new one, if better than the one currently stored)
   * @param idx the index of the constraint that generated the var
   * @return a boolean indicating whether this method call added new information or not
   * @throws ContradictionException contradiction exception
   */

  public boolean updateInf(int x, int idx) throws ContradictionException;


  /**
   * <i>Propagation events</i> updating the upper bound of a variable
   * (ie: removing all value strictly above the new upper bound from the domain).
   *
   * @param x   an upper bound of the domain (the new one, if better than the one currently stored)
   * @param idx the index of the constraint that generated the var
   * @return a boolean indicating whether this method call added new information or not
   * @throws ContradictionException contradiction exception
   */

  public boolean updateSup(int x, int idx) throws ContradictionException;

  /**
   * <i>Propagation events</i> updating the domain of a variable (by removing a value)
   *
   * @param x   the value that is not in the domain
   * @param idx the index of the constraint that generated the var
   * @return a boolean indicating whether this method call added new information or not
   * @throws ContradictionException contradiction exception
   */

  public boolean removeVal(int x, int idx) throws ContradictionException;

  /**
   * <i>Propagation events</i> updating the domain of a variable
   * (by removing an interval, ie, a sequence of consecutive values)
   *
   * @param a   the lower bound of the forbidden interval
   * @param b   the upper bound of the forbidden interval
   * @param idx the index of the constraint that generated the var
   * @return a boolean indicating whether this method call added new information or not
   * @throws ContradictionException contradiction exception
   */

  public boolean removeInterval(int a, int b, int idx) throws ContradictionException;

  /**
   * <i>Propagation events</i> instantiating a variable
   * (ie: removing all other values from the domain)
   *
   * @param x   the value of the variable
   * @param idx the index of the constraint that generated the var
   * @return a boolean indicating whether this method call added new information or not
   * @throws ContradictionException contradiction exception
   */

  public boolean instantiate(int x, int idx) throws ContradictionException;

}