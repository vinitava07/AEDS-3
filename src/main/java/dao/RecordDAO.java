package dao;

import model.Arquivo;
import model.Record;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class RecordDAO extends AnimeDAO {
    Record r;
    Arquivo arquivo;

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
            int[] posArq = new int[caminhos];
            RandomAccessFile[] files = new RandomAccessFile[caminhos * 2];
            int menorPos = 0;
            int countFile = caminhos;
            int aux = 0;
            boolean endfile;
            int blockSize = bloco;
            Record min = new Record();
            Record[] nRecord = new Record[caminhos];
            ArrayList<Record> records = new ArrayList<>();

            for (int i = 0; i < caminhos; i++) {
                posArq[i] = 0;
            }
            for (int i = 0; i < caminhos * 2; i++) {
                files[i] = new RandomAccessFile(fileNames[i], "rw");
            }
            while (Arrays.stream(posArq).min().getAsInt() < blockSize) {
                System.out.println(Arrays.stream(posArq).min().getAsInt());
                for (int i = 0; i < caminhos; i++) {
                    if (posArq[i] < blockSize) {
                        records.add(getRecord(files[posArq[i]]));
                    }
                }

                min = records.get(0);
                menorPos = 0;
                for (int i = 1; i < records.size(); i++) {
                    if (records.get(i).getId() < min.getId()) {
                        min = records.get(i);
                        menorPos = i;
                    }
                }

                posArq[menorPos]++;
                writeAnimeBytes(records.get(0), files[2], false);
//                records.sort(Comparator.comparingInt(Record::getId));
//                for (int i = 0; i <records.size() ; i++) {
//                    records.get(i).getAnime().printAttributes();
//                }

//                records.clear();
//                posArq[menorPos]++;
//                writeAnimeBytes(min, files[countFile], false);
                //  countFile = (countFile + 1) % (caminhos * 2);
            }
        } catch (Exception e) {
            System.out.println("erro intercalation " + e);
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
