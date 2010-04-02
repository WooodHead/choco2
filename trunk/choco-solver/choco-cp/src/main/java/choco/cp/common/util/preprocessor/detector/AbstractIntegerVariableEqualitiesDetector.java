/* * * * * * * * * * * * * * * * * * * * * * * * * 
 *          _       _                            *
 *         |   (..)  |                           *
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
 *                  N. Jussien    1999-2010      *
 * * * * * * * * * * * * * * * * * * * * * * * * */
package choco.cp.common.util.preprocessor.detector;

import choco.Choco;
import choco.cp.common.util.preprocessor.AbstractDetector;
import choco.cp.common.util.preprocessor.merger.IntegerVariableMerger;
import choco.cp.model.CPModel;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.kernel.common.util.objects.BooleanSparseMatrix;
import choco.kernel.common.util.objects.ISparseMatrix;
import choco.kernel.common.util.tools.StringUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.constraints.ConstraintType;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.VariableType;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.variables.integer.IntDomainVar;
import gnu.trove.TIntObjectHashMap;

import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * User : cprudhom<br/>
 * Mail : cprudhom(a)emn.fr<br/>
 * Date : 1 avr. 2010br/>
 * Since : Choco 2.1.1<br/>
 * <p/>
 * A class detector to detect equalities between IntegerVariable within a model.
 */
public abstract class AbstractIntegerVariableEqualitiesDetector extends AbstractDetector {


    public AbstractIntegerVariableEqualitiesDetector(final CPModel model) {
        super(model);
    }

    /**
     * Apply the detection defined within the detector.
     */
    @Override
    public void apply() {
        if (AbstractDetector.LOGGER.isLoggable(Level.CONFIG)) {
            AbstractDetector.LOGGER.config("IntegerVariable equalities detection :");
        }
        final ISparseMatrix matrix = analyze();
        if (matrix.getNbElement() > 0) {
            change(matrix);
        }
    }

    /**
     * Analyze the current model and record equality constraints over {@link IntegerVariable} or
     * {@link choco.kernel.model.variables.integer.IntegerConstantVariable}.
     *
     * @return
     */
    private ISparseMatrix analyze(){
        final int nbIntVars = model.getNbIntVars();
        final ISparseMatrix matrix = new BooleanSparseMatrix(nbIntVars);
        final Iterator<Constraint> iteq = model.getConstraintByType(ConstraintType.EQ);
        Constraint c;
        // Run over equalities constraints, and create edges
        while (iteq.hasNext()) {
            c = iteq.next();
            final Variable v1 = c.getVariables()[0];
            final Variable v2 = c.getVariables()[1];
            if (v1.getVariableType() == VariableType.INTEGER
                    && v2.getVariableType() == VariableType.INTEGER) {
                matrix.add(v1.getHook(), v2.getHook());
                action(c);
            }
        }
        return matrix;
    }

    /**
     * Do an action on {@code c}, depending on implementation.
     * @param c
     */
    protected abstract void action(final Constraint c);

    /**
     * Analyze the matrix and run modifications.
     * @param matrix matrix of equalities
     */
    private void change(final ISparseMatrix matrix) {
        final int nbIntVars = model.getNbIntVars();

        matrix.prepare();

        final int[] color = new int[nbIntVars];
        Arrays.fill(color, -1);
        final TIntObjectHashMap<IntegerVariableMerger> domainByColor = new TIntObjectHashMap<IntegerVariableMerger>();

        int nbDiffObject = detect(matrix, nbIntVars, color, domainByColor);
        apply(nbDiffObject, nbIntVars, color, domainByColor);
    }

    /**
     *  Detect different equalities cliques.
     * @param matrix matrix of equalities
     * @param nbIntVars nb of IntegerVariable within the model
     * @param color array of colir, ie nb different variable
     * @param domainByColor list of domain by color.
     * @return nb of different color found
     */
    private int detect(final ISparseMatrix matrix, final int nbIntVars, final int[] color,
                       final TIntObjectHashMap<IntegerVariableMerger> domainByColor) {
        int nb = -1;
        IntegerVariableMerger dtmp = new IntegerVariableMerger();
        final Iterator<Long> it = matrix.iterator();
        while (it.hasNext()) {
            final long v = it.next();
            final int i = (int) (v / nbIntVars);
            final int j = (int) (v % nbIntVars);

            if (color[i] == -1) {
                nb++;
                color[i] = nb;
                domainByColor.put(nb, new IntegerVariableMerger(model.getIntVar(i)));
            }
            final IntegerVariableMerger d = domainByColor.get(color[i]);
            //backup
            dtmp.copy(d);
            if (d.intersection(model.getIntVar(j))) {
                color[j] = color[i];
                domainByColor.put(color[i], d);
            } else {
                add(Choco.eq(model.getIntVar(i), model.getIntVar(j)));
                //rollback
                d.copy(dtmp);
                if (color[j] == -1) {
                    nb++;
                    color[j] = nb;
                    domainByColor.put(nb, new IntegerVariableMerger(model.getIntVar(j)));
                }
            }
        }
        return nb;
    }

    /**
     * Merge equal IntegerVariable into a unique one.
     * @param k number of unique variables
     * @param nbIntVars number of integer variable within the model
     * @param color array of indice of unique variables
     * @param domainByColor domain of unique variables
     */
    protected abstract void apply(final int k, final int nbIntVars, final int[] color,
                                  final TIntObjectHashMap<IntegerVariableMerger> domainByColor);

    /**
     *
     */
    public static final class IntegerVariableEqualitiesSolverDetector extends AbstractIntegerVariableEqualitiesDetector {

        private final PreProcessCPSolver ppsolver;

        public IntegerVariableEqualitiesSolverDetector(final CPModel model, final PreProcessCPSolver solver) {
            super(model);
            ppsolver = solver;
        }


        /**
         * Delete link between Model object and Solver object.
         * @param c constraint to break
         */
        @Override
        protected void action(final Constraint c) {
            ppsolver.setCstr(c, null);
        }

        /**
     * Merge equal IntegerVariable into a unique one.
     * @param k number of unique variables
     * @param nbIntVars number of integer variable within the model
     * @param color array of indice of unique variables
     * @param domainByColor domain of unique variables
     */
        @Override
        protected void apply(final int k, final int nbIntVars, final int[] color, final TIntObjectHashMap<IntegerVariableMerger> domainByColor) {
            final IntDomainVar[] var = new IntDomainVar[k+1];
            IntegerVariableMerger dtmp;
            IntegerVariable vtmp;
            for(int i = 0; i < nbIntVars; i++){
                final int col = color[i];
                if(col !=-1){
                    final IntegerVariable v = model.getIntVar(i);
                    if(var[col] == null){
                        dtmp = domainByColor.get(col);
                        vtmp = dtmp.create();
                        vtmp.addOptions(dtmp.optionsSet);
                        vtmp.findManager(model.properties);
                        var[col] = (IntDomainVar)ppsolver.getMod2Sol().readModelVariable(vtmp);
                    }
                    ppsolver.setVar(v, var[col]);
                }
            }
        }
    }

    /**
     *
     */
    public static final class IntegerVariableEqualitiesModelDetector extends AbstractIntegerVariableEqualitiesDetector {

        public IntegerVariableEqualitiesModelDetector(final CPModel model) {
            super(model);
        }

        /**
         * Store deletion instruction of {@code c}
         * @param c constraint to delete
         */
        @Override
        protected void action(final Constraint c) {
            delete(c);
        }

        /**
     * Merge equal IntegerVariable into a unique one.
     * @param k number of unique variables
     * @param nbIntVars number of integer variable within the model
     * @param color array of indice of unique variables
     * @param domainByColor domain of unique variables
     */
        @Override
        protected void apply(final int k, final int nbIntVars, final int[] color, final TIntObjectHashMap<IntegerVariableMerger> domainByColor) {
            IntegerVariable vtmp;
            IntegerVariableMerger dtmp;
            final IntegerVariable[] var = new IntegerVariable[k + 1];
            for (int i = 0; i < nbIntVars; i++) {
                final int col = color[i];
                if (col != -1) {
                    final IntegerVariable v = model.getIntVar(i);
                    if (var[col] == null) {
                        dtmp = domainByColor.get(col);
                        if (dtmp.values != null) {
                            vtmp = new IntegerVariable(StringUtils.randomName(), dtmp.values);
                        } else {
                            vtmp = new IntegerVariable(StringUtils.randomName(), dtmp.low, dtmp.upp);
                        }
                        vtmp.addOptions(dtmp.optionsSet);
                        var[col] = vtmp;
                        add(vtmp);
                    }
                    replaceBy(v, var[col]);
                    delete(v);
                }
            }
        }
    }
}
