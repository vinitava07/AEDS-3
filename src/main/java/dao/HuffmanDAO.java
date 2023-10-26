package dao;

import model.Huffman;
import model.HuffmanV;

import java.io.RandomAccessFile;

public class HuffmanDAO {

    HuffmanV huffmanV;

    public HuffmanDAO() {
//        huffman = new Huffman();
    }
    //TODO: VERIFICAR O HUFFMAN DO ALEXANDRE, ESCREVER A ARVORE NO ARQUIVO
    public void createCompressedFile(String csvFileName) {
        try (RandomAccessFile raf = new RandomAccessFile(csvFileName, "rw")) {
            RandomAccessFile rafCompression = new RandomAccessFile("../resources/HuffmanCompression.bin", "rw");
            String line;
            StringBuilder sb = new StringBuilder();
            BitManipulationDAO bitManipulationDAO = new BitManipulationDAO();
            while ((line = raf.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            huffmanV = new HuffmanV(sb.toString());
            huffmanV.createCompressedText();
            System.out.println("cabo");
            int extraBits = huffmanV.compressedText.toString().length() % 8;
            for (int i = 0; i < extraBits; i++) {
                huffmanV.compressedText.append('0');
            }
            bitManipulationDAO.writeBytes(rafCompression, huffmanV.compressedText.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
