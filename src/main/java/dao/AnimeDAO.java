package dao;

import java.io.File;
/* ULTIMO ID 
    INT(4) */

/* lapide       length   id      name        type        episodes    studio     tags       rating      release_year
 * boolean(1)   int(4)  int(4)  (2)+varchar  char(5)      int(4)     varchar    varchar    float(4)    long(8)
 */

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import model.Anime;
import model.Arquivo;

public class AnimeDAO {

    Anime a;
    Arquivo arquivo;

    public AnimeDAO(String csv, String bin) {
        a = new Anime();
        arquivo = new Arquivo(csv, bin);
    }

    public AnimeDAO(String bin) {
        a = new Anime();
        arquivo = new Arquivo(bin);
    }

    public void csvToByte() {
        File csv = new File(arquivo.nameCsv);
        File bin = new File(arquivo.nameBin);
        String animeText;
        int contador = 0;
        Anime anime;
        // (animeText = csvFile.readLine()) != null
        if (bin.exists()) {
            System.out.println("Arquivo recriado!");
            bin.delete();
        }
        try {
            RandomAccessFile csvFile = new RandomAccessFile(csv, "r");
            csvFile.readLine(); // read csv file header
            anime = new Anime();
            while (contador < 100) {
                animeText = csvFile.readLine();
                anime.parseAnime(animeText);
                // System.out.println(animeText);
                // anime.printAttributes();
                writeAnimeBytes(anime, bin, contador);
                contador++;

            }
            csvFile.close();

        } catch (Exception e) {

            // TODO: handle exception
        }
    }

    private void writeAnimeBytes(Anime anime, File bin, int x) throws Exception {

        int length = anime.getByteLength();
        int lastId = -1;
        RandomAccessFile binaryFile = new RandomAccessFile(bin, "rw");

        try {
            lastId = binaryFile.readInt() + 1;
            binaryFile.seek(0);
            binaryFile.writeInt(lastId);
        } catch (Exception e) {
            // empty file
            lastId = 0;
            binaryFile.writeInt(lastId);
        }
        binaryFile.seek(binaryFile.length());

        /**
         * the gravestone is the most significant bit of the most significant byte of the record length
         */
        binaryFile.writeInt(length); //this 4 bytes contains the record length and the gravestone
        binaryFile.writeInt(lastId);
        binaryFile.writeUTF(anime.name);

        byte[] type = new byte[5]; // write anime type
        for (int j = 0; j < anime.type.length(); j++) {
            type[j] = (byte) anime.type.charAt(j);
        }
        binaryFile.write(type);

        binaryFile.writeInt(anime.episodes);
        binaryFile.writeUTF(anime.studio);
        binaryFile.writeUTF(anime.tags);
        binaryFile.writeFloat(anime.rating);
        binaryFile.writeLong(anime.release_year.getTime());

        binaryFile.close();
    }

    public void createAnime(Anime anime) throws Exception {
        File file = new File(this.arquivo.nameBin);
        writeAnimeBytes(anime, file, 0);
    }

    public void printAllAnime() {
        File file = new File(this.arquivo.nameBin);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int tam;
            raf.seek(0);
            lastId = raf.readInt();
            System.out.println("Ultimo id: " + lastId);
            for (int i = 0; i <= raf.length(); i += (4 + tam)) {
                byte[] byteArray = {raf.readByte() , raf.readByte() , raf.readByte() , raf.readByte()};
                boolean validRecord = isValidRecord(byteArray[0]);
                tam = getRecordLength(byteArray, validRecord);
                if (validRecord) {
                    raf.seek(raf.getFilePointer() + 4); // ignores the id of the record
                    Anime anime = getRecord(raf);
                    anime.printAttributes();
                } else {
                    raf.seek(raf.getFilePointer() + tam);
                }
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

    private Anime getRecord(RandomAccessFile file) throws Exception{
        Anime anime = new Anime();

        anime.name = file.readUTF();
        
        byte[] type = new byte[5];
        file.read(type, 0, 5);
        anime.type = new String(type, StandardCharsets.UTF_8);

        anime.episodes = file.readInt();

        anime.studio = file.readUTF();

        anime.tags = file.readUTF();

        anime.rating = file.readFloat();

        anime.release_year = anime.longToTimestamp(file.readLong());

        return anime;
    }

    public Anime searchAnimeById(int id) throws Exception{
        return sequencialSearch(id);
    }
    private Anime sequencialSearch(int id) throws Exception{
        RandomAccessFile file = new RandomAccessFile(arquivo.nameBin , "rw");
        Anime result = null;
        if(file.readInt() < id) {
            //the id search key is grater than the last recorded id
        } else {
            int recordLength;
            boolean found = false;
            for(int i = 4; (i < file.length()) && (!found); i += (4 + recordLength)) {
                byte[] byteArray = {file.readByte() , file.readByte() , file.readByte() , file.readByte()};
                boolean validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                if(id == file.readInt()) {
                    if(validRecord){
                        found = true;
                        result = getRecord(file);
                    } else {
                        found = true; //found the id but the record not valid and should not return a record
                    }
                }
                file.seek(file.getFilePointer() + (recordLength - 4));
            }
        }

        return result;
    }

    public Anime removeAnime(int where) throws Exception{
        return sequencialDelete(where);
    }
    private Anime sequencialDelete(int where) throws Exception{
        RandomAccessFile file = new RandomAccessFile(arquivo.nameBin , "rw");
        Anime deletedRecord = null;
        if(file.readInt() < where) {
            //the id search key is grater than the last recorded id
        } else {
            int recordLength;
            boolean found = false;
            for(int i = 4; (i < file.length()) && (!found); i += (5 + recordLength)) {
                byte[] byteArray = {file.readByte() , file.readByte() , file.readByte() , file.readByte()};
                boolean validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                if(where == file.readInt()) {
                    if(validRecord){
                        found = true;
                        deleteRecord(file , byteArray);
                        deletedRecord = getRecord(file);
                    } else {
                        found = true; //found the id but the record not valid and should not return a record
                    }
                }
                file.seek(file.getFilePointer() + (recordLength - 4));
            }
        }

        return deletedRecord;
    }
    private void deleteRecord(RandomAccessFile file , byte[] b) throws Exception{
        System.out.println(ByteBuffer.wrap(b).getInt());
        b[0] ^= (1 << 7); // sets the signal bit to 1, logicaly removing the record
        file.seek(file.getFilePointer() - 8);
        System.out.println(ByteBuffer.wrap(b).getInt());
        file.write(b, 0, 4);
        file.seek(file.getFilePointer() + 4);
    }


    private boolean isValidRecord(byte b) {
        //since the most significant bit of this byte is the gravestone, if b is negative then the record is not valid
        return (b >= 0);
    }
    private int getRecordLength(byte[] byteArray , boolean isValid) {
        int length = 0;
        if(isValid){
            length = ByteBuffer.wrap(byteArray).getInt();
        } else {
            /*since the record is not valid it means the signal bit is 1
             *to revert this without losing the byte data
             *an XOR operation is made with the byte: 0b10000000
            */
            byteArray[0] ^= (1 << 7);
            length = ByteBuffer.wrap(byteArray).getInt();
        }

        return length;
    }
}