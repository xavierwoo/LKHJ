package lkhj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InstanceReader {
    static public double[][] ReadTSPInstance(String file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));

        String line;
        int dimension = 0;
        Pattern p = Pattern.compile("DIMENSION : (\\d+)");

        while (!(line = br.readLine()).equals("NODE_COORD_SECTION")) {
            Matcher m = p.matcher(line);
            if (m.matches()) {
                dimension = Integer.valueOf(m.group(1));
            }
        }

        double[][] mat = new double[dimension][dimension];
        double[][] coors = new double[dimension][2];
        int count = 0;
        while (!(line = br.readLine()).equals("EOF")) {
            String value[] = line.split(" ");
            coors[count][0] = Double.valueOf(value[1]);
            coors[count][1] = Double.valueOf(value[2]);
            ++count;
        }

        for (int i = 0; i < dimension; ++i) {
            for (int j = i + 1; j < dimension; ++j) {
                mat[i][j] = mat[j][i] = Math.round(Math.sqrt(
                        (coors[i][0] - coors[j][0]) * (coors[i][0] - coors[j][0])
                                + (coors[i][1] - coors[j][1]) * (coors[i][1] - coors[j][1])));
            }
        }
        return mat;
    }
}
