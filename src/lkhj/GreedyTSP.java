package lkhj;


/**
 * A greedy algorithm for TSP problem
 * Created by xinyun on 03/07/17.
 */
class GreedyTSP {
    private double[][] costMatrix;
    private int[] tour;
    private boolean[] isInTour;

    GreedyTSP(double[][] costMatrix){
        this.costMatrix = costMatrix;
        tour = new int[costMatrix.length];
        isInTour = new boolean[costMatrix.length];
    }

    int[] solve(){
        tour[0] = 0;
        isInTour[0] = true;
        int count = 1;
        while(count < tour.length){
            int currN = tour[count-1];
            int nextN = findNearest(currN);
            tour[count] = nextN;
            isInTour[count] = true;
            ++count;
        }
        return tour;
    }

    private int findNearest(int n){
        double smallestF = Double.MAX_VALUE;
        int nearestN = -1;

        for (int i=0; i < tour.length; ++i){
            if (i != n && !isInTour[i]){
                double f = costMatrix[n][i];
                if (f < smallestF){
                    smallestF = f;
                    nearestN = i;
                }
            }
        }
        return nearestN;
    }
}
