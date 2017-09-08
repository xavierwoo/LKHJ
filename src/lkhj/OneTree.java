package lkhj;


import org.jgrapht.util.FibonacciHeap;
import org.jgrapht.util.FibonacciHeapNode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by xinyun on 19/07/17.
 */
class OneTree {

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

        double maxSpecial = -Double.MAX_VALUE;
        for (TreeNode tn : treeNodes){
            if (!tn.children.isEmpty())continue;
            double minDistance = Double.MAX_VALUE;
            int nearestNode = -1;
            for (int n = 0; n < costMatrix.length; ++n){
                if (n != tn.father.data && n != tn.data && getCost(tn.data, n) < minDistance){
                    minDistance = getCost(tn.data, n);//costMatrix[tn.data][n];
                    nearestNode = n;
                }
            }
            if(minDistance > maxSpecial){
                maxSpecial = minDistance;
                specialConnections[0] = tn.data;
                specialConnections[1] = nearestNode;
            }
        }

        treeLength = spinningTreeLength + maxSpecial;

        for (double pii : pi){
            treeLength -= 2 * pii;
        }
        return treeLength;
    }

    private double findSpinningTree(){
        double[] C = new double[costMatrix.length];
        Arrays.fill(C, Double.MAX_VALUE);
        int[] E = new int[costMatrix.length];
        Arrays.fill(E, -1);
        FibonacciHeap<Integer> Q = new FibonacciHeap<>();
        ArrayList<FibonacciHeapNode<Integer>> fbNodes = new ArrayList<>();
        for (int i=0; i< costMatrix.length; ++i){
            FibonacciHeapNode<Integer> node = new FibonacciHeapNode<>(i);
            Q.insert(node, C[i]);
            fbNodes.add(node);
        }
        boolean[] isInQ = new boolean[costMatrix.length];
        Arrays.fill(isInQ, true);
        //isInQ[0] = false;

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


            for (int w=0; w<costMatrix.length; ++w){
                if (w == v || !isInQ[w])continue;
                double edgeLength = getCost(v, w) + pi[v] + pi[w];
                if (edgeLength < C[w]){
                    C[w] = edgeLength;
                    Q.decreaseKey(fbNodes.get(w), C[w]);
                    E[w] = v;
                }
            }
        }

        return spinningTreeLength;
    }

    int getDegree(int i){
        return treeNodes[i].children.size() + (treeNodes[i].father != null ? 1 : 0)
                + (specialConnections[0] == i ? 1 : 0) + (specialConnections[1] == i ? 1 : 0);
    }

    boolean hasEdge(int a, int b){
        if (a == 0){
            return specialConnections[0] == b || specialConnections[1] == b;
        }else if (b==0){
            return specialConnections[0] == a || specialConnections[1] == a;
        }else {
            return treeNodes[a].father == treeNodes[b] || treeNodes[b].father == treeNodes[a];
        }
    }

    private double getCost(int i, int j){
        return i > j ? costMatrix[i][j] : costMatrix[j][i];
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

}
