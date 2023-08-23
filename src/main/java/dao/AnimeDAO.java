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
         * the gravestone is the most significant bit of the most significant byte of
         * the record length
         */
        binaryFile.writeInt(length); // this 4 bytes contains the record length and the gravestone
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
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            lastId = raf.readInt();
            System.out.println("Ultimo id: " + lastId);
            for (int i = 0; i <= raf.length(); i += (4 + recordLength)) {
                // byte[] byteArray = { raf.readByte(), raf.readByte(), raf.readByte(),
                // raf.readByte() };
                // boolean validRecord = isValidRecord(byteArray[0]);
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                if (validRecord) {
                    raf.seek(raf.getFilePointer() + 4); // ignores the id of the record
                    Anime anime = getRecord(raf);
                    anime.printAttributes();
                } else {
                    raf.seek(raf.getFilePointer() + recordLength);
                }
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

    private Anime getRecord(RandomAccessFile file) throws Exception {
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

    public Anime searchAnimeById(int id) throws Exception {

        return sequencialSearch(id);
    }

    private Anime sequencialSearch(int id) throws Exception {
        Anime result = null;
        int recordLength;
        boolean found = false;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {

            if (raf.readInt() < id) {
                System.out.println("ID maior do que os cadastrados!");
                // the id search key is grater than the last recorded id
            } else {
                for (int i = 4; (i < raf.length()) && (!found); i += (4 + recordLength)) {
                    // byte[] byteArray = { raf.readByte(), raf.readByte(), raf.readByte(),
                    // raf.readByte() };
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    if (id == raf.readInt()) {
                        if (validRecord) {
                            found = true;
                            result = getRecord(raf);
                            System.out.println("Registro econtrado!");

                        } else {
                            System.out.println("Registro foi excluido!");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (!found) {
            System.out.println("O registro não existe!");
        }
        return result;
    }

    public Anime removeAnime(int where) throws Exception {
        return sequencialDelete(where);
    }

    private Anime sequencialDelete(int where) throws Exception {

        Anime deletedRecord = null;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {
            byte[] byteArray = new byte[4];
            boolean validRecord;
            if (raf.readInt() < where) {
                System.out.println("O ID não existe");
                // the id search key is grater than the last recorded id
            } else {
                int recordLength;
                boolean found = false;
                for (int i = 4; (i < raf.length()) && (!found); i += (5 + recordLength)) {

                    // byte[] byteArray = { raf.readByte(), raf.readByte(), raf.readByte(),
                    // raf.readByte() };
                    // boolean validRecord = isValidRecord(byteArray[0]);
                    // recordLength = getRecordLength(byteArray, validRecord);
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);

                    if (where == raf.readInt()) {

                        if (validRecord) {
                            found = true;
                            deleteRecord(raf, byteArray);
                            deletedRecord = getRecord(raf);
                            System.out.println("Registro deletado!");
                        } else {
                            System.out.println("O registro já foi deletado");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
                if (!found) {
                    System.out.println("Registro não encontrado!");
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }

        return deletedRecord;
    }

    }

    private void deleteRecord(RandomAccessFile file, byte[] b) throws Exception {
        System.out.println(ByteBuffer.wrap(b).getInt());
        b[0] ^= (1 << 7); // sets the signal bit to 1, logicaly removing the record
        file.seek(file.getFilePointer() - 8);
        System.out.println(ByteBuffer.wrap(b).getInt());
        file.write(b, 0, 4);
        file.seek(file.getFilePointer() + 4);
    }

    private boolean isValidRecord(byte b) {
        // since the most significant bit of this byte is the gravestone, if b is
        // negative then the record is not valid
        return (b >= 0);
    }

    private int getRecordLength(byte[] byteArray, boolean isValid) {
        int length = 0;
        if (isValid) {
            length = ByteBuffer.wrap(byteArray).getInt();
        } else {
            /*
             * since the record is not valid it means the signal bit is 1
             * to revert this without losing the byte data
             * an XOR operation is made with the byte: 0b10000000
             */
            byteArray[0] ^= (1 << 7);
            length = ByteBuffer.wrap(byteArray).getInt();
        }

        return length;
    }
}