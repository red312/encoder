package com.company.Encoder;

import java.io.*;
import java.sql.SQLOutput;
import java.util.*;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.Encoder.Huffman.*;


public class Encoder {
    public static List<Integer> encodeMtf(byte[] msg){
        ArrayList<Byte> table = new ArrayList<>();
        for (int i = 0; i < 256; i++){
            table.add((byte) i);
        }
        List<Integer> output = new LinkedList<>();
        for (byte b : msg) {
            for (int i = 0; i < table.size(); i++) {
                if (b == table.get(i)) {
                    output.add(i);
                    table.remove(i);
                    table.add(0, b);
                    break;
                }
            }
        }
        return output;
    }
    record TransformedData(byte[] last, int position) {
    }
    public static TransformedData bwt(byte[] s) {
        int n = s.length;
        int[] sa = suffixArray(s);
        byte[] last = new byte[n];
        int position = 0;
        for (int i = 0; i < n; i++) {
            last[i] = s[(sa[i] + n - 1) % n];
            if (sa[i] == 0)
                position = i;
        }
        return new TransformedData(last, position);
    }
    public static int[] suffixArray(byte[] S) {
        int n = S.length;
        Integer[] order = new Integer[n];
        for (int i = 0; i < n; i++)
            order[i] = i;
        Arrays.sort(order, Comparator.comparingInt(a -> Byte.toUnsignedInt(S[a])));
        int[] sa = new int[n];
        int[] classes = new int[n];
        for (int i = 0; i < n; i++) {
            sa[i] = order[i];
            classes[i] = S[i];
        }
        for (int len = 1; len < n; len *= 2) {
            int[] c = classes.clone();
            for (int i = 0; i < n; i++)
                classes[sa[i]] = i > 0 && c[sa[i - 1]] == c[sa[i]] && c[(sa[i - 1] + len / 2) % n] == c[(sa[i] + len / 2) % n] ? classes[sa[i - 1]] : i;
            int[] cnt = new int[n];
            for (int i = 0; i < n; i++)
                cnt[i] = i;
            int[] s = sa.clone();
            for (int i = 0; i < n; i++) {
                int s1 = (s[i] - len + n) % n;
                sa[cnt[classes[s1]]++] = s1;
            }
        }
        return sa;
    }
    public static void encodeFile(String args) throws IOException {
        String name = "";
        for (int i = args.length()-1 ; i > -1; i--){
            if (args.charAt(i) == '.'){
                name = args.substring(0, i);
                break;
            }
            else if (i == 0){
                name = args;
            }
        }
        FileInputStream fs = new FileInputStream(args);
        byte[] data = new byte[1000000];
        int len = fs.read(data, 0, data.length);
        data = Arrays.copyOf(data, len);
        TransformedData bwt = bwt(data);
        List<Integer> res = encodeMtf(bwt.last);
        System.out.println(Arrays.toString(bwt.last));
        TreeMap<Integer, Integer> frequancies = frequancy(res);
        ArrayList<Huffman.TreeNode> treeNodes = new ArrayList<>();
        for(Integer c: frequancies.keySet()){
            treeNodes.add(new Huffman.TreeNode(c, frequancies.get(c)));
        }
        Huffman.TreeNode tree = huffman(treeNodes);
        TreeMap<Integer, String> codes = new TreeMap<>();
        for(Integer c: frequancies.keySet()){
            codes.put(c, tree.getCode(c, ""));
        }
        String encoded = encode(codes, res);
        int co = 0;
        StringBuilder tmp = new StringBuilder();
        ArrayList<Byte> toBytes = new ArrayList<>();
        for (int i = 0; i < encoded.length(); i++){
            if (co < 8){
                tmp.append(encoded.charAt(i));
                co++;
            }
            else{
                toBytes.add((byte)(int)Integer.valueOf(tmp.toString(), 2));
                tmp = new StringBuilder("" + encoded.charAt(i));
                co = 1;
            }
            if (i == encoded.length()-1){
                toBytes.add((byte)(int)Integer.valueOf(tmp.toString(), 2));
            }
        }
        byte[] toWrite = new byte[toBytes.size()];
        for (int i = 0; i < toBytes.size(); i++){
            toWrite[i] = toBytes.get(i);
        }
        StringBuilder info = new StringBuilder();
        info.append(8-co).append("esc");
        info.append(";");
        info.append(bwt.position);
        info.append("esc");
        info.append(";");
        for (Integer key : frequancies.keySet()) {
            info.append(key).append(";").append(frequancies.get(key)).append(";");
        }
        info.append("esc");
        try(PrintWriter printWriter = new PrintWriter(new FileWriter(name + ".encoded"))){
            printWriter.println(info);
        }
        try(FileOutputStream writer = new FileOutputStream(name + ".encoded", true))
        {
            writer.write(toWrite);
            writer.flush();
        }
        catch(IOException ex){
            System.out.println(ex.getMessage());
        }
    }
}

