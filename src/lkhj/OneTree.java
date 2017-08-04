package lkhj;


import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xinyun on 19/07/17.
 */
public class OneTree {

    double treeLength;
    private double[][] costMatrix;
    private double[] pi;
    private TreeNode[] treeNodes;
    private int[] specialConnections = new int[2];
    private int root = -1; //index of root

    OneTree(double[][] costMatrix, double[] pi){
        this.costMatrix = costMatrix;
        treeNodes = new TreeNode[costMatrix.length];
        this.pi = pi;
        treeLength = getTreeLength();
    }

    private double getTreeLength(){
        double spinningTreeLength = findSpinningTree();
       // treeNodes = new TreeNode[costMatrix.length];
//        double spinningTreeLength = findSpinningTreeOld();
//        if (Double.compare(spinningTreeLength, spinningTreeLength2) != 0){
//            throw new Error("spinningTree");
//        }
        double min[] = new double[2];
        min[0] = min[1] = Double.MAX_VALUE;
        for (int i=1; i<costMatrix.length; ++i){
            double edgeCost = costMatrix[0][i] + pi[i] + pi[0];
            if (edgeCost < min[0] && edgeCost < min[1]){
                if (min[0] < min[1]){
                    min[1] = edgeCost;
                    specialConnections[1] = i;
                }else{
                    min[0] = edgeCost;
                    specialConnections[0] = i;
                }
            }else if (edgeCost < min[0] && edgeCost > min[1]){
                min[0] = edgeCost;
                specialConnections[0] = i;
            }else if (edgeCost > min[0] && edgeCost < min[1]){
                min[1] = edgeCost;
                specialConnections[1] = i;
            }
        }

        treeLength = spinningTreeLength + min[0] + min[1];
        for (double pii : pi){
            treeLength -= 2 * pii;
        }
        treeLength -= 1;
        //treeLength = calcTreeLength();
        return treeLength;
    }

    private double findSpinningTree(){
        double[] C = new double[costMatrix.length];
        Arrays.fill(C, Double.MAX_VALUE);
        int[] E = new int[costMatrix.length];
        Arrays.fill(E, -1);
        FibonacciHeap<Integer> Q = new FibonacciHeap<>();
        ArrayList<FibonacciHeapNode<Integer>> fbNodes = new ArrayList<>();
        for (int i=1; i< costMatrix.length; ++i){
            FibonacciHeapNode<Integer> node = new FibonacciHeapNode<>(i);
            Q.insert(node, C[i]);
            fbNodes.add(node);
        }
        boolean[] isInQ = new boolean[costMatrix.length];
        Arrays.fill(isInQ, true);
        isInQ[0] = false;

        double spinningTreeLength = 0;

        while(!Q.isEmpty()){
            int v = Q.removeMin().getData();
            isInQ[v] = false;
            TreeNode vFather = E[v] > -1 ? treeNodes[E[v]] : null;
            if (vFather == null){
                root = v;
                treeNodes[v] = new TreeNode(v, null);
            }else {
                spinningTreeLength += C[v];
                treeNodes[v] = vFather.addChild(v);
            }


            for (int w=1; w<costMatrix.length; ++w){
                if (w == v || !isInQ[w])continue;
                double edgeLength = costMatrix[v][w] + pi[v] + pi[w];
                if (edgeLength < C[w]){
                    C[w] = edgeLength;
                    Q.decreaseKey(fbNodes.get(w-1), C[w]);
                    E[w] = v;
                }
            }
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

//    private double findSpinningTreeOld(){
//        HashSet<Integer> unReachedNodes = new HashSet<>();
//        for (int node = 2; node < costMatrix.length; ++node){
//            unReachedNodes.add(node);
//        }
//        HashSet<Integer> reachedNodes = new HashSet<>();
//        reachedNodes.add(1);
//        treeNodes[1] = new TreeNode(1, null);
//        double spinningTreeLength = 0;
//        while(!unReachedNodes.isEmpty()){
//            double minCost = Double.MAX_VALUE;
//            int[] minEdge = new int[2];
//            for (Integer uNode : unReachedNodes){
//                for (Integer rNode : reachedNodes){
//                    double edgeCost = costMatrix[uNode][rNode] + pi[uNode] + pi[rNode];
//                    if (Double.compare(edgeCost, minCost) < 0){
//                        minCost = edgeCost;
//                        minEdge[0] = uNode;
//                        minEdge[1] = rNode;
//                    }
//                }
//            }
//            unReachedNodes.remove(minEdge[0]);
//            reachedNodes.add(minEdge[0]);
//            spinningTreeLength += minCost;
//            treeNodes[minEdge[0]] = treeNodes[minEdge[1]].addChild(minEdge[0]);
//        }
//        return spinningTreeLength;
//    }

    boolean hasEdge(int a, int b){
        if (a == 0){
            return specialConnections[0] == b || specialConnections[1] == b;
        }else if (b==0){
            return specialConnections[0] == a || specialConnections[1] == a;
        }else {
            return treeNodes[a].father == treeNodes[b] || treeNodes[b].father == treeNodes[a];
        }
    }

//    private double calcTreeLength(TreeNode root){
//        if (root.children.isEmpty()){
//            return 0;
//        }else{
//            double cost = 0;
//            for (TreeNode child : root.children){
//                cost += costMatrix[root.data][child.data] + calcTreeLength(treeNodes[child.data]);
//            }
//            return cost;
//        }
//    }

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

//    private double calcTreeLength(){
//        return calcTreeLength(treeNodes[root]) + costMatrix[0][specialConnections[0]] + costMatrix[0][specialConnections[1]];
//    }
}
