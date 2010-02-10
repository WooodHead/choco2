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
package choco.kernel.memory;

import choco.IGarbageCollectorAssistant;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.memory.structure.IndexedObject;
import choco.kernel.memory.structure.IntInterval;
import choco.kernel.memory.structure.PartiallyStoredIntVector;
import choco.kernel.memory.structure.PartiallyStoredVector;

import java.util.ArrayList;
import java.util.logging.Logger;

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
public interface IEnvironment extends IGarbageCollectorAssistant {
   
	/**
	 * Reference to an object for logging trace statements related memory & backtrack (using the java.util.logging package)
	 */
	final static Logger LOGGER = ChocoLogging.getEngineLogger();
	
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredBoolTrail} for storing booleans.
//     */
//
//    final int BOOL_TRAIL = 0;
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredIntTrail} for storing integers.
//     */
//
//    final int INT_TRAIL = 1;
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredVectorTrail} for storing vectors.
//     */
//
//    final int VECTOR_TRAIL = 2;
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredIntVectorTrail} for storing
//     * integer vectors.
//     *
//     */
//
//    final int INT_VECTOR_TRAIL = 3;
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredDoubleTrail} for storing
//     * integer vectors.
//     *
//     */
//
//    final int FLOAT_TRAIL = 4;
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.trail.StoredLongTrail} for storing
//     * integer vectors.
//     *
//     */
//
//    final int LONG_TRAIL = 5;
//
//
//    /**
//     * Index of the {@link choco.kernel.memory.trailing.StoredBinaryTree} for storing
//     * integer vectors.
//     *
//     */
//    final int BTREE_TRAIL = 6;


    /**
     * Returns the world number.
     *
     * @return current world index
     */

    int getWorldIndex();

    /**
     * Starts a new branch in the search tree.
     */

    void worldPush();

    /**
     * Backtracks to the previous choice point in the search tree.
     */
    void worldPop();


    /**
	 * Comitting the current world: merging it with the previous one.
	 * <p/>
	 * Not used yet.
	 */
    void worldCommit();


//    /**
//     *  Record the last branching choice in the Environment.
//     */
//
//    void pushContext(IntBranchingTrace ctx);
//
//    /**
//     *  Attaches a given search solver to this Environment
//     */
//
//    void setSearchSolver(AbstractGlobalSearchSolver solver);
//
//    /**
//     *  Record the last world index where the search failed
//     */
//
//    void setLastFail(int worldIndex);
//
//    /**
//     *  For recomputation, get how many times worlds have been saved
//     */
//
//    int getHowManySaves();
//
//    /**
//     *  Return the index of the last saved world
//     */
//
//    int getLastSavedWorldIndex();
//



    /**
     * Factory pattern: new IStateInt objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt();

    /**
     * Factory pattern: new IStateInt objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable integer
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt(int initialValue);

    /**
     * Factory pattern : new IntInterval objects are created by the environment
     * @param lowB intitial lower bound
     * @param upB intial upper bound
     * @return new IntInterval computed by the environment
     */
    IntInterval makeIntInterval(int lowB, int upB);

    /**
     * Factory pattern : new IStateInt with procedure objects are created by the environment
     * @param procedure the procedure to apply
     * @param initialValue the intial value of the integer
     * @return IStateInt with procedure
     */
    IStateInt makeIntProcedure(IStateIntProcedure procedure, int initialValue);

    /**
     * Factory pattern: new IStateBool objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable boolean
     * @return Boolean object created by the environment
     */

    IStateBool makeBool(boolean initialValue);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector();

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector(int size, int initialValue);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param entries an array to be copied as set of initial contents of the vector
     * @return IStateIntVector
     */

    IStateIntVector makeIntVector(int[] entries);

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector();

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector(int size, double initialValue);

    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment
     *
     * @param entries an array to be copied as set of initial contents of the vector
     * @return IStateDoubleVector
     */

    IStateDoubleVector makeDoubleVector(double[] entries);

    /**
     * Factory pattern: new IStateVector objects are created by the environment.
     * Creates an empty vector
     * @return IStateIntVector
     */
    <T> IStateVector<T> makeVector();

    /**
     * Factory pattern : create a new partially stored vector via the environment.
     * @param <T> object to store
     * @return PartiallyStoredVector
     */
    <T> PartiallyStoredVector<T> makePartiallyStoredVector();

    /**
     * Factory pattern : create a new partially stored int vector via the environment.
     * @return PartiallyStoredVector
     */
    PartiallyStoredIntVector makePartiallyStoredIntVector();

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initail size of the IStateBitSet
     * @return IStateBitSet
     */
    IStateBitSet makeBitSet(int size);


    /**
     * Build a shared bipartite set
     * @param size size of the bi partite set
     */
    void createSharedBipartiteSet(int size);

    /**
     * Factory pattern : shared IStateIntVector object is return by the environment
     * @return IStateIntVector
     */
    IStateIntVector getSharedBipartiteSetForBooleanVars();

    /**
     * Increase the size of the shared bi partite set,
     * it HAS to be called before the end of the environment creation
     * BEWARE: be sure you are correctly calling this method
     * @param gap the gap the reach the expected size
     */
    void increaseSizeOfSharedBipartiteSet(int gap);

    /**
     * Return the next free bit in the shared StoredBitSetVector object
     * @return
     */
    int getNextOffset();

    IStateIntVector makeBipartiteIntList(int[] entries);

    IStateIntVector makeBipartiteSet(int[] entries);

    IStateIntVector makeBipartiteSet(int nbEntries);

    IStateIntVector makeBipartiteSet(IndexedObject[] entries);

    IStateIntVector makeBipartiteSet(ArrayList<IndexedObject> entries);

    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     */

    IStateDouble makeFloat();

    public IStateLong makeLong();

    public IStateLong makeLong(int init);


    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable search
     */

    IStateDouble makeFloat(double initialValue);



    IStateBinaryTree makeBinaryTree(int inf, int sup);

    IStateObject makeObject(Object obj);

}