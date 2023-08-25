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
import model.Record;

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
        Record r = new Record();
        // (animeText = csvFile.readLine()) != null
        if (bin.exists()) {
            System.out.println("Arquivo recriado!");
            bin.delete();
        }
        try {
            RandomAccessFile csvFile = new RandomAccessFile(csv, "r");
            RandomAccessFile binFile = new RandomAccessFile(bin, "rw");

            csvFile.readLine(); // read csv file header
            anime = new Anime();
            while (contador < 10) {
                animeText = csvFile.readLine();
                anime.parseAnime(animeText);
                r.setAnime(anime);
                // System.out.println(animeText);
                // anime.printAttributes();
                writeAnimeBytes(r, binFile, false);

                contador++;

            }
            csvFile.close();
            binFile.close();

        } catch (Exception e) {

            // TODO: handle exception
        }
    }

    private void writeAnimeBytes(Record r, RandomAccessFile raf, boolean update) throws Exception {

        int length = r.getAnime().getByteLength();
        int lastId = -1;
        if (update) {
            System.out.println(raf.getFilePointer());
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
        } else {
            if (r.getSize() == 0) {

                try {
                    raf.seek(0);// set the file pointer to pos 0
                    lastId = raf.readInt() + 1;
                    raf.seek(0);
                    raf.writeInt(lastId);
                } catch (Exception e) {
                    // empty file
                    lastId = 0;
                    raf.writeInt(lastId);
                }
                r.setId(lastId);
            }
            raf.seek(raf.length());

            /**
             * the gravestone is the most significant bit of the most significant byte of
             * the record length
             */
            raf.writeInt(length); // this 4 bytes contains the record length and the gravestone
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
        }

    }

    public void createAnime(Anime anime) throws Exception {
        File file = new File(this.arquivo.nameBin);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        Record r = new Record();
        r.setAnime(anime);
        writeAnimeBytes(r, raf, false);
        raf.close();
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
            for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                long filePointer = raf.getFilePointer();
                if (validRecord) {
                    raf.seek(filePointer + 4); // ignores the id of the record
                    Anime anime = getRecord(raf);
                    anime.printAttributes();
                    raf.seek(filePointer + recordLength);
                } else {
                    raf.seek(filePointer + recordLength);
                }
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
        }

    }

    private Anime getRecord(RandomAccessFile raf) throws Exception {
        Anime anime = new Anime();

        anime.name = raf.readUTF();

        byte[] type = new byte[5];
        raf.read(type, 0, 5);
        anime.type = new String(type, StandardCharsets.UTF_8);

        anime.episodes = raf.readInt();

        anime.studio = raf.readUTF();

        anime.tags = raf.readUTF();

        anime.rating = raf.readFloat();

        anime.release_year = anime.longToTimestamp(raf.readLong());

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
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    if (id == raf.readInt()) {
                        if (validRecord) {
                            found = true;
                            result = getRecord(raf);
                            System.out.println("Registro econtrado!");

                        } else {
                            System.out.println("Registro foi excluido anteriormente!");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
            raf.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (!found) {
            System.out.println("O registro não existe!");
        }
        return result;
    }

    public Anime removeAnime(int id) throws Exception {
        return sequencialDelete(id);
    }

    private Anime sequencialDelete(int id) throws Exception {

        Anime deletedRecord = null;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {
            byte[] byteArray = new byte[4];
            boolean validRecord;
            if (raf.readInt() < id) {
                System.out.println("O ID não existe");
                // the id search key is grater than the last recorded id
            } else {
                int recordLength;
                boolean found = false;
                for (int i = 4; (i < raf.length()) && (!found); i += (5 + recordLength)) {

                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);

                    if (id == raf.readInt()) {

                        if (validRecord) {
                            found = true;
                            long filePointer = raf.getFilePointer();
                            raf.seek(filePointer - 8);
                            changeGraveyard(raf, byteArray);
                            raf.seek(filePointer);
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
            raf.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

        return deletedRecord;
    }

    private void changeGraveyard(RandomAccessFile file, byte[] b) throws Exception {
        // System.out.println(ByteBuffer.wrap(b).getInt());
        b[0] ^= (1 << 7); // sets the signal bit to 1, logicaly (removing) switching the record
        // System.out.println(ByteBuffer.wrap(b).getInt());
        file.write(b, 0, 4);
    }

    private boolean isValidRecord(byte b) {
        // since the most significant bit of this byte is the gravestone, if b is
        // negative then the record is not valid
        return (b >= 0);
    }

    private int getRecordLength(byte[] byteArray, boolean isGraveyard) {
        int length = 0;
        if (isGraveyard) {
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

    public void updateRecord(int id, Anime a) {
        sequencialUpdate(id, a);
    }

    private void sequencialUpdate(int id, Anime a) {
        Anime result = null;
        int recordLength;
        boolean found = false;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int recordId;
        int newAnimeSize = a.getByteLength();
        Record r = new Record();
        long filePointer;
        r.setAnime(a);
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.nameBin, "rw")) {
            if (raf.readInt() < id) {
                System.out.println("ID maior do que os cadastrados!");
                // the id search key is grater than the last recorded id
            } else {
                for (int i = 4; (i < raf.length()) && (!found); i += (4 + recordLength)) {

                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    recordId = raf.readInt();
                    filePointer = raf.getFilePointer();
                    if (id == recordId) {
                        if (validRecord) {
                            found = true;
                            r.setGraveyard(validRecord);
                            r.setSize(newAnimeSize);
                            r.setId(recordId);
                            r.setAnime(a);
                            if (recordLength >= newAnimeSize) {
                                System.out.println("menor");
                                writeAnimeBytes(r, raf, true);

                            } else {
                                raf.seek(filePointer - 8);
                                changeGraveyard(raf, byteArray);
                                raf.seek(filePointer);
                                writeAnimeBytes(r, raf, false);

                            }
                            System.out.println("Registro atualizado!");

                        } else {
                            System.out.println("Registro foi excluido anteriormente!");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
            raf.close();
        } catch (Exception e) {
            // TODO: handle exception
        }

    }

}