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
package choco.visu.components.papplets;

import choco.kernel.solver.variables.Var;
import choco.kernel.visu.components.IVisuVariable;
import static choco.visu.components.ColorConstant.BLACK;
import static choco.visu.components.ColorConstant.WHITE;
import choco.visu.components.bricks.AChocoBrick;
import choco.visu.components.bricks.HazardOrValueBrick;

import java.awt.*;
import java.util.ArrayList;
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 31 oct. 2008
 * Since : Choco 2.0.1
 *
 * {@code SudokuPApplet} is the {@code AChocoPApplet} that represents a sudoku grid where variables values are printed
 * inside a cell.
 *
 * Powered by Processing    (http://processing.org/)
 */

public final class SudokuPApplet extends AChocoPApplet{

    private final int size = 25;

    public SudokuPApplet(final Object parameters) {
        super(parameters);
    }

    /**
     * Initialize the ChocoPApplet with the list of concerning VisuVariables
     *
     * @param list of visu variables o watch
     */
    public final void initialize(final ArrayList<IVisuVariable> list) {
        final Var[] vars = new Var[list.size()];
        for(int i = 0; i < list.size(); i++){
            vars[i] = list.get(i).getSolverVar();
        }
        bricks = new AChocoBrick[list.size()];
        for(int i = 0; i < list.size(); i++){
            IVisuVariable vv = list.get(i);
            Var v = vv.getSolverVar();
            bricks[i] = new HazardOrValueBrick(this, v, AChocoBrick.LEFT);
            vv.addBrick(bricks[i]);
        }
        this.init();
    }

    /**
     * Return the ideal dimension of the chopapplet
     *
     * @return
     */
    public final Dimension getDimension() {
        return new Dimension(300, 400);
    }

    /**
     * build the specific PApplet.
     * This method is called inside the {@code PApplet#setup()} method.
     */
    public final void build() {
        size(200, 200);
        background(WHITE);
        textFont(font);
        noStroke();
    }

    /**
     * draws the back side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, the sudoku grid is considered as a back side
     */
    public final void drawBackSide() {
        delay(50);
        background(WHITE);
        //crossruling(size);
        for(int x = 0; x < bricks.length; x++){
            int i = x/9;
            int j = x%9;
            stroke(BLACK);
            rect(20 + (i*size)+2*(i/3), 20 + (j*size)+2*(j/3), size, size);
        }
    }

    /**
     * draws the front side of the representation.
     * This method is called inside the {@code PApplet#draw()} method.
     * For exemple, values of cells in a sudoku are considered as a back side
     */
    public final void drawFrontSide() {
        for(int x = 0; x < bricks.length; x++){
            int i = x%9;
            int j = x/9;
            bricks[x].drawBrick(39 + (i*size)+2*(i/3), 29 + (j*size)+2*(j/3), size, size);
        }
    }
}