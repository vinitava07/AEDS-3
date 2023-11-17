package model;

import util.ProgressMonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LZW {

    HashMap<String, Integer> compressDictionary;
    HashMap<Integer, String> decompressDictionary;
    public int positionCompress;
    public int positionDecompress;
    public StringBuilder compressedTxt;
    public ArrayList<Integer> compressedList;
    String rawText;
    public StringBuilder decompressedText;

    public LZW() {
//        rawText = txt;
        positionCompress = 0;
        positionDecompress = 0;
        compressDictionary = new HashMap<>();
        decompressDictionary = new HashMap<>();
        compressedTxt = new StringBuilder();
        decompressedText = new StringBuilder();
        compressedList = new ArrayList<>();
        createDictionary();
    }

    public void createDictionary() {
        AtomicLong i = new AtomicLong(0);
        ProgressMonitor progressMonitor = new ProgressMonitor("Criando dicionario", i, 255);
        progressMonitor.start();
        while (i.get() <= 255) { // Letras maiúsculas (A-Z)
            compressDictionary.put(String.valueOf((char) i.get()), positionCompress);
            decompressDictionary.put(positionDecompress, String.valueOf((char) i.getAndIncrement()));
            positionDecompress++;
            positionCompress++;
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // for (int i = 97; i <= 122; i++) { // Letras minúsculas (a-z)
        // compressDictionary.put(String.valueOf((char) i), position);
        // position++;
        // }
        // for (int i = 48; i <= 57; i++) { // Números (0-9)
        // compressDictionary.put(String.valueOf((char) i), position);
        // position++;
        // }
        // compressDictionary.put("a", 0);
        // position++;
        // compressDictionary.put("b", 1);
        // position++;
        // compressDictionary.put("w", 2);
        // position++;

    }

    public void compression(String text) {
        this.rawText = text;
        char k;
        boolean append = false;
        int cont = 0;
        StringBuilder pattern = new StringBuilder();
        String toAdd;
        AtomicLong i = new AtomicLong(0);
        ProgressMonitor progressMonitor = new ProgressMonitor("Comprimindo texto", i, rawText.length());
        progressMonitor.start();
        while (i.get() < rawText.length()) {
            pattern.append(rawText.charAt((int)i.get()));
            cont = (int)i.get();
            if (!compressDictionary.containsKey(pattern.toString())) {
                compressDictionary.put(pattern.toString(), positionCompress++);
            }
            while (compressDictionary.containsKey(pattern.toString()) && i.incrementAndGet() < rawText.length()) {
                pattern.append(rawText.charAt((int)i.get()));
            }
            // i < rawText.length()
            // System.out.println(pattern);
            if (i.get() >= rawText.length()) {
                toAdd = (pattern.substring(0, pattern.length()));

            } else if (pattern.length() > 1) {
                toAdd = (pattern.substring(0, pattern.length() - 1));
            } else {
                toAdd = (pattern.substring(0, pattern.length()));
            }
//            compressedTxt.append(String.valueOf(compressDictionary.get(toAdd) + " "));
            compressedList.add(compressDictionary.get(toAdd));
            compressDictionary.put(pattern.toString(), positionCompress++);
            pattern.setLength(0);
        }
        try{
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e){
            e.printStackTrace();
        }
        createBinaryString(compressedList);
        // System.out.println("raw: " + rawText);
        // System.out.println("final: " + compressedTxt);
        // System.out.println(compressDictionary.keySet());

    }

    public void createBinaryString(ArrayList<Integer> list) {
        double log2 = Math.ceil(Math.log(positionCompress) / Math.log(2));
        String number = "%" + (int)log2 + "s";
//        System.out.println(number);
//        compressedTxt.setLength(0);
        for (int i = 0; i < list.size(); i++) {

            compressedTxt.append(String.format(number, Integer.toBinaryString(list.get(i))).replaceAll(" ", "0"));
        }
//        System.out.println(list.get(0));
//        System.out.println(compressedTxt);

    }

    public void decompress(ArrayList<Integer> list) {

        StringBuilder newPat = new StringBuilder();
        AtomicLong i = new AtomicLong(0);
        ProgressMonitor progressMonitor = new ProgressMonitor("Decompressing", i, list.size());
        progressMonitor.start();
        while (i.get() < list.size()) {
            newPat.append(decompressDictionary.get(list.get((int)i.get())));
            if (decompressDictionary.containsKey(list.get((int)i.get()))) {
                if (i.get() == list.size() - 1 || (list.get((int)i.get() + 1)) >= positionDecompress) {
                    newPat.append(decompressDictionary.get(list.get((int)i.get())).charAt(0));
                    decompressDictionary.put(positionDecompress++, newPat.toString());
                } else {
                    newPat.append(decompressDictionary.get(list.get((int)i.get() + 1)).charAt(0));
                    decompressDictionary.put(positionDecompress++, newPat.toString());

                }
                newPat.setLength(0);
            }
            decompressedText.append(decompressDictionary.get(list.get((int) i.getAndIncrement())));
        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // decompressedText.append(decompressDictionary.get(compressedList.get(compressedList.size()
        // - 1)));
        // System.out.println("decomp " + decompressedText);
        // System.out.println("raw " + rawText);
    }
}