package com.company.Encoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class Huffman {
    static class TreeNode implements Comparable<TreeNode> {
        Integer symbol;
        int nodeWeight;
        TreeNode left;
        TreeNode right;

        public TreeNode(Integer symbol, int nodeWeight) {
            this.nodeWeight = nodeWeight;
            this.symbol = symbol;
        }

        public TreeNode(Integer symbol, int nodeWeight, TreeNode left, TreeNode right) {
            this(symbol, nodeWeight);
            this.left = left;
            this.right = right;
        }

        public String getCode(Integer ch, String parentPath) {
            if (symbol == ch) {
                return parentPath;
            } else {
                if (left != null) {
                    String path = left.getCode(ch, parentPath + 0);
                    if (path != null) {
                        return path;
                    }
                }
                if (right != null) {
                    return right.getCode(ch, parentPath + 1);
                }
            }
            return null;
        }

        @Override
        public int compareTo(TreeNode o) {
            return o.nodeWeight - nodeWeight;
        }
    }

    public static TreeNode huffman(ArrayList<TreeNode> treeNodes) {
        while (treeNodes.size() > 1) {
            Collections.sort(treeNodes);
            TreeNode left = treeNodes.remove(treeNodes.size() - 1);
            TreeNode right = treeNodes.remove(treeNodes.size() - 1);
            TreeNode parent = new TreeNode(null, right.nodeWeight + left.nodeWeight, left, right);
            treeNodes.add(parent);
        }
        return treeNodes.get(0);
    }

    public static String encode(TreeMap<Integer, String> codes, List<Integer> ls) {
        StringBuilder bld = new StringBuilder();
        ls.forEach(integer -> bld.append(codes.get(integer)));
        return bld.toString();
    }

    public static TreeMap<Integer, Integer> frequancy(List<Integer> ls) {
        TreeMap<Integer, Integer> freqMap = new TreeMap<>();
        for (int i : ls) {
            Integer count = freqMap.get(i);
            freqMap.put(i, count != null ? count + 1 : 1);
        }
        return freqMap;
    }
}


