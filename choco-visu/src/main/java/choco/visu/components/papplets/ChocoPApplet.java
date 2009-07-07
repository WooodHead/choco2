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
/*
 * Created by IntelliJ IDEA.
 * User: charles
 * Date: 12 nov. 2008
 * Since : Choco 2.0.1
 */

public enum ChocoPApplet {

    COLORORVALUE("choco.visu.components.papplets.ColorValuePApplet"),
    DOTTYTREESEARCH("choco.visu.components.papplets.DottyTreeSearchPApplet"),
    FULLDOMAIN("choco.visu.components.papplets.FullDomainPApplet"),
    GRID("choco.visu.components.papplets.GridPApplet"),
    NAMEORQUESTIONMARK("choco.visu.components.papplets.NameOrQuestionMarkPApplet"),
    NAMEORVALUE("choco.visu.components.papplets.NameOrValuePApplet"),
    SUDOKU("choco.visu.components.papplets.SudokuPApplet"),
    TREESEARCH("choco.visu.components.papplets.TreeSearchPApplet"),
    ;

    public final String path;

    ChocoPApplet(String p){
        path = p;
    }
}