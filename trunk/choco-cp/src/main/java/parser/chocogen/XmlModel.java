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
package parser.chocogen;


import choco.cp.model.CPModel;
import choco.cp.solver.constraints.integer.extension.ValidityChecker;
import choco.cp.solver.preprocessor.PreProcessCPSolver;
import choco.cp.solver.search.integer.valiterator.IncreasingDomain;
import choco.cp.solver.search.integer.valselector.RandomIntValSelector;
import choco.cp.solver.search.integer.varselector.MinDomain;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.solver.Solver;
import parser.absconparseur.tools.InstanceParser;
import parser.absconparseur.tools.SolutionChecker;
import parser.absconparseur.tools.UnsupportedConstraintException;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User:    charles
 * Date:    19 août 2008
 * <p/>
 * A class to provide facilities for loading and solving
 * CSP described in the xml format of the 2008 competition
 */
public class XmlModel {

    public final static Logger LOGGER = ChocoLogging.getParserLogger();

    //heuristics
    private static final int DOMOVERDEG = 0;
    private static final int DOMOVERWDEG = 1;
    private static final int IMPACT = 2;
    private static final int VERSATILE = 3;
    private static final int SIMPLE = 4;
    private static int heuristic = 0; // DOMOVERDEG by default

    private static int seed;

    //algo d'ac : 2001 ou 32 ou 2008
    private static int ac = 32;

    //perform singleton consistency step or not
    private static boolean singleton = false;

    private static boolean ngFromRestart = false;

    //force to restart or not
    private static Boolean forcerestart = null;
    private static int base = 10;
    private static double growth = 1.5d;

    private static int verb = 0; // if O no verb

    //total timelimit in s
    private static int timelimit = 10000; // in sec

    //initialization timelimit (for impact) in ms
    public int initialisationtime = 60000; //60

    public boolean randvalh = false;

    //temporary data
    private Boolean isFeasible = null;
    private int cheuri;
    private int nbnode = 0;
    private int nbback = 0;
    private static long[] time = new long[5];
    private static String[] values;


    public static int getAcAlgo() {
        return ac;
    }

    public static boolean doSingleton() {
        return singleton;
    }

    public void init() {
        time = new long[5];
        isFeasible = null;
        nbback = 0;
        nbnode = 0;
    }

    /**
     * Main method. Check arguments and set up the options
     * accordingly. example of command line :
     * -file mycsp.xml -h 3 -ac 32 -s true -verb 1 -time 30
     *
     * @param args arguments
     * @throws Exception
     */
    public void generate(String[] args) throws Exception {
        //ChocoLogging.setVerbosity(Verbosity.SEARCH);
        HashMap<String, String> options = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i++];
            String val = args[i];
            options.put(arg, val);
        }
        File dossier;
        if (options.containsKey("-file")) {
            dossier = new File(options.get("-file"));
            if (!dossier.exists()) {
                throw new Exception("Unknown file or directory");
            }
        } else {
            throw new Exception("file option -file is missing");
        }
        if (options.containsKey("-h")) {
            heuristic = Integer.parseInt(options.get("-h"));
        } else {
            throw new Exception("heuristic option -h is missing");
        }
        if (options.containsKey("-ac")) {
            ac = Integer.parseInt(options.get("-ac"));
        } else {
            throw new Exception("AC option -ac is missing");
        }
        if (options.containsKey("-s")) {
            singleton = Boolean.parseBoolean(options.get("-s"));
        }
        if (options.containsKey("-time")) {
            timelimit = Integer.parseInt(options.get("-time"));
        }
        if (options.containsKey("-verb")) {
            verb = Integer.parseInt(options.get("-verb"));
        }
        if (options.containsKey("-rest")) {
            forcerestart = Boolean.parseBoolean(options.get("-rest"));
        }
        if (options.containsKey("-rb")) {
            base = Integer.parseInt(options.get("-rb"));
        }
        if (options.containsKey("-rg")) {
            growth = Double.parseDouble(options.get("-rg"));
        }
        if (options.containsKey("-saclim")) {
            initialisationtime = Integer.parseInt(options.get("-saclim")) * 1000;
        }
        if (options.containsKey("-seed")) {
            seed = Integer.parseInt(options.get("-seed"));
        }
        if (options.containsKey("-randval")) {
           randvalh = Boolean.parseBoolean(options.get("-randval"));;
        }
        if (options.containsKey("-ngfres")) {
           ngFromRestart = Boolean.parseBoolean(options.get("-ngfres"));;
        }
        try {
            if (dossier.isFile()) {
                solveFile(dossier);
            } else {
                solveDirectory(dossier);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ChocoLogging.flushLogs();
    }

    /**
     * ei
     * Solve the csp given by file "fichier"
     *
     * @param fichier
     */
    public void solveFile(File fichier) {
        init();
        if (fichier.getName().endsWith(".xml")
                || fichier.getName().endsWith(".xml.bz2")) {
            try {
                InstanceParser parser = load(fichier);
                CPModel model = buildModel(parser);
                PreProcessCPSolver s = solve(model);
                postAnalyze(fichier, parser, s);
            } catch (UnsupportedConstraintException ex) {
                LOGGER.info("s UNSUPPORTED");
                ChocoLogging.flushLogs();
            } catch (Exception e) {
                e.printStackTrace();
            } catch (Error e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Solve all the csps contained in the corresponding directory :
     * dossiers
     *
     * @param dossiers : the directory where instances are stored
     */
    public void solveDirectory(File dossiers) {
        File listingDonneesEntree[] = dossiers.listFiles();
        for (File fichier : listingDonneesEntree) {
            if (fichier.isFile()) {
                solveFile(fichier);
            } else if (fichier.isDirectory()) {
                solveDirectory(fichier);
            }
        }
    }

    /**
     * Parse the xml and return the parser object (Christophe parser) which
     * can be used to access variables, constraints, etc...
     *
     * @param fichier
     * @return A parser object containing the description of the problem
     * @throws Exception
     * @throws Error
     */
    public InstanceParser load(File fichier) throws Exception, Error {
        try {
            if (verb > 0) {
                LOGGER.log(Level.INFO, "========================================================\nTraitement de :{0}", fichier.getName());
            }
            // Parse the xml and get the abscon representation of the problem
            time[0] = System.currentTimeMillis();
            InstanceParser parser = new InstanceParser();
            parser.loadInstance(fichier.getAbsolutePath());
            parser.parse(false);
            time[1] = System.currentTimeMillis();
            return parser;
        } catch (UnsupportedConstraintException ex) {
            throw ex;
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (Error er) {
            er.printStackTrace();
        }
        return null;
    }


    /**
     * Building the Model and solver
     *
     * @param parser
     * @return
     * @throws Exception
     * @throws Error
     */
    public CPModel buildModel(InstanceParser parser) throws Exception, Error {
        boolean forceExp = false; //force all expressions to be handeled by arc consistency
        ChocoLogging.setOnlyParserLogger(Level.INFO);
        CPModel m = new CPModel(parser.getMapOfConstraints().size(), parser.getNbVariables(), 50, 0, 100, 100, 100);
        ChocoFactory chocofact = new ChocoFactory(parser, m);
        chocofact.createVariables();
        chocofact.createRelations();
        chocofact.createConstraints(forceExp);
        time[2] = System.currentTimeMillis();
        return m;
    }


    /**
     * Solving process
     *
     * @param model
     * @return
     * @throws Exception
     * @throws Error
     */
    public PreProcessCPSolver solve(CPModel model) throws Exception, Error {

        PreProcessCPSolver s = new PreProcessCPSolver();
        s.read(model);
        if (verb > 0) {
            LOGGER.info(MessageFormat.format("solve...dim:[nbv:{0}][nbc:{1}][nbconstants:{2}]", s.getNbIntVars(), s.getNbIntConstraints(), s.getNbConstants()));
        }

        time[3] = System.currentTimeMillis();
        s.setTimeLimit(timelimit * 1000);
        s.monitorBackTrackLimit(true);

        if (verb > 1) LOGGER.info(s.pretty());

        isFeasible = true;
        //do the initial propagation to decide to do restarts or not
        if (!s.initialPropagation()) {
            isFeasible = false;
        } else {
            if (randvalh) s.setRandomValueOrdering(seed);
            cheuri = heuristic;
            //set the search
            switch (cheuri) {
                case VERSATILE:
                    isFeasible = s.setVersatile(s, initialisationtime);
                    cheuri = s.getBBSearch().determineHeuristic(s);
                    break;
                case DOMOVERDEG:
                    isFeasible = s.setDomOverDeg(s); break;
                case DOMOVERWDEG:
                    isFeasible = s.setDomOverWeg(s, initialisationtime);
                    //((DomOverWDegBranching) s.tempGoal).setRandomVarTies(seed);
                    break;
                case IMPACT:
                    isFeasible = s.setImpact(s, initialisationtime);
                    //((ImpactBasedBranching) s.tempGoal).setRandomVarTies(seed);
                    break;
                case SIMPLE:
                    s.setVarIntSelector(new MinDomain(s));
                    if (randvalh)
                        s.setValIntSelector(new RandomIntValSelector(seed));
                    else s.setValIntIterator(new IncreasingDomain());
                default:
                    break;
            }
        }
        if (forcerestart != null) {
            if (forcerestart) {
                s.setGeometricRestart(base, growth);
            }
        } else {
            if (s.restartMode) {
                s.setGeometricRestart(10, 1.3);                                
                //s.setGeometricRestart(Math.min(Math.max(s.getNbIntVars(), 200), 400), 1.4d);
            }
        }

        //s.setLoggingMaxDepth(200);
        if (isFeasible && (cheuri == IMPACT || s.rootNodeSingleton(initialisationtime))) {
            if (ngFromRestart && (s.restartMode || forcerestart)) {
            	s.setRecordNogoodFromRestart(true);
            	s.generateSearchStrategy();
                //s.getSearchStrategy().setSearchLoop(new SearchLoopWithNogoodFromRestart(s.getSearchStrategy(), s.getRestartStrategy()));
                s.launch();

            } else s.solve();
//            s.solve();

            isFeasible = s.isFeasible();
            nbnode = s.getNodeCount();
            nbback = s.getBackTrackCount();
        } else {
            isFeasible = false;
        }
        return s;
    }

    /**
     * Output in the standart console a set of statistics on the search
     *
     * @param fichier
     * @param parser
     * @param s
     * @throws Exception
     * @throws Error
     */
    public void postAnalyze(File fichier, InstanceParser parser, PreProcessCPSolver s) throws Exception, Error {
        //CPSolver.flushLogs();
        time[4] = System.currentTimeMillis();
        //LOGGER.info("" + isFeasible);
        //Output in a format for internal competition
        if (isFeasible == Boolean.TRUE
            && ((!checkEverythingIsInstantiated(parser, s))
                  || s.checkSolution(false) !=Boolean.TRUE)) {
            isFeasible = null;
        }
        values = new String[parser.getVariables().length + 1];
        StringBuffer res = new StringBuffer("c ");
        if (isFeasible == null) {
            res.append("TIMEOUT");
            LOGGER.info("s UNKNOWN");
        } else if (!isFeasible) {
            res.append("UNSAT");
            LOGGER.info("s UNSATISFIABLE");
        } else {
//            if (!s.checkSolution(false)) {
//                //Check the solution with choco
//                res.append("WRONGSOL?");
//                LOGGER.info("s UNKNOWN");
//            } else {
            res.append("SAT");
            LOGGER.info("s SATISFIABLE");
            String sol = "v ";
            values[0] = fichier.getPath();
            for (int i = 0; i < parser.getVariables().length; i++) {
                try {
                    values[i + 1] = "" + s.getVar(parser.getVariables()[i].getChocovar()).getVal();
                } catch (NullPointerException e) {
                    values[i + 1] = "" + parser.getVariables()[i].getChocovar().getLowB();
                }
                sol += values[i + 1] + " ";
            }
            LOGGER.info(sol);
            //}
        }
        double rtime = (double) (time[4] - time[0]) / 1000D;
        res.append(" ").append(rtime).append(" TIME     ");
        res.append(" ").append(nbnode).append(" NDS   ");
        res.append(" ").append(time[1] - time[0]).append(" PARSTIME     ");
        res.append(" ").append(time[2] - time[1]).append(" BUILDPB      ");
        res.append(" ").append(time[3] - time[2]).append(" CONFIG       ");
        res.append(" ").append(time[4] - time[3]).append(" RES      ");
        res.append(" ").append(s.restartMode).append(" RESTART      ");
        res.append(" ").append(cheuri).append(" HEURISTIC      ");
        res.append(" ").append(randvalh).append(" RANDVAL      ");
        LOGGER.info("d AC " + ac);
        LOGGER.info("d RUNTIME " + rtime);
        LOGGER.info("d NODES " + nbnode);
        LOGGER.info("d NODES/s " + Math.round((double) nbnode / rtime));
        LOGGER.info("d BACKTRACKS " + nbback);
        LOGGER.info("d BACKTRACKS/s " + Math.round((double) nbback / rtime));
        LOGGER.info("d CHECKS " + ValidityChecker.nbCheck);
        LOGGER.info("d CHECKS/s " + Math.round((double) ValidityChecker.nbCheck / rtime));

        ValidityChecker.nbCheck = 0;

        LOGGER.info("" + res);
        if (verb > 0) {
            if (isFeasible == Boolean.TRUE) {
                SolutionChecker.main(values);
            }
        }
        ChocoLogging.flushLogs();
    }

    public long getParseTime() {
        return (time[1] - time[0]);
    }

    public long getBuildTime() {
        return (time[2] - time[1]);
    }

    public long getConfTime() {
        return (time[3] - time[2]);
    }

    public long getResTime() {
        return (time[4] - time[3]);
    }

    public long getFullTime() {
        return (time[4] - time[0]);
    }

    public int getNbNodes() {
        return nbnode;
    }

    public Boolean isFeasible() {
        return isFeasible;
    }

    public String[] getValues() {
        return values;
    }

    public boolean checkEverythingIsInstantiated(InstanceParser parser, Solver s) {
        for (int i = 0; i < parser.getVariables().length; i++) {
            try {
                if (!s.getVar(parser.getVariables()[i].getChocovar()).isInstantiated()) {
                    return false;
                }
            } catch (NullPointerException ignored) {
            }
        }
        return true;
    }


    /**
     * An example on how to use the xml parser-solver from the api
     */
    public static void example() {
        String fichier = "../../ProblemsData/CSPCompet/intension/nonregres/graph1.xml";
        File instance = new File(fichier);
        XmlModel xs = new XmlModel();

        try {
            InstanceParser parser = xs.load(instance);
            CPModel model = xs.buildModel(parser);

            //use the blackbox solver and blackbox search
            PreProcessCPSolver s = xs.solve(model);
            xs.postAnalyze(instance, parser, s);

            //use a blackbox solver or a standart CP solver
            //and perform the search by yourself
//            BlackBoxCPSolver s = new BlackBoxCPSolver();
//            CPSolver s = new CPSolver();
//            s.read(model);
//            s.solve();
//            LOGGER.info(s.pretty());
//            s.printRuntimeSatistics();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
//         example();
        XmlModel xs = new XmlModel();
        xs.generate(args);
    }


}