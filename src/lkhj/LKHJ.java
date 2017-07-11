package lkhj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * An LK heuristic implementation for TSP problem
 * Created by xinyun on 03/07/17.
 */
public class LKHJ {

    private Random random;
    private double[][] costMatrix;
    private TwoLevelTree tree;
    private double objective;

    private ArrayList<ArrayList<Integer>> candidatesTable;
    private int MAX_CANDIDATES = 5;
    private int MAX_MOVE_LEVEL = 5;

    public LKHJ(double[][] costMatrix, Random random){
        this.costMatrix = costMatrix;
        this.random = random;
        genNearestTable(MAX_CANDIDATES);
    }

    public void setMAX_MOVE_LEVEL(int level){
        MAX_MOVE_LEVEL = level;
    }

    public void setMAX_CANDIDATES(int num){
        MAX_CANDIDATES = num;
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
                    return Double.compare(costMatrix[index][integer], costMatrix[index][t1]);
                }
            });
        }

        for (ArrayList<Integer> line : candidatesTable){
            line.subList(nearestCount, line.size()).clear();
            line.trimToSize();
        }
    }

    private void genInitialTour(){
        int[] tour = new GreedyTSP(costMatrix).solve();
        tree = new TwoLevelTree(tour);
        objective = calculateObj();

//        ArrayList<Integer> tour = new ArrayList<>();
//        for (int i=0; i<costMatrix.length; ++i){
//            tour.add(i);
//        }
//        Collections.shuffle(tour, random);
//        tree = new TwoLevelTree(tour.stream().mapToInt(Integer::intValue).toArray());
//        objective = calculateObj();
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

    public void twoOptSolve(){
        genInitialTour();

        for (int iter=0;; ++iter){
            FlipMove flipMove = findMove();
            if (Double.compare(flipMove.deltaObj, 0) >= 0){
                break;
            }
            makeMove(flipMove);


            if (iter % 100 == 0){
                System.out.println(objective);
            }
        }
        double tmp = objective;
        if (Double.compare(tmp, calculateObj()) != 0 || Double.compare(tmp, checkCalcObjective()) !=0){
            throw new Error();
        }
        System.out.println("objective : " + objective);
    }

    private FlipMove findMove(){
        FlipMove bestFlipMove = new FlipMove(-1, -1, -1, -1, Double.MAX_VALUE);
        int bestCount = 0;

        int tourStart = tree.getHeadCityID();
        int tourEnd = tree.prev(tourStart);

        int a, b, c, d;
        for (a = tourStart; a != tree.prev(tree.prev(tourEnd)) ; a = tree.next(a)){
            b = tree.prev(a);
            for (c = tree.next(a); c!= tourEnd && tree.next(c) != b; c = tree.next(c)){
                d = tree.next(c);

                //if (!checkMove(a, b, c, d)) throw new Error();
                FlipMove flipMove = evaluateMove(a, b, c, d);
                int cmp = Double.compare(bestFlipMove.deltaObj, flipMove.deltaObj);
                if (cmp > 0){
                    bestFlipMove = flipMove;
                    bestCount = 1;
                }else if(cmp == 0 && random.nextInt(++bestCount) == 0){
                    bestFlipMove = flipMove;
                }
            }
        }
        return bestFlipMove;
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
        int[] tour = getCurrentTour();
        double obj = 0;
        for (int i=0; i< tour.length - 1; ++i){
            obj += costMatrix[tour[i]][tour[i+1]];
        }
        obj += costMatrix[tour[tour.length-1]][tour[0]];
        return obj;
    }

    public double solve(){
        genInitialTour();

        while(LKMove());

        System.out.println(objective);
        System.out.println(tree.checkTree());
        System.out.println(calculateObj());
        return objective;
    }

    private boolean LKMove(){
        int startCity = tree.getHeadCityID();
        int t1 = startCity;

        do{
            if (moveFromCity(t1)){
                return true;
            }
            t1 = tree.next(t1);
        }while(t1 != startCity);
        return false;
    }

    private boolean isFeasibleFlipMove(int t1, int t2, int t3, int t4){
        return t2 != t4 && t3 != t1 && t1 != t4
                && tree.between(t4, t2, t1) && tree.between(t1, t3, t4);
    }

    private boolean tryDuelMove(int t1, ArrayList<FlipMove> duelMoves, double sumDelta, int level){
        int t2 = tree.prev(t1);
        for (int t3 : candidatesTable.get(t2)){

            //tri move type 1
            int t4 = tree.next(t3);

            if (!isFeasibleFlipMove(t1, t2, t3, t4))continue;
            FlipMove fmv = evaluateMove(t1, t2, t3, t4);
            if (fmv.deltaObj + sumDelta < 0){
                makeMove(fmv);
                System.out.println(level + "-opt move! " + tree.checkTree() + " " + objective);
                return true;
            }else{
                duelMoves.add(fmv);
            }
        }
        return false;
    }

    private boolean tryTriMoveType1(FlipMove fmv, ArrayList<Edge> ys,
                                    ArrayList<ArrayList<FlipMove>> triMoves1){
        makeMove(fmv);
        ArrayList<FlipMove> nextDuelMove = new ArrayList<>();
        ArrayList<FlipMove> tmv1 = new ArrayList<>(2);
        ys.add(new Edge(fmv.b, fmv.c));
        if (fmv.a == tree.next(fmv.d)){
            if(tryDuelMove(fmv.a, nextDuelMove,fmv.deltaObj, 3)){
                return true;
            }
        }else{
            if (tryDuelMove(fmv.d, nextDuelMove, fmv.deltaObj, 3)){
                return true;
            }
        }
        ys.remove(ys.size()-1);
        if (fmv.a == tree.next(fmv.d) && fmv.b == tree.next(fmv.c)){
            makeMove(new FlipMove(fmv.a, fmv.d, fmv.c, fmv.d, -fmv.deltaObj));
        }else{
            makeMove(new FlipMove(fmv.d, fmv.a, fmv.b, fmv.c, -fmv.deltaObj));
        }
        for (FlipMove mv : nextDuelMove){
            tmv1.add(fmv);tmv1.add(mv);
        }
        triMoves1.add(tmv1);
        return false;
    }

    private boolean tryTriMoveType(final int t1, ArrayList<ArrayList<FlipMove>> triedMoves){
        final int t2 = tree.prev(t1);
        double x1 = costMatrix[t1][t2];
        for (int t3 : candidatesTable.get(t2)){
            if (t3 == t1)continue;
            double y1 = costMatrix[t2][t3];

            //evaluate type 1
            int t4 = tree.next(t3);
            double x2 = costMatrix[t4][t3];
            if (isFeasibleFlipMove(t1, t2, t3, t4)){
                for (int t5 : candidatesTable.get(t4)){
                    if (t5==t3 || t5==t2 || t5==t1)continue;
                    int t6; double x3, y3;
                    double y2 = costMatrix[t4][t5];
                    if (tree.between(t4, t5, t1)){
                        t6 = tree.prev(t5);
                    } else{
                        t6 = tree.next(t5);
                    }
                    if (t6==t4 || t6==t3||t6==t2||t6==t1)continue;
                    x3 = costMatrix[t5][t6];
                    y3 = costMatrix[t6][t1];

                    if (y1+y2+y3 < x1 + x2 + x3){
                        makeMove(evaluateMove(t1,t2,t3,t4));
                        makeMove(evaluateMove(t4, t1, t6, t5));
                        return true;
                    }else{
                        ArrayList<FlipMove> triMvs = new ArrayList<>(2);
                        triMvs.add(evaluateMove(t1,t2,t3,t4));
                        triMvs.add(evaluateMove(t4, t1, t6, t5));
                        triedMoves.add(triMvs);
                    }
                }
            }

            //evaluate type 2
            t4 = tree.prev(t3);
            x2 = costMatrix[t4][t3];
            if (t4 != t1){
                for (int t5 : candidatesTable.get(t4)){
                    if (!tree.between(t3,t5,t2))continue;
                    double x3, y3;
                    double y2 = costMatrix[t4][t5];
                    int t6 = tree.prev(t5);
                    if (t6 != t3){
                        x3 = costMatrix[t5][t6];
                        y3 = costMatrix[t6][t1];
                        if (y1+y2+y3 < x1 +x2 +x3){
                            makeMove(evaluateMove(t1,t2,t4,t3));
                            makeMove(evaluateMove(t4,t2,t6,t5));
                            makeMove(evaluateMove(t6,t2,t3,t1));
                            printLog(3 + "-opt move! " + tree.checkTree() + " " + objective);
                            return true;
                        }else {
                            ArrayList<FlipMove> triMvs = new ArrayList<>(3);
                            triMvs.add(evaluateMove(t1,t2,t3,t4));
                            triMvs.add(evaluateMove(t4,t2,t6,t5));
                            triMvs.add(evaluateMove(t6,t2,t3,t1));
                            triedMoves.add(triMvs);
                        }
                    }
                }
            }
        }
        return false;
    }

    private void printLog(String string){
        System.out.println(string);
    }

    private boolean moveFromCity(final int t1){
        ArrayList<Edge> ys = new ArrayList<>();
        ArrayList<ArrayList<FlipMove>> triMoves = new ArrayList<>();
        ArrayList<FlipMove> duelMoves = new ArrayList<>();

        if (tryDuelMove(t1, duelMoves, 0, 2)){
            return true;
        }


        if (tryTriMoveType(t1, triMoves)){
            return true;
        }
        return false;
        //return findNext2OptMove(t1, t2, ys, 0, 2);
    }

    private boolean findNext2OptMove(int a, int b,
                                     ArrayList<Edge> ys,
                                     double sumDelta, int level, final int maxLevel){
        ArrayList<FlipMove> searchedMoves = new ArrayList<>();
        for (int c : candidatesTable.get(b)) {
            int d = tree.next(c);
            if (ys.contains(new Edge(c, d))
                    ||
                    !isFeasibleFlipMove(a, b, c, d)) continue;

            FlipMove fmv = evaluateMove(a, b, c, d);
            if (fmv.deltaObj + sumDelta < 0) {
                makeMove(fmv);
                printLog(level + "-opt move! " + tree.checkTree() + " " + objective);
                return true;
            } else {
                searchedMoves.add(fmv);
            }

        }

        if(level < MAX_MOVE_LEVEL){
            for (FlipMove fmv : searchedMoves){
                makeMove(fmv);
                ys.add(new Edge(fmv.b, fmv.c));
                if (fmv.a == tree.next(fmv.d)){
                    if (findNext2OptMove(fmv.a, fmv.d, ys,
                            fmv.deltaObj + sumDelta,
                            level+1, maxLevel)){
                        return true;
                    }
                }else{
                    if (findNext2OptMove(fmv.d, fmv.a, ys,
                            fmv.deltaObj + sumDelta,
                            level+ 1, maxLevel)){
                        return true;
                    }
                }
                ys.remove(ys.size()-1);
                if (fmv.a == tree.next(fmv.d) && fmv.b == tree.next(fmv.c)){
                    makeMove(new FlipMove(fmv.a, fmv.d, fmv.c, fmv.b, -fmv.deltaObj));
                }else{
                    makeMove(new FlipMove(fmv.d, fmv.a, fmv.b, fmv.c, -fmv.deltaObj));
                }
            }
        }
        return false;
    }

    public int[] getCurrentTour(){
        return tree.getCurrentTour();
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
