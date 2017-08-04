package lkhj;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.*;

/**
 * An LK heuristic implementation for TSP problem
 * Created by xinyun on 03/07/17.
 */
public class LKHJ {

    final private int MAX_CANDIDATES = 5;
    final private int MAX_MOVE_LEVEL = 5;
    private Random random;
    private double[][] costMatrix;
    private TwoLevelTree tree;
    private double objective;
    private double LB;
    private ArrayList<ArrayList<Integer>> candidatesTable;
    private double[] pi;
    int[] bestTour = null;


    public LKHJ(double[][] costMatrix, Random random){
        this.costMatrix = costMatrix;
        this.random = random;

    }

    private void genNearestTable(int nearestCount){
        candidatesTable = new ArrayList<>();

        for (int i=0; i<costMatrix.length; ++i){
            ArrayList<Integer> line = new ArrayList<>(costMatrix.length-1);
            for (int j=0; j<costMatrix.length; ++j){
                if (i != j){
                    line.add(j);
                }
            }
            candidatesTable.add(line);
        }

        for (int i = 0; i< candidatesTable.size(); ++i){
            final int index = i;
            ArrayList<Integer> line = candidatesTable.get(index);
            line.sort(new Comparator<Integer>() {
                @Override
                public int compare(Integer integer, Integer t1) {
                    return Double.compare(costMatrix[index][integer] + pi[integer], costMatrix[index][t1] + pi[t1]);
                }
            });
        }

        for (ArrayList<Integer> line : candidatesTable){
            line.subList(nearestCount, line.size()).clear();
            line.trimToSize();
        }
    }

    private void genInitialTour(OneTree oneTree){
//        int[] tour = new GreedyTSP(costMatrix).solve();
//        tree = new TwoLevelTree(tour);
//        objective = calculateObj();

        int[] tour = new int[costMatrix.length];
        int count = 1;

        HashSet<Integer> reMainingNodes = new HashSet<>();
        for (int i=0; i<costMatrix.length; ++i){
            reMainingNodes.add(i);
        }
        tour[0] = random.nextInt(costMatrix.length);
        reMainingNodes.remove(tour[0]);

        while(count < costMatrix.length){
            int currNode = tour[count-1];
            int nextNode = chooseNextNodeForInit(currNode, reMainingNodes, oneTree);
            tour[count] = nextNode;
            reMainingNodes.remove(nextNode);
            ++count;
        }

        tree = new TwoLevelTree(tour);
        objective = calculateObj();
    }

    private int chooseNextNodeForInit(int currNode, HashSet<Integer> remainings, OneTree oneTree){
        for (int n : candidatesTable.get(currNode)){
            if (remainings.contains(n) && oneTree.hasEdge(currNode, n)){
                return n;
            }
        }
        for (int n : candidatesTable.get(currNode)){
            if (remainings.contains(n)){
                return n;
            }
        }

        int randomPick = random.nextInt(remainings.size());
        for (int n : remainings){
            if (--randomPick < 0)return n;
        }
        throw new Error("chooseNextNodeForInit");
    }

    private double calculateObj(){
        double tourCost = 0;
        int headCityID = tree.getHeadCityID();
        int currCityID = headCityID;

        do{
            int nextCityID = tree.next(currCityID);
            tourCost += costMatrix[currCityID][nextCityID];
            currCityID = nextCityID;
        }while(currCityID != headCityID);


        return tourCost;
    }

    private void makeMove(FlipMove flipMove){
        if (flipMove.a == tree.next(flipMove.b)) {
            tree.flip(flipMove.a, flipMove.b, flipMove.c, flipMove.d);
        }else{
            tree.flip(flipMove.b, flipMove.a, flipMove.d, flipMove.c);
        }
        objective += flipMove.deltaObj;
    }

    private FlipMove evaluateMove(int a, int b, int c, int d){
        return new FlipMove(a, b, c, d,
                0 - costMatrix[a][b] - costMatrix[c][d] + costMatrix[a][d] + costMatrix[b][c]);
    }

    private double checkCalcObjective(){
        int[] tour = tree.getCurrentTour();
        double obj = 0;
        for (int i=0; i< tour.length - 1; ++i){
            obj += costMatrix[tour[i]][tour[i+1]];
        }
        obj += costMatrix[tour[tour.length-1]][tour[0]];

        return obj;
    }

    public double solve(){


        double bestLength = Double.MAX_VALUE;

        OneTree oneTree = initialize();
        System.out.println("LB: " + LB);
        genNearestTable(MAX_CANDIDATES);


        for (int run = 0; run < 10; ++run) {
            System.out.println("Run #" + run);
            genInitialTour(oneTree);
            int iter = 0;
            while (LKMove()) {
//                if (iter % 100 == 0) {
//                    printObjAndGap();
//                }
                ++iter;
            }

            printObjAndGap();
            System.out.println(tree.checkTree());
            System.out.println(checkCalcObjective());

            if (Double.compare(bestLength, objective) > 0){
                bestLength = objective;
                bestTour = tree.getCurrentTour();
            }
        }

        System.out.println("Best Tour Found: " + bestLength + ". Gap = " + (bestLength - LB)/LB*100 + "%");

        return bestLength;
    }

    private boolean LKMove(){
        int t1 = 0;
        do{
            if (moveFromCity(t1)){
                return true;
            }
            t1 = (t1+1) % costMatrix.length;
        }while(t1 != 0);
        return false;
    }

    private boolean isFeasibleFlipMove(int t1, int t2, int t3, int t4){
        return t2 != t4 && t3 != t1 && t1 != t4
                && tree.between(t4, t2, t1) && tree.between(t1, t3, t4);
    }

    private void printLog(String string){
        System.out.println(string);
    }

    private void printObjAndGap(){
        printLog("obj : " + (objective) + "  Gap : " + ((objective - LB)/LB * 100) + "%");
    }

    private boolean moveFromCity(final int t1){
        ArrayList<Edge> xs = new ArrayList<>();
        ArrayList<Edge> ys = new ArrayList<>();

        int t2 = tree.prev(t1);
        return findNextMove(t1, t2, xs, ys, 0, 2, MAX_MOVE_LEVEL, "");
    }

    private boolean tryT4IsNextT3(int t1, int t2, int t3,
                                  ArrayList<Edge> xs,
                                  ArrayList<Edge> ys,
                                  double sumDelta, int level, int maxLevel, String star){
        final int t4 = tree.next(t3);
        if (ys.contains(new Edge(t3, t4))
                ||
                !isFeasibleFlipMove(t1, t2, t3, t4)) return false;

        FlipMove fmv = evaluateMove(t1, t2, t3, t4);
        if (fmv.deltaObj + sumDelta < 0) {
            makeMove(fmv);
            //printLog(level + star+ "-opt move! " + tree.checkTree());
            return true;
        } else if (level < maxLevel){
            makeMove(fmv);
            xs.add(new Edge(fmv.c, fmv.d));
            ys.add(new Edge(fmv.b, fmv.c));
            if (fmv.a == tree.next(fmv.d)){
                if (findNextMove(fmv.a, fmv.d,xs, ys,
                        fmv.deltaObj + sumDelta,
                        level + 1, maxLevel, star)){
                    return true;
                }
            }else {
                if (findNextMove(fmv.d, fmv.a,xs, ys,
                        fmv.deltaObj + sumDelta,
                        level + 1, maxLevel, star)) {
                    return true;
                }
            }
            xs.remove(xs.size()-1);
            ys.remove(ys.size() - 1);
            if (fmv.a == tree.next(fmv.d) && fmv.b == tree.next(fmv.c)){
                makeMove(new FlipMove(fmv.a, fmv.d, fmv.c, fmv.b, -fmv.deltaObj));
            }else{
                makeMove(new FlipMove(fmv.d, fmv.a, fmv.b, fmv.c, -fmv.deltaObj));
            }
        }
        return false;
    }

    private boolean tryT4IsPrevT3(int t1, int t2, int t3, int maxLevel){
        final int t4 = t1 == tree.next(t2) ? tree.prev(t3) : tree.next(t3);
        if (t4==t1 || t4==t2)return false;
        final double x1 = costMatrix[t1][t2];
        final double y1 = costMatrix[t2][t3];
        final double x2 = costMatrix[t3][t4];
        for (int t5 : candidatesTable.get(t4)){
            if ((t1 == tree.next(t2) && !tree.between(t3,t5,t2))
                    ||
                    (t2 == tree.next(t1) && !tree.between(t2,t5,t3)))continue;
            final double y2 = costMatrix[t4][t5];
            if (Double.compare(y2,x2) > 0) continue;
            int t6 = t1 == tree.next(t2) ? tree.prev(t5) : tree.next(t5);
            if (t6==t3)continue;
            double x3 = costMatrix[t5][t6];
            double y3 = costMatrix[t6][t1];
            if (Double.compare(y1+y2+y3, x1+x2+x3) < 0){
                makeMove(evaluateMove(t1,t2,t4,t3));
                makeMove(evaluateMove(t4,t2,t6,t5));
                makeMove(evaluateMove(t6,t2,t3,t1));
                //printLog("3*-opt move! " + tree.checkTree());
                return true;
            }else{

                makeMove(evaluateMove(t1, t2, t4, t3));
                makeMove(evaluateMove(t4, t2, t6, t5));
                makeMove(evaluateMove(t6, t2, t3, t1));

                ArrayList<Edge> ys = new ArrayList<>();
                ArrayList<Edge> xs = new ArrayList<>();
                xs.add(new Edge(t1,t2));
                xs.add(new Edge(t3,t4));
                xs.add(new Edge(t5,t6));
                ys.add(new Edge(t1,t6));
                ys.add(new Edge(t2,t3));
                ys.add(new Edge(t4,t5));
                if (t1 == tree.next(t6)){
                    if(findNextMove(t1,t6, xs, ys,
                            y1+y2+y3-x1-x2-x3,
                            4, maxLevel, "*")){
                        return true;
                    }
                }else{
                    if(findNextMove(t6,t1, xs, ys,
                            y1+y2+y3-x1-x2-x3,
                            4, maxLevel, "*")){
                        return true;
                    }
                }
                makeMove(new FlipMove(t3,t2,t6,t1, x1+x2+x3-y1-y2-y3));
                makeMove(new FlipMove(t6,t2,t4,t5,0));
                makeMove(new FlipMove(t4,t2,t1,t3, 0));
                xs.subList(xs.size()-3, ys.size()).clear();
                ys.subList(ys.size()-3, ys.size()).clear();
            }
        }

        return false;
    }

    private boolean findNextMove(int t1, int t2,
                                 ArrayList<Edge> xs,
                                 ArrayList<Edge> ys,
                                 double sumDelta, int level, final int maxLevel, String star){
        final double x1 = costMatrix[t1][t2];
        for (int t3 : candidatesTable.get(t2)) {
            if (t3 == t1 || xs.contains(new Edge(t2,t3)))continue;
            final double y1 = costMatrix[t2][t3];
            if (Double.compare(y1,x1) > 0)continue;
            if (tryT4IsNextT3(t1, t2, t3,xs, ys, sumDelta, level, maxLevel, star)){
                return true;
            }
            if (level == 2 && tryT4IsPrevT3(t1,t2,t3,maxLevel)){
                return true;
            }
        }
        return false;
    }

    private void init(){
        pi = new double[costMatrix.length];
        double[] piCopy = new double[costMatrix.length];
        double v[] = new double[costMatrix.length];
        LB = - Double.MAX_VALUE;
        double tk = 0.01;

        for (;;){
            OneTree tree = new OneTree(costMatrix, pi);
            if (Double.compare(LB, tree.treeLength) < 0){
                LB = tree.treeLength;
                printLog(LB + " " + tk);
                calcV(tree, v);
                if (subgradientIsOpt(v)){
                    printLog("Optimal got in initialization");
                }
                System.arraycopy(pi, 0, piCopy, 0, pi.length);
                updatePi(pi, tk, v);
            }else{
                System.arraycopy(piCopy, 0, pi, 0, pi.length);
                tk /= 2;
                calcV(tree, v);
                updatePi(pi, tk, v);
            }
            if (tk < 0.00000001)break;
        }
    }

    private OneTree initialize(){
        pi = new double[costMatrix.length];
        double v[] = new double[costMatrix.length];
        LB = -Double.MIN_VALUE;
        double tk = 0.02;
        int period = costMatrix.length / 2;

        int iter = 0;
        boolean firstPeriod = true;
        OneTree bestTree = null;
        System.out.println("Initializing...");
        for (;;) {
            OneTree tree = new OneTree(costMatrix, pi);
            double w = tree.treeLength;
            System.out.println(w + " " + tk + " " + iter);
            if (Double.compare(LB,w) < 0){
                LB = w;
                bestTree = tree;
                if (iter == period) period*= 2;
            }else if (firstPeriod){
                tk /=2;
                firstPeriod = false;
            }
            calcV(tree, v);
            if (subgradientIsOpt(v)){
                System.out.println("Optima in Initialization");
                break;
            }

            updatePi(pi, tk, v);
            ++iter;
            if (iter == period){
                iter = 0;
                period /= 2;
                tk /= 2;
                //firstPeriod = true;
                if (period == 0 || tk < 0.000001){
                    break;
                }
            }
        }
        return bestTree;
    }

    private void updatePi(double[] pi, double tk, double[] v){
        for (int i=0; i<pi.length; ++i){
            pi[i] += tk * v[i];
        }
    }

    private boolean subgradientIsOpt(double[] v){
        for (double vi : v){
            if (Double.compare(vi, 0) != 0) return false;
        }
        return true;
    }

//    private double computeW(double treeLength, double[] pi){
//        double w = treeLength;
//        twoSumPi = 0;
//        for (double pii : pi){
//            twoSumPi += 2*pii;
//        }
//        return w - twoSumPi;
//    }

    private void calcV(OneTree tree, double[] v){
        for (int i=0; i<v.length; ++i){
            v[i] = tree.getDegree(i) - 2;
        }
    }

    public int[] getCurrentTour(){
        return bestTour;
    }

    private class Edge{
        int s;
        int d;
        Edge(int s, int d){
            this.s = s;
            this.d = d;
        }

        @Override
        public boolean equals(Object o){
            if (o==null) return false;
            if (!Edge.class.isAssignableFrom(o.getClass())) return false;
            Edge ep = (Edge) o;
            return this.s == ep.s && this.d == ep.d
                    ||
                    this.s == ep.d && this.d == ep.s;
        }
    }

    private class FlipMove {
        final int a;
        final int b;
        final int c;
        final int d;
        final double deltaObj;
        FlipMove(int a, int b, int c, int d, double deltaObj){
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.deltaObj = deltaObj;
        }
    }
}
