package model;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

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

    public double getPeso() {
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
    private double peso;
    private Node leftNode;
    private Node rigthNode;

    public Node (char element , double peso) {
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

    public double getPeso() {
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
    private String originalText;
    private Bits compressedBin;
    private String compressedText;
    private StringBuilder deCompressedText;
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
        return deCompressedText.toString();
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

    public HashMap<Character, String> getTable() {
        return table;
    }

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
        compressedBin = new Bits();
        this.deCompressedText = new StringBuilder();
    }

    public long getTotal() {
        return total;
    }

    public byte[] getCompressedBin() {
        return compressedBin.getFinalArray();
    }

    private boolean buildDictionary(String input) {
        boolean status;
        this.total += input.length();
        try {
            ProgressMonitor progressMonitor = new ProgressMonitor("Building Dictionary");
            progressMonitor.start();
            for (int i = 0; i < input.length(); i++) {
                this.checkSymbol(input.charAt(i));
            }
            progressMonitor.endProcess();
            progressMonitor.join();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }


        return status;
    }

    private void buildTree() {
        ArrayList<HuffmanTree> trees = new ArrayList<>();
        ProgressMonitor progressMonitor = new ProgressMonitor("Building Huffman Tree");

        progressMonitor.start();

        for (Character c : symbols.keySet()) {
            trees.add(new HuffmanTree(new Node(c , ((double) symbols.get(c) / this.total))));
        }

        HuffmanTree[] treeArray = trees.toArray(new HuffmanTree[0]);

        Arrays.sort(treeArray, new Comparator<HuffmanTree>() {
            @Override
            public int compare(HuffmanTree tree1, HuffmanTree tree2) {

                int pesoComparison = Double.compare(tree1.getPeso(), tree2.getPeso());

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

                    int pesoComparison = Double.compare(tree1.getPeso(), tree2.getPeso());

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
        try{
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        this.originalText = input;
        buildDictionary(input);
        buildTree();
        buildTable();
        ProgressMonitor progressMonitor = new ProgressMonitor("Compressing");
        StringBuilder decodedText = new StringBuilder();
        progressMonitor.start();
        for (int i = 0; i < input.length(); i++) {
            String code = table.get(input.charAt(i));
            for (int j = 0; j < code.length(); j++) {
                compressedBin.addBit(code.charAt(j) - 48);
            }
            decodedText.append(code);
        }
        this.compressedText = decodedText.toString();
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deCompressBinary() {
        /** Ler tabela contendo os códigos de cada símbolo
         * Por vias de poupar tempo iremos aproveitar o fato da árvore estar em memória primária*/
        byte[] bytes = this.getCompressedBin();
        int size = bytes.length;
        AtomicLong i = new AtomicLong(1);
        ProgressMonitor progressMonitor = new ProgressMonitor("Decompressing File", i, size);
        progressMonitor.start();
        Node temp = this.huffmanTree.getRoot();
        byte shift = 0;
        while (i.get() < size){
            if(temp.getElement() != '\u001b') {
                this.deCompressedText.append(temp.getElement());
                temp = this.huffmanTree.getRoot();
            } else {
                if ((bytes[(int) i.get()] & 0b10000000) == 0) {
                    temp = temp.getLeftNode();
                } else {
                    temp = temp.getRigthNode();
                }
                if(shift == 7) {
                    i.incrementAndGet();
                    shift = 0;
                } else {
                    bytes[(int) i.get()] <<= 1;
                    shift++;
                }
            }
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(this.deCompressedText);
    }

    public void deCompressText() {
        int i = 0;
        ProgressMonitor progressMonitor = new ProgressMonitor("Decompressing file");
        progressMonitor.start();
        while (i < this.compressedText.length()) {
            i = putSymbol(i);
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        }catch (Exception e) {
            e.printStackTrace();
        }
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

        this.deCompressedText.append(temp.getElement());

        return index;
    }

    public void printBin() {
        this.compressedBin.print();
    }
}
