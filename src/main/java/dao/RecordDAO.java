package dao;

import model.Arquivo;
import model.Record;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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


    public void intercalacaoBalanceada(int caminhos, int bloco) {

        File f01 = new File("f1.bin");
        File f02 = new File("f2.bin");
        File f03 = new File("f3.bin");
        File f04 = new File("f4.bin");
        if (f01.exists()) {
            f01.delete();
        }
        if (f02.exists()) {
            f02.delete();
        }
        if (f03.exists()) {
            f03.delete();
        }
        if (f04.exists()) {
            f04.delete();
        }
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {
            ArrayList<Record> records = new ArrayList<>();
            Record rec = new Record();
            long fileLength = raf.length();
            int lastID = raf.readInt();
            RandomAccessFile[] f1 = new RandomAccessFile[4];
            f1[0] = new RandomAccessFile("f1.bin", "rw");
            f1[1] = new RandomAccessFile("f2.bin", "rw");
            int aux = 0;
            while (raf.getFilePointer() != fileLength) { // ORDENANDO E SEPARANDO EM ARQUIVOS
                for (int i = 0; i < bloco && raf.getFilePointer() != fileLength; i++) {
                    rec = getRecord(raf);
                    if (!rec.getGraveyard()) {
                        records.add(rec);
                    } else {
                        i--;
                    }
                }
                records.sort(Comparator.comparingInt(Record::getId));
                for (int i = 0; i < records.size(); i++) {
                    writeAnimeBytes(records.get(i), f1[aux], false);
                }
                records.clear();
                aux = (aux + 1) % 2;
            }


        } catch (Exception e) {

        }


    }

}
