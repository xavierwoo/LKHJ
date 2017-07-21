package lkhj;


import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by xinyun on 19/07/17.
 */
public class OneTree {

    double[][] costMatrix;
    double[] pi;
    TreeNode[] treeNodes; //index 1 is the root
    int[] specialConnections = new int[2];
    double treeLength;
    public OneTree(double[][] costMatrix, double[] pi){
        this.costMatrix = costMatrix;
        treeNodes = new TreeNode[costMatrix.length];
        this.pi = pi;
        treeLength = getTreeLength();
    }

    private class TreeNode{
        TreeNode father;
        int data;
        ArrayList<TreeNode> children = new ArrayList<>();

        TreeNode(int data, TreeNode father){
            this.father = father;
            this.data = data;
            if (father != null){
                father.children.add(this);
            }
        }

        TreeNode addChild(int data){
            return new TreeNode(data, this);
        }
    }

    private double getTreeLength(){
        double spinningTreeLength = findSpinningTree();
        double min[] = new double[2];
        min[0] = min[1] = Double.MAX_VALUE;
        for (int i=1; i<costMatrix.length; ++i){
            double edgeCost = costMatrix[0][i] + pi[i] + pi[0];
            if (Double.compare(edgeCost, min[0]) < 0
                    && Double.compare(edgeCost, min[1]) < 0){
                if (Double.compare(min[0], min[1]) < 0){
                    min[1] = edgeCost;
                    specialConnections[1] = i;
                }else{
                    min[0] = edgeCost;
                    specialConnections[0] = i;
                }
            }else if (Double.compare(edgeCost, min[0])<0
                    && Double.compare(edgeCost, min[1]) > 0 ){
                min[0] = edgeCost;
                specialConnections[0] = i;
            }else if (Double.compare(edgeCost, min[0]) > 0
                    && Double.compare(edgeCost, min[1]) < 0){
                min[1] = edgeCost;
                specialConnections[1] = i;
            }

        }
        treeLength = spinningTreeLength + min[0] + min[1];
        for (double pii : pi){
            treeLength -= 2* pii;
        }
        return treeLength;
    }


    private double findSpinningTree(){
        HashSet<Integer> unReachedNodes = new HashSet<>();
        for (int node = 2; node < costMatrix.length; ++node){
            unReachedNodes.add(node);
        }
        HashSet<Integer> reachedNodes = new HashSet<>();
        reachedNodes.add(1);
        treeNodes[1] = new TreeNode(1, null);
        double spinningTreeLength = 0;
        while(!unReachedNodes.isEmpty()){
            double minCost = Double.MAX_VALUE;
            int[] minEdge = new int[2];
            for (Integer uNode : unReachedNodes){
                for (Integer rNode : reachedNodes){
                    double edgeCost = costMatrix[uNode][rNode] + pi[uNode] + pi[rNode];
                    if (Double.compare(edgeCost, minCost) < 0){
                        minCost = edgeCost;
                        minEdge[0] = uNode;
                        minEdge[1] = rNode;
                    }
                }
            }
            unReachedNodes.remove(minEdge[0]);
            reachedNodes.add(minEdge[0]);
            spinningTreeLength += minCost;
            treeNodes[minEdge[0]] = treeNodes[minEdge[1]].addChild(minEdge[0]);
        }
        return spinningTreeLength;
    }

    int getDegree(int i){
        if (i == 0){
            return 2;
        }else if (i ==1){
            return treeNodes[1].children.size()
                    + (specialConnections[0] == 1 ? 1 : 0)
                    + (specialConnections[1] == 1 ? 1 : 0);
        }else{
            return treeNodes[i].children.size() +1
                    + (specialConnections[0] == i ? 1 : 0)
                    + (specialConnections[1] == i ? 1 : 0);
        }
    }

    double calcTreeLength(TreeNode root){
        if (root.children.isEmpty()){
            return 0;
        }else{
            double cost = 0;
            for (TreeNode child : root.children){
                cost += costMatrix[root.data][child.data] + calcTreeLength(treeNodes[child.data]);
            }
            return cost;
        }
    }

    double checkTreeLength(){
        return calcTreeLength(treeNodes[1]) + costMatrix[0][specialConnections[0]] + costMatrix[0][specialConnections[1]];
    }
}
