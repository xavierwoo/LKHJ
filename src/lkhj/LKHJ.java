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

    private void printLog(String string){
        System.out.println(string);
    }


    private boolean moveFromCity(final int t1){
        ArrayList<Edge> ys = new ArrayList<>();
        int t2 = tree.prev(t1);
        return findNextMove(t1, t2, ys, 0, 2, MAX_MOVE_LEVEL, "");
    }

    private boolean tryT4IsNextT3(int t1, int t2, int t3,
                                  ArrayList<Edge> ys, double sumDelta, int level, int maxLevel, String star){
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
                ys.subList(ys.size()-3, ys.size());
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
            if (level == 2 && tryT4IsPrevT3(t1,t2,t3,maxLevel)){
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
