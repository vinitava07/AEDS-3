package dao;

import model.Bits;
import model.Huffman;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;

public class HuffmanDAO {

    private long compressedSize;
    Huffman huffman;

    public HuffmanDAO() {
        huffman = new Huffman();
        compressedSize = 0;
    }

    public long getCompressedSize() {
        return compressedSize;
    }

    //TODO: VERIFICAR O HUFFMAN DO ALEXANDRE, ESCREVER A ARVORE NO ARQUIVO
    public void compressFile(String csvFileName) {
        try (RandomAccessFile raf = new RandomAccessFile(csvFileName, "rw")) {
            StringBuilder sb = new StringBuilder();
            while(raf.getFilePointer() < raf.length()) {
                sb.append(raf.readLine() + '\n');
            }
            huffman.compressText(sb.toString());
            File dir = new File("../resources/huffman/");
            if (writeCompressed(dir)) {
                System.out.println("Completed!!");
            } else {
                System.out.println("Failed to compress File!!");
            }
            this.compressedSize = new File(dir.getAbsolutePath() + "\\HuffmanCompression.bin").length();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean writeCompressed(File dir) {
        boolean status = false;
        if(dir.exists()) {
            deleteDir(dir);
            dir.delete();
        }
        if (dir.mkdir()) {
            try (RandomAccessFile raf1 = new RandomAccessFile(dir.getAbsolutePath() + "\\HuffmanCompression.bin", "rw")) {
                raf1.write(huffman.getCompressedBin());
                RandomAccessFile tree = new RandomAccessFile(dir.getAbsolutePath() + "\\tree.bin" , "rw");
                HashMap<Character , String> table = huffman.getTable();
                for (Character c :
                        table.keySet()) {
                    tree.writeUTF(c + ":" + table.get(c));
                }
                tree.close();
                status = true;
            } catch (Exception e) {
                status = false;
                e.printStackTrace();
            }
        } else {
            System.out.println("Deu ruim!!!!");
            System.out.println(dir.getAbsolutePath());
        }
        return status;
    }
    private static void deleteDir(File dir) {
        File[] files = dir.listFiles();

        assert files != null;
        for (File myFile: files) {
            if (myFile.isDirectory()) {
                deleteDir(myFile);
            }
            myFile.delete();

        }
    }


    public boolean deCompressFile() {
        try (RandomAccessFile raf = new RandomAccessFile("../resources/huffman/HuffmanCompression.bin" , "r")) {
            byte[] bytes = new byte[(int) raf.length()];
            raf.read(bytes);
            Bits bits = new Bits();
            bits.setBitsArray(bytes);

        }  catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
