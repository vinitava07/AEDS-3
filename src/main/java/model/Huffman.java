package model;

import java.io.RandomAccessFile;
import java.util.*;
import util.*;

class HuffmanTree {
    private Node root;

    public HuffmanTree(Node root) {
        this.root = root;
    }

    public HuffmanTree(Node left , Node right) {
        this.root = new Node('\u001b' , left.getPeso() + right.getPeso());
        this.root.setLeftNode(left);
        this.root.setRigthNode(right);
    }

    public float getPeso() {
        return this.root.getPeso();
    }

    public Node getLeft() {
        return root.getLeftNode();
    }

    public Node getRight() {
        return root.getRigthNode();
    }

    public Node getRoot() {
        return root;
    }
}

class Node {
    private char element;
    private float peso;
    private Node leftNode;
    private Node rigthNode;

    public Node (char element , float peso) {
        this.element = element;
        this.peso = peso;
        this.leftNode = null;
        this.rigthNode = null;
    }

    public Node getLeftNode() {
        return leftNode;
    }

    public Node getRigthNode() {
        return rigthNode;
    }

    public char getElement() {
        return element;
    }

    public void setLeftNode(Node leftNode) {
        this.leftNode = leftNode;
    }

    public void setRigthNode(Node rigthNode) {
        this.rigthNode = rigthNode;
    }

    public float getPeso() {
        return peso;
    }

    public static void print(Node node , String s) {
        if (node != null) {

            if (node.element != '\u001b') {
                System.out.println(s + " " + node.element);
            }
            print(node.leftNode, s + "0");
            print(node.rigthNode , s + "1");
        }
    }
}
public class Huffman {
    private String compressedText;
    private String deCompressedText;
    private HuffmanTree huffmanTree;
    public class Element {
        private final char symbol;
        private int amount;

        private Element (char c) {
            this.symbol = c;
            this.amount = 1;
        }

        public int getAmount() {
            return amount;
        }

        public char getSymbol() {
            return symbol;
        }
    }

    public String getDeCompressedText() {
        return deCompressedText;
    }

    public String getCompressedText() {
        return compressedText;
    }

    public class TableElement {
        private String code;
        private char symbol;
        public TableElement(char symbol) {
            this.symbol = symbol;
            this.code = "";
        }
        public TableElement(char symbol , String path) {
            this.symbol = symbol;
            this.code = path;
        }

        public void addToPath(char c) {
            this.code += c;
        }

        public static String removeLastFromPath(String path) {
            StringBuilder stringBuilder = new StringBuilder(path);
            stringBuilder.deleteCharAt(path.length()-1);
            path = stringBuilder.toString();
            return path;
        }
    }
    private long total;
    private HashMap<Character , Integer> symbols;
    public HashMap<Character , String> table;
    private void checkSymbol(char c) {
        if(symbols.get((c)) == null) {
            symbols.put(c , 1);
        } else {
            symbols.replace(c , symbols.get(c) + 1);
        }
    }

    private void printSymbols() {
        System.out.println(symbols);
    }

    public Huffman() {
        symbols = new HashMap<Character, Integer>();
        total = 0;
        this.huffmanTree = new HuffmanTree(null);
        this.deCompressedText = "";
    }

    public long getTotal() {
        return total;
    }

    private boolean buildDictionary(String input) {
        boolean status;
        this.total += input.length();
        try {
//            ProgressBar progressBar = new ProgressBar("Building Dictionary" , input.length());
//            progressBar.startProcess();
            for (int i = 0; i < input.length(); i++) {
                this.checkSymbol(input.charAt(i));
//                progressBar.updateStatus(i);
            }
//            progressBar.done();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }


        return status;
    }

    private void buildTree() {
        ArrayList<HuffmanTree> trees = new ArrayList<>();
//        ProgressBar progressBar = new ProgressBar("Building Huffman Tree" , -1);

//        progressBar.startProcess();

        for (Character c : symbols.keySet()) {
            trees.add(new HuffmanTree(new Node(c , ((float) symbols.get(c) / this.total))));
        }

        HuffmanTree[] treeArray = trees.toArray(new HuffmanTree[0]);

        Arrays.sort(treeArray, new Comparator<HuffmanTree>() {
            @Override
            public int compare(HuffmanTree tree1, HuffmanTree tree2) {

                int pesoComparison = Float.compare(tree1.getPeso(), tree2.getPeso());

                if (pesoComparison == 0) {
                    return Character.compare(tree1.getRoot().getElement() , tree2.getRoot().getElement());
                }

                return pesoComparison;
            }
        });

        trees = new ArrayList<>(Arrays.stream(treeArray).toList());

        boolean flag = (trees.get(0).getPeso() >= 1);
        while (!flag) {
            ArrayList<HuffmanTree> aux = new ArrayList<>();

            treeArray = mergeNodes(trees , aux).toArray(new HuffmanTree[0]);
            Arrays.sort(treeArray, new Comparator<HuffmanTree>() {
                @Override
                public int compare(HuffmanTree tree1, HuffmanTree tree2) {

                    int pesoComparison = Float.compare(tree1.getPeso(), tree2.getPeso());

                    if (pesoComparison == 0) {
                        return Character.compare(tree1.getRoot().getElement(), tree2.getRoot().getElement());
                    }

                    return pesoComparison;
                }
            });

            aux = new ArrayList<>(Arrays.stream(treeArray).toList());

            trees = aux;

            flag = (trees.get(0).getPeso() >= 1);
        }

        this.huffmanTree = trees.get(0);
//        progressBar.done();
    }
    private ArrayList<HuffmanTree> mergeNodes(ArrayList<HuffmanTree> main, ArrayList<HuffmanTree> aux) {

        HuffmanTree auxNode;
        if (main.size() == 1) {
            return main;
        }
        auxNode = new HuffmanTree(main.get(0).getRoot(), main.get(1).getRoot());

        for (int i = 2; i < main.size(); i++) {
            aux.add(main.get(i));
        }
        sortStack(aux, auxNode);
        main.clear();
        return mergeNodes(aux, main);

    }
    private void sortStack(ArrayList<HuffmanTree> aux, HuffmanTree huffNode) {

        for (int i = 0; i < aux.size(); i++) {
            if (aux.get(i).getPeso() > huffNode.getPeso()) {
                aux.add(i, huffNode);
                return;
            }
        }
        aux.add(huffNode);
    }

    private void buildTable() {
        this.table = new HashMap<Character , String>();
        buildTable(this.huffmanTree.getRoot(), "");
    }

    private void buildTable(Node node , String path) {
        if(node.getLeftNode() == null) {
            if(node.getRigthNode() == null) this.table.put(node.getElement() , path);
            else buildTable(node.getRigthNode() , path+="1");
        } else {
            buildTable(node.getLeftNode() , path+="0");
            path = TableElement.removeLastFromPath(path);
            if(node.getRigthNode() != null) buildTable(node.getRigthNode(), path+="1");
        }
    }

    public void printTable() {
        System.out.println(this.table);
    }

    public void printTree() {
        Node.print(this.huffmanTree.getRoot() , "");
    }

    public void compare(String input) {
        for (int i = 0; i < input.length(); i++) {
            System.out.print(table.get(input.charAt(i)));
        }
        System.out.println('\n' + this.compressedText);
    }

    public void compressText(String input) {
        buildDictionary(input);
        buildTree();
        // printTree();
        buildTable();
        // printTable();
        StringBuilder decodedText = new StringBuilder();
//        ProgressBar progressBar = new ProgressBar("Compressing Text" , input.length());
//        progressBar.startProcess();
        for (int i = 0; i < input.length(); i++) {
            decodedText.append(table.get(input.charAt(i)));
//            progressBar.updateStatus(i);
        }

        this.compressedText = decodedText.toString();
//        progressBar.done();
    }

    public void deCompressText() {
        int i = 0;
        ProgressBar progressBar = new ProgressBar("Decompressing file" , this.compressedText.length());
        progressBar.startProcess();
        while (i < this.compressedText.length()) {
            i = putSymbol(i);
            // System.out.println(i);
            progressBar.updateStatus(i);
        }
        progressBar.done();
    }
    private int putSymbol(int index) {
        Node temp = this.huffmanTree.getRoot();
        boolean flag = temp == null;
        while (!flag) {
            if(this.compressedText.charAt(index++) == '0') temp = temp.getLeftNode();
            else temp = temp.getRigthNode();
            flag = ((temp.getElement() != '\u001b') || (index >= this.compressedText.length()));
        }
        assert temp != null;
        // System.out.print(temp.getElement() + "|");
        this.deCompressedText += temp.getElement();
        // System.out.println(this.deCompressedText);
        return index;
    }

}