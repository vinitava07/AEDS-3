package dao;

import model.Arquivo;
import model.LZW;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class LZWDAO {

    LZW lzw;
    Arquivo fileToCompress;
    String compressedFile = "../resources/LZWCompress.bin";
    String decompressedFile;

    public LZWDAO(String filename) {
        fileToCompress = new Arquivo("");
        fileToCompress.csvFile = filename;
        lzw = new LZW();

    }

    public void createCompressedFile() {
        File file = new File(compressedFile);
        if (file.exists()) file.delete();


        try (RandomAccessFile raf = new RandomAccessFile(fileToCompress.csvFile, "rw")) {
            RandomAccessFile rafLZW = new RandomAccessFile(compressedFile, "rw");
            StringBuilder sb = new StringBuilder();
            BitManipulationDAO bitManipulationDAO = new BitManipulationDAO();
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readLine() + '\n');
            }
            lzw.compression(sb.toString());
            double log2 = Math.ceil(Math.log(lzw.positionCompress) / Math.log(2));
            for (int i = 0; i < log2; i++) {
                lzw.compressedTxt.append('0');
            }
            System.out.println("Escrevendo arquivo comprimido");
            bitManipulationDAO.writeBytes(rafLZW, lzw.compressedTxt.toString());
            System.out.println("binário necessário: " + log2);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void decompressFile() {
        System.out.println("Descomprimindo:");
        try (RandomAccessFile rafLZW = new RandomAccessFile(compressedFile, "rw")) {
            BitManipulationDAO bitManipulationDAO = new BitManipulationDAO();
            double log2 = Math.ceil(Math.log(lzw.positionCompress) / Math.log(2));

            ArrayList<Integer> decompressedList = new ArrayList<>();
            StringBuilder bitString = new StringBuilder();
            rafLZW.seek(0);
//            System.out.println(rafLZW.getFilePointer()/8);
            System.out.println(lzw.compressedList.size());
            int j = 0;
            while (j < lzw.compressedList.size()) {
                for (int i = 0; i < log2; i++) {
                    bitString.append(bitManipulationDAO.readBit(rafLZW));
                }
//                System.out.println("bitstring: " + bitString);
                decompressedList.add(Integer.parseInt(bitString.toString(), 2));
                bitString.setLength(0);
                j++;
            }
            lzw.decompress(decompressedList);
            System.out.println(lzw.decompressedText);
            System.out.println(log2 % 8);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
