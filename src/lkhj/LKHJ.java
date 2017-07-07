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
    private int MAX_CANDIDATES = 9;
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
//        int[] tour = new GreedyTSP(costMatrix).solve();
//        tree = new TwoLevelTree(tour);
//        objective = calculateObj();

        ArrayList<Integer> tour = new ArrayList<>();
        for (int i=0; i<costMatrix.length; ++i){
            tour.add(i);
        }
        Collections.shuffle(tour, random);
        tree = new TwoLevelTree(tour.stream().mapToInt(Integer::intValue).toArray());
        objective = calculateObj();
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
        tree.flip(flipMove.a, flipMove.b, flipMove.c, flipMove.d);
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

    private boolean moveFromCity(final int t1){
        ArrayList<Edge> ys = new ArrayList<>();

        int t2 = tree.prev(t1);
        return findNext2OptMove(t1, t2, ys, 0, 2);
    }

    private boolean findNext2OptMove(int a, int b,
                                     ArrayList<Edge> ys,
                                     double sumDelta, int level){
        for (int c : candidatesTable.get(b)){
            int d = tree.next(c);
            if (ys.contains(new Edge(c, d))
                    ||
                    !isFeasibleFlipMove(a, b, c, d)) continue;

            FlipMove fmv = evaluateMove(a, b, c, d);
            if (fmv.deltaObj + sumDelta < 0){
                makeMove(fmv);
                System.out.println(level + "opt move!");
                return true;
            }else if(level < MAX_MOVE_LEVEL){
                makeMove(fmv);
                ys.add(new Edge(b, c));
                if (a == tree.next(d)){
                    if (findNext2OptMove(a, d, ys,
                            fmv.deltaObj + sumDelta,
                            level+1)){
                        return true;
                    }
                }else{
                    if (findNext2OptMove(d, a, ys,
                            fmv.deltaObj + sumDelta,
                            level+ 1)){
                        return true;
                    }
                }
                ys.remove(ys.size()-1);
                if (a == tree.next(d) && b == tree.next(c)){
                    makeMove(new FlipMove(a, d, c, b, -fmv.deltaObj));
                }else{
                    makeMove(new FlipMove(d, a, b, c, -fmv.deltaObj));
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
