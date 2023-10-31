package dao;

import model.Arquivo;
import model.LZW;

import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZWDAO {

    LZW lzw;
    Arquivo arquivo;

    public LZWDAO(String filename) {
        arquivo = new Arquivo("");
        arquivo.csvFile = filename;
        lzw = new LZW();

    }

    public void createCompressedFile() {

        try (RandomAccessFile raf = new RandomAccessFile(arquivo.csvFile, "rw")) {
            RandomAccessFile rafLZW = new RandomAccessFile("../resources/LZWCompress.bin", "rw");
            StringBuilder sb = new StringBuilder();
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readLine() + '\n');
            }
            lzw.compression(sb.toString());
            double log2 = Math.ceil(Math.log(lzw.positionCompress) / Math.log(2));
            lzw.createBinaryString(lzw.compressedList);
            for (int i = 0; i < log2 % 8; i++) {
                lzw.compressedTxt.append('0');
            }
            BitManipulationDAO bitManipulationDAO = new BitManipulationDAO();
            bitManipulationDAO.writeBytes(rafLZW, lzw.compressedTxt.toString());
            System.out.println("binário necessário: " + log2);
            ArrayList<Integer> decompressedList = new ArrayList<>();
            StringBuilder bitString = new StringBuilder();
            rafLZW.seek(0);
            while (rafLZW.getFilePointer() < rafLZW.length()) {
                for (int i = 0; i < 19; i++) {
                    bitString.append(bitManipulationDAO.readBit(rafLZW));
                }
                System.out.println("bitstring: " + bitString);
                decompressedList.add(Integer.parseInt(bitString.toString(), 2));
                bitString.setLength(0);
//                System.out.println(raf);
//            System.out.println("substring: " + lzw.compressedTxt.substring(0, 19));
//            System.out.println("bitstring: " + bitString);
//            System.out.println(decompressedList.get(0));
//            System.out.println(lzw.compressedList.get(0));
            }
            lzw.decompress(decompressedList);
            System.out.println(lzw.decompressedText);
//            lzw.decompress();

//            lzw.decompress();
//            System.out.println(lzw.decompressedText);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
