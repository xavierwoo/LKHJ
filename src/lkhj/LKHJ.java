package lkhj;

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

    public LKHJ(double[][] costMatrix, Random random){
        this.costMatrix = costMatrix;
        this.random = random;
    }

    private void genInitialTour(){
        int[] tour = new GreedyTSP(costMatrix).solve();
        tree = new TwoLevelTree(tour);
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

    public void solve(){
        genInitialTour();

        for (int iter=0;; ++iter){
            Move move = findMove();
            if (Double.compare(move.deltaObj, 0) >= 0){
                break;
            }
            makeMove(move);


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

    private Move findMove(){
        Move bestMove = new Move(-1, -1, -1, -1, Double.MAX_VALUE);
        int bestCount = 0;

        int tourStart = tree.getHeadCityID();
        int tourEnd = tree.prev(tourStart);

        int a, b, c, d;
        for (a = tourStart; a != tree.prev(tree.prev(tourEnd)) ; a = tree.next(a)){
            b = tree.prev(a);
            for (c = tree.next(a); c!= tourEnd && tree.next(c) != b; c = tree.next(c)){
                d = tree.next(c);

                //if (!checkMove(a, b, c, d)) throw new Error();
                Move move = evaluateMove(a, b, c, d);
                int cmp = Double.compare(bestMove.deltaObj, move.deltaObj);
                if (cmp > 0){
                    bestMove = move;
                    bestCount = 1;
                }else if(cmp == 0 && random.nextInt(++bestCount) == 0){
                    bestMove = move;
                }
            }
        }
        return bestMove;
    }

    private void makeMove(Move move){
        tree.flip(move.a, move.b, move.c, move.d);
        objective += move.deltaObj;
    }

    private Move evaluateMove(int a, int b, int c, int d){
        return new Move(a, b, c, d,
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

    public int[] getCurrentTour(){
        return tree.getCurrentTour();
    }

    private class Move{
        final int a;
        final int b;
        final int c;
        final int d;
        final double deltaObj;
        Move(int a, int b, int c, int d, double deltaObj){
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.deltaObj = deltaObj;
        }
    }
}
