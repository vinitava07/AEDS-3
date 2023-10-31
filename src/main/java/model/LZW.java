package model;

import util.ProgressBar;

import java.util.ArrayList;
import java.util.HashMap;

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
        ProgressBar progressBar = new ProgressBar("Criando dicionario", 255);
        progressBar.startProcess();
        for (int i = 0; i <= 255; i++) { // Letras maiúsculas (A-Z)
            compressDictionary.put(String.valueOf((char) i), positionCompress);
            decompressDictionary.put(positionDecompress, String.valueOf((char) i));
            positionDecompress++;
            positionCompress++;
            progressBar.updateStatus(i);
        }
        progressBar.done();
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
        ProgressBar progressBar = new ProgressBar("Comprimindo texto", rawText.length());
        progressBar.startProcess();
        for (int i = 0; i < rawText.length(); ) {
            pattern.append(rawText.charAt(i));
            cont = i;
            if (!compressDictionary.containsKey(pattern.toString())) {
                compressDictionary.put(pattern.toString(), positionCompress++);
            }
            while (compressDictionary.containsKey(pattern.toString()) && ++i < rawText.length()) {
                pattern.append(rawText.charAt(i));
            }
            // i < rawText.length()
            // System.out.println(pattern);
            if (i >= rawText.length()) {
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
            progressBar.updateStatus(i);

        }
        progressBar.done();
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
        for (int i = 0; i < list.size(); i++) {
            newPat.append(decompressDictionary.get(list.get(i)));
            if (decompressDictionary.containsKey(list.get(i))) {
                if (i == list.size() - 1 || (list.get(i + 1)) >= positionDecompress) {
                    newPat.append(decompressDictionary.get(list.get(i)).charAt(0));
                    decompressDictionary.put(positionDecompress++, newPat.toString());
                } else {
                    newPat.append(decompressDictionary.get(list.get(i + 1)).charAt(0));
                    decompressDictionary.put(positionDecompress++, newPat.toString());

                }
                newPat.setLength(0);
            }
            decompressedText.append(decompressDictionary.get(list.get(i)));

        }
        // decompressedText.append(decompressDictionary.get(compressedList.get(compressedList.size()
        // - 1)));
        // System.out.println("decomp " + decompressedText);
        // System.out.println("raw " + rawText);
    }
}