package dao;

import model.Arquivo;
import util.ProgressMonitor;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Random;


public class BoyerMooreDAO {
    String text;
    String pattern;
    HashMap<Character, Integer> table;
    int[] dsb;
    Arquivo arquivo;

    public BoyerMooreDAO() {
        text = "";
        pattern = "";
        table = new HashMap<>();
        dsb = new int[0];
        arquivo = null;

    }

    public BoyerMooreDAO(String padrao, String csv) {

//        text = texto;
        pattern = padrao;
        table = new HashMap<>();
        dsb = new int[padrao.length()];
        arquivo = new Arquivo("", csv);
        System.out.println("Carregando arquivo csv");
        try (RandomAccessFile raf = new RandomAccessFile("../resources/" + csv, "rw")) {
            StringBuilder sb = new StringBuilder();
            while (raf.getFilePointer() < raf.length()) {
                sb.append(raf.readLine());
                sb.append('\n');
            }
            text = sb.toString();
            System.out.println("Leitura finalizada");
        } catch (Exception e) {
            e.printStackTrace();
        }
        createBadCharMap();
        createDSBArray();

    }

    public void createBadCharMap() {

        for (int i = pattern.length() - 2; i >= 0; i--) {

            if (!table.containsKey(pattern.charAt(i))) {
                table.put(pattern.charAt(i), i);
            }

        }

    }

    public void createDSBArray() {

        StringBuilder auxPattern;
        StringBuilder suffix = new StringBuilder();
        StringBuilder suffixAux = new StringBuilder();
        int pos = 0;
        int k = 0;
        boolean found = false;
        int length = pattern.length();
        dsb[length - 1] = 1;
        suffix.append(pattern.charAt(length - 1));
        int prefixPos = length - 2;
        for (int i = length - 2; i >= 0; i--) {
            int aux = length - suffix.length() - 1;
            pos = length - suffix.length();
            // System.out.println("charat: " + pattern.charAt(i));
            for (int j = length - 1; !found && j >= 0 && aux >= 0; j--) {
                auxPattern = new StringBuilder(pattern.substring(aux, j));
                if (auxPattern.compareTo(suffix) == 0) {
                    if (aux > 0 && pattern.charAt(aux - 1) != pattern.charAt(prefixPos)) {
                        dsb[i] = pos - aux;
                        found = true;
                    }
                }
                aux--;

            }
            boolean prefixFound = false;
            if (!found) {
                int sufPos = pattern.lastIndexOf(suffix.toString());
                int lastIndexOf = 0;
                suffixAux = new StringBuilder(suffix);
                auxPattern = new StringBuilder(pattern.substring(0, sufPos));
                for (int j = 0; !prefixFound && j < suffix.length(); j++) {
                    lastIndexOf = auxPattern.lastIndexOf(suffixAux.toString());
                    if (lastIndexOf == 0) {
                        // System.out.println("teste: " + (auxPattern.length() - lastIndexOf));
                        dsb[i] = (auxPattern.length() - lastIndexOf);
                        prefixFound = true;
                    }
                    auxPattern.append(suffixAux.charAt(0));
                    suffixAux.deleteCharAt(0);
                }

            }
            if (!prefixFound && !found) {
                dsb[i] = pattern.length() - 1;
            }
            found = false;
            prefixPos -= 1;
            suffix.insert(0, pattern.charAt(i));

        }

    }

    public int calculateMaxJump(int pos, char fail) {

        int max = 1;
        int opTable, opDsb;
        if (table.containsKey(fail)) {
            opTable = pos - table.get(fail);
            opTable = (opTable < 0) ? (opTable * -1) : opTable;
        } else {
            opTable = pos + 1;
        }
        opDsb = dsb[pos];
        // System.out.println("opTable: " + opTable + " opsbb: " + opDsb);
        max = Math.max(opTable, opDsb);
        return max;
    }

    public void searchPattern() {

        boolean canRun = true;
        int i = 0;
        int pattSize = pattern.length() - 1;
        int aux = pattSize;
        int count = 0;
        int jump = 1;
        int comparacoes = 0;
        int txtPointer = 0;
        // int j = pattSize;
        boolean found = false;
        ProgressMonitor progressMonitor = new ProgressMonitor("Boyer Moore patter matching");
        progressMonitor.start();
        while (canRun) {
            if (aux >= text.length()) {
                canRun = false;
            } else {
                txtPointer = aux;
                boolean diff = false;
                count = 0;
                jump = 1;
                for (int j = pattSize; !diff && j >= 0; j--) {

                    // System.out.println(
                    // "comparou do txt: " + text.charAt(txtPointer) + " com do p: " +
                    // pattern.charAt(j));
                    if (text.charAt(txtPointer) != pattern.charAt(j)) {
                        jump = calculateMaxJump(j, text.charAt(txtPointer));
                        // System.out.println("pulou: " + jump);
                        diff = true;
                    } else {
                        count++;
                    }
                    comparacoes++;
                    txtPointer--;

                }
                aux += jump;
                if (count == pattSize + 1) {
                    found = true;
                    canRun = false;
                }
                // System.out.println(text.charAt(aux));

            }

        }
        try {
            progressMonitor.endProcess();
            progressMonitor.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (found) {
            System.out.println("Padrão encontrado, inicio: " + (txtPointer + 1) + " fim: " + aux);
            System.out.println(text.substring(txtPointer + 1, aux));
        } else {
            System.out.println("Padrao não encontrado!");
        }
        System.out.println("Comparações: " + comparacoes);

    }


}
