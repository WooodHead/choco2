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
package samples.tutorials.seminar.socialgolfer;

import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.ComponentConstraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;

import java.util.Arrays;
import java.util.logging.Logger;

import static choco.Choco.*;

/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 2 juin 2008
 * Since : Choco 2.0.0
 *
 */
public class ExSocialGolfer {
    protected final static Logger LOGGER = ChocoLogging.getMainLogger();

    public int w; // number of weeks
    public int g; // number of groups
    public int s; // size of the groups

    public ExSocialGolfer(int w, int g, int s) {
        this.w = w;
        this.g = g;
        this.s = s;
    }

    public int[] getOneMatrix(int n) {
        int[] mat = new int[n];
        Arrays.fill(mat, 1);
        return mat;
    }

    public void booleanSocialGofler() {
        Model m = new CPModel();

        int numplayers = g * s;
        IntegerVariable[][][] golfmat = new IntegerVariable[g][w][numplayers];
        // golfmat[i][j][k] : est ce que le joueur numéro k joue semaine j dans le groupe i ?
        for (int i = 0; i < g; i++) {
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < numplayers; k++) {
                    golfmat[i][j][k] = makeIntVar("(" + i + "_" + j + "_" + k + ")", 0, 1);
                }
            }
        }
        //every golfer plays once in every week
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < numplayers; j++) {
                IntegerVariable[] vars = new IntegerVariable[g];
                for (int k = 0; k < g; k++) {
                    vars[k] = golfmat[k][i][j];
                }
                m.addConstraint(eq(scalar(vars, getOneMatrix(g)), 1)); // tout golfer doit être placé dans un groupe
            }
        }

        //every group is of size s
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < g; j++) {
                IntegerVariable[] vars = new IntegerVariable[numplayers];
                System.arraycopy(golfmat[j][i], 0, vars, 0, numplayers);
                m.addConstraint(eq(scalar(vars, getOneMatrix(numplayers)), s));
            }
        }

        // each pair of players only meet once
        // Efficient way : use of a ScalarAtMost
        for (int i = 0; i < numplayers; i++) {
            for (int j = i + 1; j < numplayers; j++) {
                IntegerVariable[] vars = new IntegerVariable[w * g * 2];
                int cpt = 0;
                for (int k = 0; k < w; k++) {
                    for (int l = 0; l < g; l++) {
                        vars[cpt] = golfmat[l][k][i];
                        vars[cpt + w * g] = golfmat[l][k][j];
                        cpt++;
                    }
                }
                m.addConstraint(new ComponentConstraint(ScalarAtMost.ScalarAtMostManager.class, new int[]{w * g, 1}, vars));
            }
        }

        // break symetries among weeks
        // enforce a lexicgraphic ordering between any pairs of week
        for (int i = 0; i < w; i++) {
            for (int j = i + 1; j < w; j++) {
                IntegerVariable[] vars1 = new IntegerVariable[numplayers * g];
                IntegerVariable[] vars2 = new IntegerVariable[numplayers * g];
                int cpt = 0;
                for (int k = 0; k < numplayers; k++) {
                    for (int l = 0; l < g; l++) {
                        vars1[cpt] = golfmat[l][i][k];
                        vars2[cpt] = golfmat[l][j][k];
                        cpt++;
                    }
                }
                m.addConstraint(lex(vars1, vars2));
            }
        }

        // break symetries among groups
        for (int i = 0; i < numplayers; i++) {
            for (int j = i + 1; j < numplayers; j++) {
                IntegerVariable[] vars1 = new IntegerVariable[w * g];
                IntegerVariable[] vars2 = new IntegerVariable[w * g];
                int cpt = 0;
                for (int k = 0; k < w; k++) {
                    for (int l = 0; l < g; l++) {
                        vars1[cpt] = golfmat[l][k][i];
                        vars2[cpt] = golfmat[l][k][j];
                        cpt++;
                    }
                }
                m.addConstraint(lex(vars1, vars2));
            }
        }

        // break symetries among players
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < g; j++) {
                for (int p = j + 1; p < g; p++) {
                    IntegerVariable[] vars1 = new IntegerVariable[numplayers];
                    IntegerVariable[] vars2 = new IntegerVariable[numplayers];
                    int cpt = 0;
                    for (int k = 0; k < numplayers; k++) {
                        vars1[cpt] = golfmat[j][i][k];
                        vars2[cpt] = golfmat[p][i][k];
                        cpt++;
                    }
                    m.addConstraint(lex(vars1, vars2));
                }
            }
        }

        // gather branching variables
        IntegerVariable[] staticvars = new IntegerVariable[g * w * numplayers];
        int cpt = 0;
        for (int i = 0; i < numplayers; i++) {
            for (int j = 0; j < w; j++) {
                for (int k = 0; k < g; k++) {
                    staticvars[cpt] = golfmat[k][j][i];
                    cpt++;
                }
            }
        }

        Solver s = new CPSolver();
        s.read(m);
        s.setVarIntSelector(new StaticVarOrder(s, s.getVar(staticvars)));

        s.setTimeLimit(120000);

        s.solve();
        if (s.isFeasible() == Boolean.TRUE) printSol(golfmat, s);
    }

    public void printSol(IntegerVariable[][][] gvars, Solver solver) {
        for (int i = 0; i < w; i++) {
            String semi = "";
            for (int j = 0; j < g; j++) {
                String gj = "(-";
                for (int k = 0; k < g * s; k++) {
                    if (solver.getVar(gvars[j][i][k]).isInstantiatedTo(1)) gj += k + "-";
                }
                semi += gj + ") ";
            }
            LOGGER.info("" + semi);
        }
    }

    public static void main(String[] args) {
        ExSocialGolfer e = new ExSocialGolfer(11, 6, 2);
        e.booleanSocialGofler();
    }
}
