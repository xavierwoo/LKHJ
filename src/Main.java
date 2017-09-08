import lkhj.InstanceReader;
import lkhj.LKHJ;
import lkhj.TwoLevelTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Main {

    static double getCost(double[][] mat, int i, int j){
        return i > j ? mat[i][j] : mat[j][i];
    }
    static private void writeInstance(double[][] mat) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("instance.tsp"));

        bw.write("NAME : tmpFile\n" +
                "TYPE : TSP\n" +
                "DIMENSION : " + mat.length + "\n" +
                "EDGE_WEIGHT_TYPE : EXPLICIT\n" +
                "EDGE_WEIGHT_FORMAT : FULL_MATRIX\n" +
                "EDGE_WEIGHT_SECTION\n");

        for (int i=0; i < mat.length; ++i){
            for (int j=0; j < mat.length; ++j){
                bw.write(String.valueOf((int)getCost(mat, i, j)) + " ");

            }
            bw.write("\n");
        }

        bw.write("EOF\n");
        bw.close();
    }

    static void setMatrix(double[][] mat, int i, int j, double value){
        if (i > j) {
            mat[i][j] = value;
        }else{
            mat[j][i] = value;
        }
    }

    static double[][] genRandomMatrix(int dimension, double min, double max, Random random){
        double[][] matrix = new double[dimension][];

        for (int i=0; i<dimension; ++i){
            matrix[i] = new double[i+1];
        }
        for (int i=0; i<matrix.length; ++i){
            for (int j=0; j<matrix[i].length; ++j){
                //double temp = min + (max - min) * random.nextDouble();
                double temp = min + random.nextInt((int)(max - min));
                setMatrix(matrix, i, j, temp);//matrix[i][j] = matrix[j][i] = temp;
            }
        }
        return matrix;
    }

    static void testRandom() throws IOException {
        Random random = new Random(1);

        int num = 1000;
        double[][] mat = genOptTandomMatrix(num, 2, 100, random);
        writeInstance(mat);
        LKHJ solver = new LKHJ(mat, new Random());
        //solver.twoOptSolve();
        solver.solve();
    }

    static void testFileInstance() throws IOException {
        LKHJ solver = new LKHJ(InstanceReader.ReadTSPInstance(
                "instances/xqg237.tsp"), new Random());
        solver.solve();
    }

    static double[][] genOptTandomMatrix(int dimension,double min, double max, Random random){
        double[][] matrix = genRandomMatrix(dimension, min + 1, max, random);

        ArrayList<Integer> optTour = new ArrayList<>(dimension);
        for (int i=0; i<dimension; ++i){
            optTour.add(i);
        }
        Collections.shuffle(optTour, random);
        for (int i=0; i<optTour.size() - 1; ++i){
            int a = optTour.get(i);
            int b = optTour.get(i+1);
            setMatrix(matrix, a, b, 1);//matrix[a][b] = matrix[b][a] = 1;
            if (random.nextInt(2) == 0){
                int c = random.nextInt(dimension);
                if (c != a && c != b){
                    setMatrix(matrix, a, c, 1);//matrix[a][c] = matrix[c][a] = 1;
                }
            }
        }

        setMatrix(matrix, optTour.get(optTour.size()-1), optTour.get(0), 1);
//        matrix[optTour.get(optTour.size() - 1)][optTour.get(0)]
//                = matrix[optTour.get(0)][optTour.get(optTour.size() - 1)] = 1;
        return matrix;
    }

    static private double[][] genDecreasingMatrix(int num){
        double[][] matrix = new double[num][num];

        for (int i=0; i<matrix.length; ++i){
            for (int j=i+1; j<matrix[i].length; ++j){
                matrix[i][j] = matrix[j][i] = matrix.length -j;
            }
        }
        return matrix;
    }

    public static void main(String[] args) throws IOException {
//        testRandom();
        testFileInstance();
//
//        int[] tour = new int[200];
//
//        for (int i=0; i < tour.length; ++i){
//            tour[i] = i;
//        }

//        TwoLevelTree tree = new TwoLevelTree(tour);
//        tree.printTour();
//        tree.nonSequ4Exchange2(0,1,7,8,11,12,3,4);
//        tree.printTour();
//        System.out.println(tree.checkTree());


////
//        TwoLevelTree tree = new TwoLevelTree(tour);
//        for (int t1 = 0; t1 < tour.length-4; ++t1){
//            int t2 = t1+1;
//            for (int t3 = t2 + 2; t3 < tour.length - 3; ++t3){
//                int t4 = t3 + 1;
//                for (int t5 = t4; t5 < tour.length - 2; ++t5){
//                    int t6 = t5+1;
//                    for (int t7 = t2; t7 < t3-1; ++t7){
//                        int t8 = t7+1;
//
//                        int[] t = tree.getCurrentTour();
//                        tree.nonSequ4Exchange(t[t1], t[t2], t[t3], t[t4], t[t5], t[t6],t[t7],t[t8]);
//                        //tree.printTour();
//                        if  (!tree.checkTree()){
//                            System.out.println(t1 +" " +
//                                    t2 + " " + t3 + " " + t4 + " " +
//                                    t5 + " " + t6 + " " + t7 + " " + t8);
//                            throw new Error();
//                        }
//                    }
//                }
//            }
//        }


    }
}
