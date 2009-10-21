package choco.cp.solver.constraints.global.geost.layers;

import choco.cp.solver.constraints.global.geost.Setup;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistGeq;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistLeq;
import choco.cp.solver.constraints.global.geost.externalConstraints.DistLinear;
import choco.cp.solver.constraints.global.geost.externalConstraints.ExternalConstraint;
import choco.cp.solver.constraints.global.geost.geometricPrim.Obj;
import choco.cp.solver.constraints.global.geost.geometricPrim.Region;
import choco.cp.solver.constraints.global.geost.internalConstraints.InternalConstraint;
import choco.cp.solver.constraints.global.geost.layers.continuousSolver.Quimper;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: szampelli
 * Date: 14 ao�t 2009
 * Time: 16:27:20
 * To change this template use File | Settings | File Templates.
 */
public class GeostNumeric {

    Setup stp=null;
    Quimper engine=null;
    int paramid=0; //each detected new param gets a param id which is the id of the param in the extengine
    int varid=0;    //each detected new variable gets a variable id which is the id of the var in the extengine
    // (extengine=external engine=numeric solver here)
    int ctrid=0; //each added new constraint gets a cstr id
    String listVars="";
    String listParams="";
    String listCstrs="";
    String listContractors="";


    HashMap<Obj,String> cstrs = new HashMap<Obj,String>();
    HashMap<Obj,String> contractorName = new HashMap<Obj,String>();
    //list of var ids in the ext engine corresponding to domain variable
    HashMap<IntDomainVar,Vector<Integer>> VarVarId=new HashMap<IntDomainVar,Vector<Integer>>();
    //list of param ids in the ext engine corresponding to domain variable
    HashMap<IntDomainVar,Vector<Integer>> varParamId=new HashMap<IntDomainVar,Vector<Integer>>();
    //list of domain variables that exist in the external engine
    HashMap<IntDomainVar,Boolean> listOfVars=new HashMap<IntDomainVar,Boolean>();
    HashMap<Obj,Vector<String>> objCstrs=new HashMap<Obj,Vector<String>>();
    HashMap<Obj,Vector<Integer>> objParamId=new HashMap<Obj,Vector<Integer>>();

    HashMap<IntDomainVar,HashMap<ExternalConstraint, Integer>> VarParamId = new HashMap<IntDomainVar,HashMap<ExternalConstraint, Integer>>();
    HashMap<Obj,HashMap<ExternalConstraint, String>> ObjParamIdText = new HashMap<Obj,HashMap<ExternalConstraint, String>>();
    HashMap<Obj,HashMap<ExternalConstraint,String>> ObjCstrName = new HashMap<Obj,HashMap<ExternalConstraint,String>>();


    long cr=-1; //conversion rate between domain variable and external engine
    double isThick=0.0;

    public GeostNumeric(Setup stp_, int maxNbrOfBoxes) {
        stp=stp_;

        cr=computeConversionRate();

        isThick=computeIsThick(maxNbrOfBoxes);


        for (int oid=0; oid<stp.getObjectKeySet().size(); oid++){ //fixed order iteration
            Obj o=stp.getObject(oid);
            addObj(o);
        }

        for (int oid=0; oid<stp.getObjectKeySet().size(); oid++){ //fixed order iteration
            Obj o=stp.getObject(oid);
            for (ExternalConstraint ectr:o.getRelatedExternalConstraints()) {
                addCstr(o,ectr); //write constraint and params at the same time
            }
            writeContractor(o);
        }

        writeFile("/tmp/quimper.qpr");

        engine = new Quimper("/tmp/quimper.qpr");
    }

    private void addObj(Obj o) {
        listVars+=strObj(o);
        int k=o.getCoordinates().length;
        for (int d=0; d<k; d++) {
            IntDomainVar v=o.getCoord(d);
            addVar(v);
        }
    }

    private void addVar(IntDomainVar v) {
        if (!VarVarId.containsKey(v)) VarVarId.put(v,new Vector<Integer>());
        VarVarId.get(v).add(varid++);
        listOfVars.put(v,true);
    }


    private String  strObj(Obj o) {
        String r="";
        int k=o.getCoordinates().length;
        r="o"+o.getObjectId()+"["+k+"] in [";
        for (int d=0; d<k; d++) {
            r+="[";
            r+=coordToExtEngine(o.getCoord(d).getInf())+"/*"+o.getCoord(d).getInf()+"*/,";
            r+=coordToExtEngine(o.getCoord(d).getSup())+"/*"+o.getCoord(d).getSup()+"*/";
            if (d==k-1) r+="]"; else r+="];";
        }
        r+="];\n";
        return r;
    }


    private String strParam(Obj o,ExternalConstraint ectr) {
        String r="";
        int k=o.getCoordinates().length;
        r=ObjParamIdText.get(o).get(ectr)+"["+k+"] in [";
        for (int d=0; d<k; d++) {
            r+="[";
            r+=coordToExtEngine(o.getCoord(d).getInf())+"/*"+o.getCoord(d).getInf()+"*/"+",";
            r+=coordToExtEngine(o.getCoord(d).getSup())+"/*"+o.getCoord(d).getSup()+"*/";
            if (d==k-1) r+="]"; else r+="];";
        }
        r+="];\n";
        return r;
    }


    private void addParam(Obj o, ExternalConstraint ectr) {
        //Add all obj to the system which are different from o and included in ectr
        if (ectr instanceof DistLeq) {
            DistLeq dl = (DistLeq) ectr;
            int oid=o.getObjectId();
            int toAdd=oid;
            if (dl.o1==oid) toAdd=dl.o2; else toAdd=dl.o1;
            addObjParamText(stp.getObject(toAdd),ectr);
            listParams+=strParam(stp.getObject(toAdd),ectr);
            if (dl.hasDistanceVar()) { addVarParam(dl.getDistanceVar(),ectr); listParams+=strParam(dl.getDistanceVar(),ectr); }
        }
        else if (ectr instanceof DistGeq){
            DistGeq dl = (DistGeq) ectr;
            int oid=o.getObjectId();
            int toAdd=oid;
            if (dl.o1==oid) toAdd=dl.o2; else toAdd=dl.o1;
            addObjParamText(stp.getObject(toAdd),ectr);
            listParams+=strParam(stp.getObject(toAdd),ectr);
            if (dl.hasDistanceVar()) { addVarParam(dl.getDistanceVar(),ectr); listParams+=strParam(dl.getDistanceVar(),ectr); }
        }
        else if (ectr instanceof DistLinear) {
            //no parameter
        }
        else {
            System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:addParam():External Constraint "+ectr+" not supported yet.");
            System.exit(-1);            
        }
        ctrid++;
    }

    private String strParam(IntDomainVar v,ExternalConstraint ectr) {
        String name="p"+VarParamId.get(v).get(ectr);
        String r=name+" in ";
        r+="[";
        r+=coordToExtEngine(v.getInf())+",";
        r+=coordToExtEngine(v.getSup());
        r+="];\n";
        return r;
    }


    private void addObjParamText(Obj o, ExternalConstraint ectr) {
        if (ObjParamIdText.get(o)==null) ObjParamIdText.put(o,new HashMap<ExternalConstraint, String>());
        ObjParamIdText.get(o).put(ectr,strObjName(o,ectr));

        for (IntDomainVar v:o.getCoordinates()) {
            addVarParam(v,ectr);
            paramid++;
        }
    }

    private String strObjName(Obj o, ExternalConstraint ectr) {
        //return the name of the param of obj o and constraint ectr
        return "o"+o.getObjectId()+"_ctr"+ctrid;
    }

    private void addVarParam(IntDomainVar v, ExternalConstraint ectr) {
        if (VarParamId.get(v)==null) VarParamId.put(v,new HashMap<ExternalConstraint, Integer>());
        VarParamId.get(v).put(ectr,paramid);
        if (varParamId.get(v)==null) varParamId.put(v,new Vector<Integer>());
        varParamId.get(v).add(paramid);
        listOfVars.put(v,true);
        paramid++;
    }


    private Vector<IntDomainVar> extractParams(Obj o, ExternalConstraint ectr) {
        Vector<IntDomainVar> result = new Vector<IntDomainVar>();
        if (ectr instanceof DistLeq) {
            DistLeq dl = (DistLeq) ectr;
            int oid = 0;
            if (o.getObjectId()==dl.o1) oid=2; else oid=dl.o1;
            for (int i=0; i<stp.getObject(oid).getCoordinates().length; i++)
                result.add(stp.getObject(oid).getCoord(i));

            if (dl.hasDistanceVar()) result.add(dl.getDistanceVar());
        }
        else if (ectr instanceof DistGeq) {
            DistGeq dl = (DistGeq) ectr;
            int oid = 0;
            if (o.getObjectId()==dl.o1) oid=2; else oid=dl.o1;
            for (int i=0; i<stp.getObject(oid).getCoordinates().length; i++)
                result.add(stp.getObject(oid).getCoord(i));
            if (dl.hasDistanceVar()) result.add(dl.getDistanceVar());
        }
        else if (ectr instanceof DistLinear) {
            //This block is empty on purpose            
            //since there are no other variables involved than the pruned variable itself
        }
        else {
            System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:extractParam():External Constraint "+ectr+" not supported yet.");
            System.exit(-1);
        }


        return result;
    }




    private void addCstr(Obj o, ExternalConstraint ectr) {
        addParam(o,ectr);
        listCstrs+=strCstr(o,ectr); //return the cstr string and associate o with the name of the constraint
        addCstrName(o,ectr); //asociate o and the constraint name
        ctrid++;
    }

    private void addCstrName(Obj o, ExternalConstraint ectr) {
        String name=strCstrName(o,ectr);
        if (ObjCstrName.get(o)==null) ObjCstrName.put(o,new HashMap<ExternalConstraint,String>());
        ObjCstrName.get(o).put(ectr,name);
    }

    private Vector<String> getListStringCstr(Obj o) {
        return objCstrs.get(o);        
    }

    private String strCstr(Obj o, ExternalConstraint ectr) {
                String name=strCstrName(o,ectr);
                String r="";
                if (ectr instanceof DistLeq) {
                    DistLeq dl = (DistLeq) ectr;
                    int oid=o.getObjectId();
                    r+="constraint "+strCstrName(o,ectr)+"\n";
                    if (dl.hasDistanceVar()) {
                        r+=" distance(o"+oid+","+getObjectParamIdText(stp.getObject(dl.o2),ectr)+")<="
                            +getVarParamIdText(dl.getDistanceVar(),ectr)+";\n";
                    }
                    else {
                        r+=" distance(o"+oid+","+getObjectParamIdText(stp.getObject(dl.o2),ectr)+")<="+coordToExtEngine(dl.D/2)+"/*"+dl.D+"*/"+";\n";
                    }
                    r+="end\n";
                }
                else if (ectr instanceof DistGeq) {
                    DistGeq dl = (DistGeq) ectr;
                    int oid=o.getObjectId();
                    r+="constraint "+strCstrName(o,ectr)+"\n";
                    if (dl.hasDistanceVar()) {
                        r+=" distance(o"+oid+","+getObjectParamIdText(stp.getObject(dl.o2),ectr)+")>="
                            +getVarParamIdText(dl.getDistanceVar(),ectr)+";\n";
                    }
                    else {
                        r+=" distance(o"+oid+","+getObjectParamIdText(stp.getObject(dl.o2),ectr)+")>="+coordToExtEngine(dl.D)+"/*"+dl.D+"*/"+";\n";
                    }
                    r+="end\n";
                }
                else if (ectr instanceof DistLinear) {
                    System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:strCstr():External Constraint "+ectr+" not supported yet.");
                    System.exit(-1);
                }

                return r;
    }

    private String getObjectParamIdText(Obj o, ExternalConstraint ectr) {
        return ObjParamIdText.get(o).get(ectr);
    }

    private String strCstrName(Obj o, ExternalConstraint ectr) {
        return "obj"+o.getObjectId()+"_c"+ctrid;
    }

    private String getVarParamIdText(IntDomainVar v, ExternalConstraint ectr) {
        return "p"+VarParamId.get(v).get(ectr);
    }


    private double computeIsThick(int maxNbrOfBoxes) {
        //The goal of 'computeIsThick' is to compute the parameter isThick such that
        //the max. nbr of boxes generated by the numeric engine os 'maxNbrOfBoxes'.
        //PRE: cr has been computed

        if (cr==-1) {
            System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeIsThick:conversion ratio cr must be computed before isThick can be compured");
            System.exit(-1);
        }

        double maxNbrOfBoxesDouble=(double) maxNbrOfBoxes;

        int k = stp.getObject(0).getCoordinates().length;
        double volume=(double)volume();
        System.out.println("volume:"+String.format("%f",volume));

        isThick=1.0;
        double inverse_k=(double) 1.0/((double)k);
        if (inverse_k<=0) {
            System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeIsThick:unable to compute isThick because of 1/k:"+inverse_k);
            System.exit(-1);
        }
        return Math.pow((volume/maxNbrOfBoxesDouble), inverse_k)/cr;
    }

    private long volume() {
        int k = stp.getObject(0).getCoordinates().length;
        long volume=1;

        for (int dim=0; dim<k; dim++) {
            //determine min and max for dim i
            int min=stp.getObject(0).getCoord(dim).getInf();
            int max=stp.getObject(0).getCoord(dim).getSup();
            for(int i : stp.getObjectKeySet()) {
                    Obj o = stp.getObject(i);
                    min=Math.min(min,o.getCoord(dim).getInf());
                    max=Math.max(max,o.getCoord(dim).getSup());
            }

           volume*=Math.abs(max-min);
        }

        return volume;

    }


    private long computeConversionRate() {
        //compute conversion rate (of the form 10^k) on min and max
        //determine min and max
        int min=stp.getObject(0).getCoord(0).getInf();
        int max=stp.getObject(0).getCoord(0).getSup();
        for(int i : stp.getObjectKeySet()) {
            Obj o = stp.getObject(i);
            for (int j=0; j<o.getCoordinates().length; j++) {
                min=Math.min(min,o.getCoord(j).getInf());
                max=Math.max(max,o.getCoord(j).getSup());
            }
        }
        //determine conversion rate of min
        long cr_min=1;
        while ((min/cr_min)>0) {
            cr_min*=10;
            if (cr_min<0) {
                System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeConversionRate:long limit cr_min exceeded");
                System.exit(-1);
            }
        }
        //determine conversion rate of max
        long cr_max=1;
        while ((max/cr_max)>0) {
            cr_max*=10;
            if (cr_max<0) {
                System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:computeConversionRate:long limit cr_max exceeded");
                System.exit(-1);
            }
        }

        //return the max of the two
        return Math.max(cr_min,cr_max);
    }

    private double coordToExtEngine(int v) {
        double vd=(double) v;
        return (vd/cr);
    }

    private void writeContractor(Obj o) {
        Vector<ExternalConstraint> vectr=o.getRelatedExternalConstraints();
        if (vectr.size()>0) {
            listContractors+="contractor object"+o.getObjectId()+"\npropag(";

            for (int i=0; i<vectr.size();i++) {
                    String ctrname=ObjCstrName.get(o).get(vectr.elementAt(i));
                    if (i==vectr.size()-1) listContractors+=ctrname+");"; else listContractors+=ctrname+";";
            }
            listContractors+="\nend/*listContractors*/\n";
            contractorName.put(o,"object"+o.getObjectId());
        }



    }

    private void writeFile(String filename) {
        try {
            File f = new File(filename);
            PrintWriter pw = new PrintWriter(new FileWriter(f));
            pw.println("Variables");
            pw.println(listVars);
            pw.println("Parameters");
            pw.println(listParams);
            pw.println("function d=distance(x[2],y[2])\n\td=sqrt((x[1]-y[1])^2+(x[2]-y[2])^2);\nend");
            pw.println(listCstrs);
            pw.println(listContractors);
            //DecimalFormat df = new DecimalFormat("###.#######################");
            // df.format(isThick)
            pw.println("contractor isthick\nmaxdiamGT("+String.format("%f",isThick).replace(",",".")+");\nend\n");

            pw.close();
        }
        catch(Exception e) {
                System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:writeFile():could not write file");
                System.exit(-1);
        }
    }

    public void synchronize() {
//        for (int i : stp.getObjectKeySet()) {
//            Obj o=stp.getObject(i);
//            synchronize(o);
//        }

        for (IntDomainVar v : listOfVars.keySet())
            synchronize(v);        
    }

    public void synchronize(Obj o) {
        for (int i=0; i<o.getCoordinates().length; i++) {
            IntDomainVar v=o.getCoord(i);
            for (int id : VarVarId.get(v)) {
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id,inf,sup);
            }
        }
    }

    public void synchronize(IntDomainVar v) {

        Vector<Integer> a=VarVarId.get(v);
        Vector<Integer> b=varParamId.get(v);
        if (a!=null) {
            for (int id : VarVarId.get(v)) {
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id,inf,sup);
            }
        }
        else if (b!=null) {
            Vector<Integer> vint = varParamId.get(v);
            for (int i=0; i<vint.size(); i++) {
                int id=vint.elementAt(i);
                double inf = coordToExtEngine(v.getInf());
                double sup = coordToExtEngine(v.getSup());
                engine.set_var_domain(id,inf,sup);
            }

        }
        else {
            System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:synchronize:IntDomainVar not found");
            System.exit(-1);

        }


    }

    public void synchronize(ExternalConstraint ectr) {
        System.out.println("choco.cp.solver.constraints.global.geost.layers.GeostNumeric:synchronize:External Constraints are not supported yet.");
        System.exit(-1);            

    }

    public Region propagate(Obj o) {
        //Returns a region /*containing*/ the solution
        System.out.println("--Entering GeostNumeric:propagate()");

        int k = o .getCoordinates().length;
        if (contractorName.get(o)==null) return new Region(k,o);
        System.out.println("calling contract(object"+o.getObjectId()+") because of contractorName.get("+o+")="+contractorName.get(o));

        engine.contract("object"+o.getObjectId());
        Region r = new Region(k,o.getObjectId());
        for (int i=0; i<k; i++) {
            //Probleme de conversion inverse ici
            String id = "o"+o.getObjectId()+"["+i+"]";
            double lb=engine.get_lb(id);
            double ub=engine.get_ub(id);
            int lb_int=(int) Math.floor(lb*cr);
            int ub_int=(int) Math.ceil(ub*cr);
            r.setMinimumBoundary(i,lb_int);
            r.setMaximumBoundary(i,ub_int);
        }
        System.out.println("--Exiting GeostNumeric:propagate()");
        return r;
    }


    public void Prune(Obj o, int k, Vector<InternalConstraint> ictrs) throws ContradictionException {
        System.out.println("Entering Prune:"+o+","+k+","+ictrs);
        //returns no value, but throws a contradiction exception if failure occurs
        //call engine to propagate
        synchronize();
        Region box = propagate(o);
        //update o with box; set b to true if o is modified.
        for (int i=0; i<k; i++) {
            int min = box.getMinimumBoundary(i);
            int max = box.getMaximumBoundary(i);
            int min_ori=o.getCoord(i).getInf();
            int max_ori=o.getCoord(i).getSup();
            System.out.println("Prune():"+o+"["+i+"] updated to ["+min+","+max+"]");

            boolean var_was_modified=false;
            if (min>min_ori) {
                var_was_modified=true;
                //Detect failure to update b
                o.getCoord(i).setInf(min);
            }
            if (max<max_ori) {
                var_was_modified=true;
                o.getCoord(i).setSup(max);
            }

            //TODO: synchronize variables only when they are modified
            if (var_was_modified) synchronize(o.getCoord(i)); //(A)
            System.out.println("Prune():synchronize o["+i+"]=["+o.getCoord(i).getInf()+","+o.getCoord(i).getSup()+"]");
        }
        //The following line is an alternative to the synchronization in line (A)
        //if (b) synchronize(o); //(B)
        //(B) is simply more computational-expensive than (A)
        System.out.println("Exiting Prune()");
    }


}


//        for (ExternalConstraint ectr : stp.getConstraints()) {
//        }
//
//
//
//
//        //new ComponentConstraint(ConstraintType.GEOST, new Object[]{dim, shiftedBoxes, eCtrs, objects, ctrlVs, opt}, vars);
//            Object[] params = (Object[])parameters;
//            int dim = (Integer)params[0];
//            Vector<ShiftedBox> shiftedBoxes = (Vector<ShiftedBox>)params[1];
//            Vector<IExternalConstraint> ectr = (Vector<IExternalConstraint>)params[2];
//            Vector<GeostObject> vgo = (Vector<GeostObject>)params[3];
//            Vector<int[]> ctrlVs = (Vector<int[]>)params[4];
//            GeostOptions opt = (GeostOptions) params[5];
//            if (opt==null) { opt=new GeostOptions(); }
//
//            CPSolver solver = (CPSolver) stp.getObject(0).getCoord(0).getSolver();
//
//            //Transformation of Geost Objects (model) to interval geost object (constraint)
//            Vector<Obj> vo = new Vector<Obj>(vgo.size());
//            for (int i = 0; i < vgo.size(); i++) {
//                GeostObject g = vgo.elementAt(i);
//                vo.add(i, new Obj(g.getDim(),
//                        g.getObjectId(),
//                        solver.getVar(g.getShapeId()),
//                        solver.getVar(g.getCoordinates()),
//                        solver.getVar(g.getStartTime()),
//                        solver.getVar(g.getDurationTime()),
//                        solver.getVar(g.getEndTime()),
//                        g.getRadius())
//                        );
//            }
//
//
//        String vars="";
//        int k=stp.getObject(0).getCoordinates().length;
//        int index=0;
//        //for each object i
//        for (int i=0; i<stp.getObjectKeySet().size(); i++) {
//            Obj o = stp.getObject(i);
//            //declare variable domain
//            cvars.put(i,index);
//            vars+="o"+i+"["+k+"] in ";
//            vars+="[";
//            for (int d=0; d<k; d++) {
//                vars+="[";
//                vars+=o.getCoord(d).getInf()+",";
//                vars+=o.getCoord(d).getSup();
//                if (d==k-1) vars+="]"; else vars+="];";
//            }
//            vars+="];\n";
//            index+=2;
//
//
//            //Assign a parameter for all variables related to o_i
//           for (ExternalConstraint ect : stp.getConstraints() ) {
//                if (ect instanceof DistLeq) {
//                    DistLeq dl = (DistLeq) ect;
//                    //pars[i][dl.o2]=
//
//                }
//
//            }
//
//            String constraints="";
//            int nextCstr=0;
//            //for each constraint attached to object i
//            for (ExternalConstraint ect : stp.getConstraints() ) {
//                if (ect instanceof DistLeq) {
//                    DistLeq dl = (DistLeq) ect;
//                    constraints+="constraint obj"+i+"_c"+nextCstr+"\n";
//                    constraints+=" distance(o"+i+",p"+pars.get(dl.getObjectIds()[1])+")<=p"+pars.getVarCstr(dl.getCstrId())+";\n";
//                    constraints+="end\n";
//
//                }
//            }
//
//
//
//
//
//        }
//
