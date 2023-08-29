package dao;

import model.Arquivo;
import model.Record;
import model.Tape;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class RecordDAO extends AnimeDAO {
    Record r;
    Arquivo arquivo;

    int qtdRegistros = 0;

    public RecordDAO() {
        super();
        r = null;
        arquivo = null;
    }

    public RecordDAO(Record record, Arquivo arquivrecordo) {
        this.r = record;
        this.arquivo = arquivo;
    }

    public RecordDAO(Arquivo arquivo) {
        this.r = null;
        this.arquivo = arquivo;
    }

    private Record getRecord(RandomAccessFile raf) {
        Record r = new Record();

        boolean validRecord;
        int recordLength;
        int animeID;
        try {
            byte[] byteArray = new byte[4];
            raf.read(byteArray, 0, 4);
            validRecord = !isValidRecord(byteArray[0]);//the oposite of valid record
            recordLength = getRecordLength(byteArray, validRecord);
            animeID = raf.readInt();
            r.setSize(recordLength);
            r.setGraveyard(validRecord);
            r.setId(animeID);
            r.setAnime(getAnime(raf));
        } catch (Exception e) {

        }
        return r;
    }

    private String[] createFileNames(int caminhoDobro) {
        String[] fileNames = new String[caminhoDobro];
        String name = "f";
        for (int i = 0; i < caminhoDobro; i++) {
            fileNames[i] = name + String.valueOf(i) + ".bin";
        }
        return fileNames;
    }

    private void sortAndInsert(RandomAccessFile raf, String[] fileNames, int caminhos, int bloco) {
        try {
            int countFile = 0;
            long fileLength = raf.length();
            int lastId = raf.readInt();
            Record record = new Record();
            ArrayList<Record> records = new ArrayList<>();
            RandomAccessFile tempFile;
            while (fileLength != raf.getFilePointer()) {
                tempFile = new RandomAccessFile(fileNames[countFile], "rw");
                for (int i = 0; raf.getFilePointer() != fileLength && i < bloco; i++) { // grava a quantidade de records do bloco
                    record = getRecord(raf);
                    if (!record.getGraveyard()) {// se for tumulo ignora ele
                        records.add(record);
                        this.qtdRegistros++;
                    } else {
                        i--;
                    }
                }
                records.sort(Comparator.comparingInt(Record::getId));// ordena o bloco
//                System.out.println("size: " + records.size());
//                for (int i = 0; i < records.size(); i++) {
//                    System.out.println("id: " + records.get(i).getId());
//                    records.get(i).getAnime().printAttributes();
//                }
                for (int i = 0; i < records.size(); i++) {// grava o bloco no caminho n
                    writeAnimeBytes(records.get(i), tempFile, false);
                }
                records.clear();
                countFile = (countFile + 1) % caminhos;// garante a alternação de caminhos

            }

        } catch (Exception e) {
            System.out.println("ERRO SORT AND INSERT");
        }

    }

    private void intercalacaoPar() {

    }

    private void intercalacaoImpar() {

    }

    private void intercalation(String[] fileNames, int caminhos, int bloco) {

        try {
//            ArrayList<Record> recordArrayList = new ArrayList<>();
            Record minRecord = new Record();
            Tape[] tape = new Tape[caminhos];
            for (int i = 0; i < caminhos; i++) {
                tape[i] = new Tape();
            }
            int[] arquivos = new int[caminhos * 2];
            for (int i = 0; i < caminhos * 2; i++) {
                arquivos[i] = i;
            }
            boolean firstIteration = true;
            int posLido = 0;
            boolean allBlocksRead = false;

            int fileToWrite = caminhos;
            int tamBloco = bloco;
            boolean caminhoUltimos = true;
            RandomAccessFile[] files = new RandomAccessFile[caminhos * 2];
            while (tamBloco < this.qtdRegistros) {
            for (int i = 0; i < caminhos * 2; i++) {
                files[i] = new RandomAccessFile(fileNames[i], "rw");
            }
            int contador = 0;
            while (tamBloco < this.qtdRegistros) {
                while (contador < Math.ceil(this.qtdRegistros / tamBloco)) {
                    while (allBlocksRead == false) {
                        if (firstIteration) {
                            for (int i = 0; i < caminhos; i++) {
                                tape[i].record = getRecord(files[i]);
                            }
                            firstIteration = false;
                        } else {
                            if (tape[posLido].canRead) {
                                tape[posLido].record = getRecord(files[posLido]);
                            }
                        }
                        boolean repeat = true;
                        for (int i = 0; repeat && i < caminhos; i++) {
                            if (tape[i].canRead) {
                                minRecord = tape[i].record;
                                repeat = false;
                            }
                        }
                        for (int i = 0; i < caminhos; i++) {
                            if (tape[i].canRead && tape[i].record.getId() <= minRecord.getId()) {
                                minRecord = tape[i].record;
                                posLido = i;
                            }
                        }

                        writeAnimeBytes(minRecord, files[fileToWrite], false);

                        tape[posLido].filePointer++;
                        if (tape[posLido].filePointer == tamBloco) {
                            tape[posLido].canRead = false;
                        }
                        int countCantRead = 0;
                        for (int i = 0; i < caminhos; i++) {
                            if (tape[i].canRead == false) {
                                countCantRead++;
                            }
                        }
                        if (countCantRead == caminhos) {
                            allBlocksRead = true;
                        }

                    }
                    for (int i = 0; i < caminhos; i++) {
                        tape[i].canRead = true;
                        tape[i].filePointer = 0;
                    }
                    firstIteration = true;
                    allBlocksRead = false;
                    System.out.println(fileToWrite);
                    if (caminhoUltimos) {
                        if (fileToWrite == (caminhos * 2) - 1) {
                            fileToWrite = caminhos;
                        } else {
                            fileToWrite++;
                        }
                    } else {
                        if (fileToWrite == caminhos - 1) {
                            fileToWrite = 0;
                        } else {
                            fileToWrite++;
                        }
                    }

                    contador++;
                }
                caminhoUltimos = !caminhoUltimos;
                if (caminhoUltimos) {
                    fileToWrite = caminhos;
                } else {
                    fileToWrite = 0;
                }
                tamBloco = tamBloco * caminhos;
            }
            }
//            System.out.println("tape fp:");
//            for (int i = 0; i < caminhos; i++) {
//                System.out.println(tape[i].filePointer);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void intercalacaoBalanceada(int caminhos, int bloco) {
        int caminhoDobro = caminhos * 2;

        File[] file = new File[caminhoDobro];
        String[] fileNames = new String[caminhoDobro];
        fileNames = createFileNames(caminhoDobro);

        for (int i = 0; i < caminhoDobro; i++) { // open files
            file[i] = new File(fileNames[i]);
            if (file[i].exists()) {
                file[i].delete();
            }
        }
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {
            sortAndInsert(raf, fileNames, caminhos, bloco);
            intercalation(fileNames, caminhos, bloco);
        } catch (Exception e) {
            System.out.println("Erro intercalacao Balanceada");
        }
//
//


    }

}
//while (!allBlocksRead) {
//        fileToWrite = caminhos;
//        for (int i = 0; i < caminhos; i++) {
//        if (canRead[i]) {
//        records[i] = getRecord(files[i]);
//        }
//        }
//        minRecord = records[0];
//        for (int i = 0; i < caminhos; i++) {
//        if (canRead[i] && minRecord.getId() > records[i].getId()) {
//        minRecord = records[i];
//        posLido = i;
//        }
//        }
//        writeAnimeBytes(minRecord, files[fileToWrite], false);
//        filePos[posLido]++;
//        int countRead = 0;
//        for (int i = 0; i < caminhos; i++) {
//        if (filePos[i] == tamBloco) {
//        canRead[i] = false;
//        }
//        }
//        for (int i  = 0; i < caminhos; i++) {
//        if (!canRead[i]) {
//        countRead++;
//        }
//        }
//        if (countRead == caminhos) {
//        allBlocksRead = true;
//        }
