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
                records.sort(Comparator.comparingInt(Record::getId));
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

    private void writeDirectRecordBytes(Record r, RandomAccessFile raf) {
        try {
            System.out.println(r.getAnime());
            raf.writeInt(r.getSize()); // this 4 bytes contains the record length and the gravestone
            raf.writeInt(r.getId());
            raf.writeUTF(r.getAnime().name);

            byte[] type = new byte[5]; // write r.getAnime() type
            for (int j = 0; j < r.getAnime().type.length(); j++) {
                type[j] = (byte) r.getAnime().type.charAt(j);
            }
            raf.write(type);

            raf.writeInt(r.getAnime().episodes);
            raf.writeUTF(r.getAnime().studio);
            raf.writeUTF(r.getAnime().tags);
            raf.writeFloat(r.getAnime().rating);
            raf.writeLong(r.getAnime().release_year.getTime());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERRO NO BYTE RECORDS");
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
            File[] arquivos = new File[caminhos*2];
            for (int i = 0; i < caminhos * 2; i++) {
                arquivos[i] = new File(fileNames[i]);
            }
            Tape[] tape = new Tape[caminhos];
            for (int i = 0; i < caminhos; i++) {
                tape[i] = new Tape();
            }
            int[] fileArray = new int[caminhos];
            int[] fileArray2 = new int[caminhos];
            for (int i = 0; i < caminhos; i++) {
                fileArray[i] = i;
                fileArray2[i] = caminhos + i;
            }
            int fileToRead = 0;
            int fileToWrite = caminhos;
            boolean firstIteration = true;
            int posLido = 0;
            boolean allBlocksRead = false;
            int contadorFile = 0;
            int tamBloco = bloco;
            boolean caminhoUltimos = true;
            RandomAccessFile[] files = new RandomAccessFile[caminhos * 2];
            int contador = 0;
            int contador2 = 0;
//            (this.qtdRegistros / tamBloco)
//            tamBloco < this.qtdRegistros)
            while (contador2 < 2) {
                for (int i = 0; i < caminhos * 2; i++) {
                    files[i] = new RandomAccessFile(fileNames[i], "rw");
                }
                contador = 0;
                while (contador < qtdRegistros / (tamBloco * caminhos)) {
                    while (allBlocksRead == false) {
                        if (firstIteration) {
                            for (int i = 0; i < caminhos; i++) {
                                if (files[fileToRead + i].getFilePointer() == files[fileToRead + i].length()) {
                                    tape[i].canRead = false;
                                } else {
                                    tape[i].record = getRecord(files[fileToRead + i]);
                                }
                            }
                            firstIteration = false;
                        } else {
                            if (files[posLido].getFilePointer() == files[posLido].length()) {
                                tape[posLido].canRead = false;
                            }
                            if (tape[posLido].canRead) {
                                tape[posLido].record = getRecord(files[posLido]);
                            }
                        }

                        boolean repeat = true;
                        for (int i = 0; repeat && i < caminhos; i++) {
                            if (tape[i].canRead) {
                                minRecord = tape[i].record;
                                repeat = false;
                            } else {
                                minRecord = null;
                            }
                        }
                        System.out.println(minRecord.getId());
                        if (minRecord != null && minRecord.getAnime() != null ) {
                            for (int i = 0; i < caminhos; i++) {
                                if (tape[i].canRead && tape[i].record.getId() <= minRecord.getId()) {
                                    minRecord = tape[i].record;
                                    posLido = i;
                                }
                            }

                            writeDirectRecordBytes(minRecord, files[fileToWrite]);
                        }

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

                    }// INTERCALA OS BLOCOS
                    for (int i = 0; i < caminhos; i++) {
                        tape[i].canRead = true;
                        tape[i].filePointer = 0;
                    }
                    firstIteration = true;
                    allBlocksRead = false;

                    System.out.println("ftoW0: " + fileToWrite);
                    System.out.println("ftoR0: " + fileToRead);
                    contadorFile = (contadorFile + 1) % caminhos;
                    if (caminhoUltimos) {
                        fileToWrite = fileArray2[contadorFile];
                        fileToRead = 0;
                    } else {
                        fileToWrite = fileArray[contadorFile];
                        fileToRead = caminhos;

                    }
//                    System.out.println("ftoR: " + fileToRead);
                    contador++;
                }// FAZ A INTERCALAÇÃO N VEZES
                System.err.println("contador: " + contador);
                tamBloco = tamBloco * caminhos;
                contadorFile = 0;

                for (int i = 0; i < caminhos * 2; i++) {
                    files[i].close();
                }

                caminhoUltimos = !caminhoUltimos;
                if (caminhoUltimos) {
                    fileToWrite = fileArray2[contadorFile];
//                    System.out.println("ftoW: " + fileToWrite);
//                    arquivos[fileToWrite].delete();
                    fileToRead = 0;
//                    arquivos[fileToWrite + 1].delete();

                } else {
                    fileToWrite = fileArray[contadorFile];
//                    System.out.println("ftoW: " + fileToWrite);
//                    arquivos[fileToWrite].delete();
                    fileToRead = caminhos;
//                    arquivos[fileToWrite + 1].delete();
//                    System.out.println("ftoW: " + (fileToWrite + 1));


                }

                contador2++;
//                tamBloco = qtdRegistros;
//                contadorFile = 1;



            }// ACABOU A INTERCALAÇÃO

            System.out.println("ftoW FINAL: " + fileToWrite);


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
//        records[i] = getRecord(files[fileToRead]);
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
