package lkhj;

import java.util.*;

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


    private boolean theMoveIsCloseTour(int[] t, int i) {

        if (t[0] != tree.next(t[1])) return false;
        for (int iter = 1; iter < i; ++iter) {
            if (t[iter] != tree.next(t[iter + 1]) && t[iter + 1] != tree.next(t[iter])) {
                return false;
            }
        }

        for (int iter = 0; iter < i * 2 - 1; ++iter) {
            for (int iter2 = iter + 1; iter2 < i * 2; ++iter2) {
                if (t[iter] == t[iter2]) return false;
            }
        }

        switch (i) {
            case 1:
                throw new Error("Cannot check when i==1 !");
            case 2:
                return t[3] == tree.next(t[2]);
            case 3:
                if (t[3] == tree.next(t[2])) {
                    if (tree.between(t[3], t[4], t[1])) {
                        return t[4] == tree.next(t[5]);
                    } else {
                        return t[5] == tree.next(t[4]);
                    }
                } else {
                    return (t[2] == tree.next(t[3]))
                            &&
                            (!tree.between(t[0], t[4], t[3]))
                            && (t[5] == tree.next(t[4]) || t[4] == tree.next(t[5]));
                }
            case 4:

                if (t[3] == tree.next(t[2])) {
                    if (tree.between(t[3], t[4], t[1])
                            && t[4] == tree.next(t[5])) {
                        if (tree.between(t[0], t[6], t[2]) || tree.between(t[3], t[6], t[5])) {
                            return t[7] == tree.next(t[6]);
                        } else {
                            return t[6] == tree.next(t[7]);
                        }
                    } else if (tree.between(t[0], t[4], t[2])
                            && t[5] == tree.next(t[4])) {
                        if (tree.between(t[0], t[6], t[4]) || tree.between(t[3], t[6], t[1])) {
                            return t[7] == tree.next(t[6]);
                        } else {
                            return t[6] == tree.next(t[7]);
                        }
                    } else {
                        return false;
                    }
                } else if (t[2] == tree.next(t[3])) {
                    if (tree.between(t[2], t[4], t[1])) {
                        if (t[4] == tree.next(t[5])) {
                            return t[7] == tree.next(t[6]);
                        } else {
                            if (tree.between(t[2], t[6], t[4]) || tree.between(t[5], t[6], t[1])) {
                                return t[6] == tree.next(t[7]);
                            } else {
                                return t[7] == tree.next(t[6]);
                            }
                        }
                    } else {
                        return false;
                    }
                }


            default:
                throw new Error("Not supported yet!");
        }
    }

    private boolean isInY(Edge[] y, Edge xi, int i){
        for (int iter = 0; iter < i; ++iter){
            if ((y[i].s == xi.s && y[i].d == xi.d)
                    ||
                    (y[i].d == xi.s && y[i].s == xi.d)){
                return true;
            }
        }
        return false;
    }

    private boolean lkSolve(){
        //int startCity = tree.getHeadCityID();
        int[] t = new int[10];
        Edge[] x = new Edge[5];
        Edge[] y = new Edge[5];
        double[] g = new double[5];

        t[0] = 0;
        step2:
        for (;;) {
            t[1] = tree.prev(t[0]);
            x[0] = new Edge(t[0],t[1]);
            for (int t1follow: candidatesTable.get(t[1])){
                t[2] = t1follow;
                y[0] = new Edge(t[1],t[2]);
                g[0] = costMatrix[t[0]][t[1]] - costMatrix[t[1]][t[2]];
                if (g[0] < 0)continue;
                for (int i=1; i<5; ++i){
                    t[2*i+1] = tree.next(t[2*i]);
                    x[i] = new Edge(t[2*i], t[2*i+1]);
                    if (theMoveIsCloseTour(t,i)){
                        //g[i] = costMatrix[t[2*i]][t[2*i+1]] - costMatrix
                    }
                }

            }
            t[0]  = (t[0]+1)%costMatrix.length;
            if (t[0] == 0)break;
        }
//        step2:
//        for(;;){
//            final int t2 = tree.prev(t1);
//            final double x1 = costMatrix[t1][t2];
//            for (int t3 : candidatesTable.get(t2)){
//                if (t3 == t1)continue;
//                final double y1 = costMatrix[t2][t3];
//                if (y1 > x1)continue;
//                final int t4 = tree.next(t3);
//                if (!isFeasibleFlipMove(t1,t2,t3,t4))continue;
//
//                final double x2 = costMatrix[t3][t4];
//                final double y2 = costMatrix[t4][t1];
//                if (y1 + y2 < x1 + x2){
//                    FlipMove fmv = new FlipMove(t1,t2,t3,t4, y1+y2-x1-x2);
//                    makeMove(fmv);
//                    printLog("2-opt move "+ tree.checkTree() + " " + objective);
//                    t1 = 0;
//                    continue step2;
//                }
//            }
//            t1 = (t1+1) % costMatrix.length;
//            if(t1 == 0)break;
//        }
        return false;

    }

    public double solve(){
        genInitialTour();

        //lkSolve();
        while(LKMove());

//        for(;;){
//            //int[] tour = getCurrentTour();
//            ArrayList<ArrayList<FlipMove>> tripleMoves = new ArrayList<>();
//            if (try2optMove()){
//                continue;
//            }
//            if (try3optMove(tripleMoves)){
//                continue;
//            }
//            break;
//        }

        System.out.println(objective);
        System.out.println(tree.checkTree());
        System.out.println(checkCalcObjective());
        return objective;
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

    private boolean try2optMove(){
        final int startCity = tree.getHeadCityID();
        int t1 = startCity;

        do{
            final int t2 = tree.prev(t1);
            for (int t3 : candidatesTable.get(t2)){
                final int t4 = tree.next(t3);
                if (!isFeasibleFlipMove(t1, t2, t3, t4))continue;
                FlipMove fmv = evaluateMove(t1,t2,t3,t4);
                if (fmv.deltaObj < 0){
                    makeMove(fmv);
                    printLog("2-opt move! " + tree.checkTree() + " " + objective);
                    return true;
                }
            }

            t1 = tree.next(t1);
        }while(t1 != startCity);
        return false;

//        for (int i=0; i<currTour.length-1; ++i){
//            final int t1, t2;
//            if (currTour[i] == tree.next(currTour[i+1])){
//                t1 = currTour[i]; t2 = currTour[i+1];
//            }else{
//                t1 = currTour[i+1]; t2 = currTour[i];
//            }
//            for (int t3 : candidatesTable.get(t2)){
//                final int t4 = tree.next(t3);
//                if (!isFeasibleFlipMove(t1, t2, t3, t4))continue;
//                FlipMove fmv = evaluateMove(t1,t2,t3,t4);
//                if (fmv.deltaObj < 0){
//                    makeMove(fmv);
//                    printLog("2-opt move! " + tree.checkTree() + " " + objective);
//                    return true;
//                }
//            }
//
//        }
//        return false;
    }

    private boolean try3optMove(ArrayList<ArrayList<FlipMove>> triedMoves){
        final int startCity = tree.getHeadCityID();
        int t1 = startCity;

        do{
            final int t2 = tree.prev(t1);

            double x1 = costMatrix[t1][t2];
            for (int t3 : candidatesTable.get(t2)){
                if (t3==t1)continue;
                final double y1 = costMatrix[t2][t3];

                //evaluate type 1
                int t4 = tree.next(t3);
                double x2 = costMatrix[t4][t3];
                if (!isFeasibleFlipMove(t1,t2,t3,t4)){
                    for (int t5 : candidatesTable.get(t4)){
                        if (t5==t3 || t5==t2 || t5==t1)continue;
                        final double y2 = costMatrix[t4][t5];
                        final int t6 = tree.between(t4, t5, t1) ? tree.prev(t5) : tree.next(t5);
                        if (t6==t4 || t6==t3 || t6==t2 || t6==t1)continue;
                        final double x3 = costMatrix[t5][t6];
                        final double y3 = costMatrix[t6][t1];

                        if (y1+y2+y3 < x1+x2+x3){
                            makeMove((evaluateMove(t1, t2, t3, t4)));
                            makeMove(evaluateMove(t4, t1, t6, t5));
                            printLog(3 + "-opt move! " + tree.checkTree() + " " + objective);
                            return true;
                        }else{
                            ArrayList<FlipMove> triMvs = new ArrayList<>(2);
                            triMvs.add(evaluateMove(t1,t2,t3,t4));
                            triMvs.add(evaluateMove(t4,t1,t6,t5));
                            triedMoves.add(triMvs);
                        }
                    }
                }

                //evaluate type 2
                t4 = tree.prev(t3);
                x2 = costMatrix[t4][t3];
                if (t4 == t1)continue;
                for (int t5 : candidatesTable.get(t4)){
                    if (!tree.between(t3,t5,t2))continue;
                    final int t6 = tree.prev(t5);
                    if (t6 == t3)continue;
                    final double y2 = costMatrix[t4][t5];
                    final double x3 = costMatrix[t5][t6];
                    final double y3 = costMatrix[t6][t1];
                    if (y1 + y2 + y3 < x1 + x2 +x3){
                        makeMove(evaluateMove(t1, t2, t4, t3));
                        makeMove(evaluateMove(t4, t2, t6, t5));
                        makeMove(evaluateMove(t6, t2, t3, t1));
                        printLog(3 + "-opt move! " + tree.checkTree() + " " + objective);
                        return true;
                    }else{
                        ArrayList<FlipMove> triMvs = new ArrayList<>(3);
                        triMvs.add(evaluateMove(t1,t2,t3,t4));
                        triMvs.add(evaluateMove(t4,t2,t6,t5));
                        triMvs.add(evaluateMove(t6,t2,t3,t1));
                        triedMoves.add(triMvs);
                    }
                }
            }

            t1 = tree.next(t1);
        }while(t1 != startCity);

        return false;

//        for (int i=0; i<currTour.length-1; ++i){
//            final int t1, t2;
//            if (currTour[i] == tree.next(currTour[i+1])){
//                t1 = currTour[i]; t2 = currTour[i+1];
//            }else{
//                t1 = currTour[i+1]; t2 = currTour[i];
//            }
//            double x1 = costMatrix[t1][t2];
//            for (int t3 : candidatesTable.get(t2)){
//                if (t3==t1)continue;
//                final double y1 = costMatrix[t2][t3];
//
//                //evaluate type 1
//                int t4 = tree.next(t3);
//                double x2 = costMatrix[t4][t3];
//                if (!isFeasibleFlipMove(t1,t2,t3,t4)){
//                    for (int t5 : candidatesTable.get(t4)){
//                        if (t5==t3 || t5==t2 || t5==t1)continue;
//                        final double y2 = costMatrix[t4][t5];
//                        final int t6 = tree.between(t4, t5, t1) ? tree.prev(t5) : tree.next(t5);
//                        if (t6==t4 || t6==t3 || t6==t2 || t6==t1)continue;
//                        final double x3 = costMatrix[t5][t6];
//                        final double y3 = costMatrix[t6][t1];
//
//                        if (y1+y2+y3 < x1+x2+x3){
//                            makeMove((evaluateMove(t1, t2, t3, t4)));
//                            makeMove(evaluateMove(t4, t1, t6, t5));
//                            printLog(3 + "-opt move! " + tree.checkTree() + " " + objective);
//                            return true;
//                        }else{
//                            ArrayList<FlipMove> triMvs = new ArrayList<>(2);
//                            triMvs.add(evaluateMove(t1,t2,t3,t4));
//                            triMvs.add(evaluateMove(t4,t1,t6,t5));
//                            triedMoves.add(triMvs);
//                        }
//                    }
//                }
//
//                //evaluate type 2
//                t4 = tree.prev(t3);
//                x2 = costMatrix[t4][t3];
//                if (t4 == t1)continue;
//                for (int t5 : candidatesTable.get(t4)){
//                    if (!tree.between(t3,t5,t2))continue;
//                    final int t6 = tree.prev(t5);
//                    if (t6 == t3)continue;
//                    final double y2 = costMatrix[t4][t5];
//                    final double x3 = costMatrix[t5][t6];
//                    final double y3 = costMatrix[t6][t1];
//                    if (y1 + y2 + y3 < x1 + x2 +x3){
//                        makeMove(evaluateMove(t1, t2, t4, t3));
//                        makeMove(evaluateMove(t4, t2, t6, t5));
//                        makeMove(evaluateMove(t6, t2, t3, t1));
//                        printLog(3 + "-opt move! " + tree.checkTree() + " " + objective);
//                        return true;
//                    }else{
//                        ArrayList<FlipMove> triMvs = new ArrayList<>(3);
//                        triMvs.add(evaluateMove(t1,t2,t3,t4));
//                        triMvs.add(evaluateMove(t4,t2,t6,t5));
//                        triMvs.add(evaluateMove(t6,t2,t3,t1));
//                        triedMoves.add(triMvs);
//                    }
//                }
//            }
//        }
//        return false;
    }

    private boolean tryDuelMove(int t1, double sumDelta, int level){

        int t2 = tree.prev(t1);
        final double x1 = costMatrix[t1][t2];
        for (int t3 : candidatesTable.get(t2)){
            if (t3 == t1)continue;
            final double y1 = costMatrix[t2][t3];
            if (y1 > x1)continue;
            int t4 = tree.next(t3);

            if (!isFeasibleFlipMove(t1, t2, t3, t4))continue;

            final double x2 = costMatrix[t3][t4];
            final double y2 = costMatrix[t4][t1];
            if (y1+y2 < x1+x2){
                FlipMove fmv = new FlipMove(t1,t2,t3,t4, y1+y2-x1-x2);
                makeMove(fmv);
                System.out.println(level + "-opt move! " + tree.checkTree() + " " + objective);
                return true;
            }
        }
        return false;
    }



    private boolean tryTriMoveType(final int t1, ArrayList<ArrayList<FlipMove>> triedMoves){
        final int t2 = tree.prev(t1);
        double x1 = costMatrix[t1][t2];
        for (int t3 : candidatesTable.get(t2)){
            if (t3 == t1)continue;
            double y1 = costMatrix[t2][t3];
            if (y1 > x1)continue;

            //evaluate type 1
            int t4 = tree.next(t3);
            double x2 = costMatrix[t4][t3];
            if (isFeasibleFlipMove(t1, t2, t3, t4)){
                for (int t5 : candidatesTable.get(t4)){
                    if (t5==t3 || t5==t2 || t5==t1)continue;
                    int t6; double x3, y3;
                    double y2 = costMatrix[t4][t5];
                    if (y2 > x2)continue;
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
                    final double y2 = costMatrix[t4][t5];
                    if (y2 > x2)continue;
                    int t6 = tree.prev(t5);
                    double x3, y3;
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
        int t2 = tree.prev(t1);
        return findNextMove(t1, t2, ys, 0, 2, MAX_MOVE_LEVEL, "");
    }

    private boolean tryT4IsNextT3(int t1, int t2, int t3, ArrayList<Edge> ys, double sumDelta, int level, int maxLevel, String star){
        final int t4 = tree.next(t3);
        if (ys.contains(new Edge(t3, t4))
                ||
                !isFeasibleFlipMove(t1, t2, t3, t4)) return false;

        FlipMove fmv = evaluateMove(t1, t2, t3, t4);
        if (fmv.deltaObj + sumDelta < 0) {
            makeMove(fmv);
            printLog(level + star+ "-opt move! " + tree.checkTree() + " " + objective);
            return true;
        } else if (level < maxLevel){
            makeMove(fmv);
            ys.add(new Edge(fmv.b, fmv.c));
            if (fmv.a == tree.next(fmv.d)){
                if (findNextMove(fmv.a, fmv.d, ys,
                        fmv.deltaObj + sumDelta,
                        level + 1, maxLevel, star)){
                    return true;
                }
            }else{
                if( findNextMove(fmv.d, fmv.a, ys,
                        fmv.deltaObj + sumDelta,
                        level + 1, maxLevel, star)){
                    return true;
                }
            }
            ys.remove(ys.size() - 1);
            if (fmv.a == tree.next(fmv.d) && fmv.b == tree.next(fmv.c)){
                makeMove(new FlipMove(fmv.a, fmv.d, fmv.c, fmv.b, -fmv.deltaObj));
            }else{
                makeMove(new FlipMove(fmv.d, fmv.a, fmv.b, fmv.c, -fmv.deltaObj));
            }
        }
        return false;
    }

    private boolean tryT4IsPrevT3(int t1, int t2, int t3, int maxLevel, String star){
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
            if (y2 > x2) continue;
            int t6 = t1 == tree.next(t2) ? tree.prev(t5) : tree.next(t5);
            if (t6==t3)continue;
            double x3 = costMatrix[t5][t6];
            double y3 = costMatrix[t6][t1];
            if (y1 +y2 +y3 < x1+ x2+x3){
                makeMove(evaluateMove(t1,t2,t4,t3));
                makeMove(evaluateMove(t4,t2,t6,t5));
                makeMove(evaluateMove(t6,t2,t3,t1));
                printLog("3*-opt move! " + tree.checkTree() + " " + objective);
                return true;
            }else{

                makeMove(evaluateMove(t1, t2, t4, t3));
                makeMove(evaluateMove(t4, t2, t6, t5));
                makeMove(evaluateMove(t6, t2, t3, t1));

                ArrayList<Edge> ys = new ArrayList<>();
                ys.add(new Edge(t1,t6));
                ys.add(new Edge(t2,t3));
                ys.add(new Edge(t4,t5));
                if (t1 == tree.next(t6)){
                    if(findNextMove(t1,t6, ys,
                            y1+y2+y3-x1-x2-x3,
                            4, maxLevel, "*")){
                        return true;
                    }
                }else{
                    if(findNextMove(t6,t1, ys,
                            y1+y2+y3-x1-x2-x3,
                            4, maxLevel, "*")){
                        return true;
                    }
                }
                makeMove(new FlipMove(t3,t2,t6,t1, x1+x2+x3-y1-y2-y3));
                makeMove(new FlipMove(t6,t2,t4,t5,0));
                makeMove(new FlipMove(t4,t2,t1,t3, 0));
            }
        }

        return false;
    }

    private boolean findNextMove(int t1, int t2,
                                 ArrayList<Edge> ys,
                                 double sumDelta, int level, final int maxLevel, String star){
        final double x1 = costMatrix[t1][t2];
        for (int t3 : candidatesTable.get(t2)) {
            if (t3 == t1)continue;
            final double y1 = costMatrix[t2][t3];
            if (y1 > x1)continue;
            if (tryT4IsNextT3(t1, t2, t3, ys, sumDelta, level, maxLevel, star)){
                return true;
            }
            if (level == 2 && tryT4IsPrevT3(t1,t2,t3,maxLevel, "*")){
                return true;
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
