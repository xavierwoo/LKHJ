import lkhj.LKHJ;
import lkhj.OneTree;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {

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
                bw.write(String.valueOf((int)mat[i][j]) + " ");

            }
            bw.write("\n");
        }

        bw.write("EOF\n");
        bw.close();
    }

    static double[][] genRandomMatrix(int dimension, double min, double max, Random random){
        double[][] matrix = new double[dimension][dimension];

        for (int i=0; i<matrix.length; ++i){
            for (int j=i+1; j<matrix[i].length; ++j){
                //double temp = min + (max - min) * random.nextDouble();
                double temp = min + random.nextInt((int)(max - min));
                matrix[i][j] = matrix[j][i] = temp;
            }
        }
        return matrix;
    }

    public static void main(String[] args) throws IOException {
	// write your code here
        Random random = new Random(0);

        int num = 1000;
        double[][] mat = genRandomMatrix(num, 1, 100, random);
        writeInstance(mat);
        LKHJ solver = new LKHJ(mat, new Random());
        //solver.twoOptSolve();
        solver.solve();
    }
}
