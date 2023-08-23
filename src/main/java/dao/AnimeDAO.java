package dao;

import java.io.File;
/* ULTIMO ID 
    INT(4) */

/* lapide       length   id      name        type        episodes    studio     tags       rating      release_year
 * boolean(1)   int(4)  int(4)  (2)+varchar  char(5)      int(4)     varchar    varchar    float(4)    long(8)
 */

import java.io.RandomAccessFile;
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
            while (contador < 18400) {
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

        /*
         * length = id size(int) + (writeUTF extra 2 bytes * number of uses) + name size
         * + type fixed size of 5 +
         * episodes size(int) + studio size + tags size + rating size(float) +
         * release_year size(Timestamp)
         */
        int length = anime.getByteLength();
        int lastId = -1;
        RandomAccessFile binaryFile = new RandomAccessFile(bin, "rw");

        try {
            lastId = binaryFile.readInt() + 1;
            binaryFile.seek(0);
            binaryFile.writeInt(lastId);
        } catch (Exception e) { // empty file
            lastId = 0;
            binaryFile.writeInt(lastId);
        }

        binaryFile.seek(binaryFile.length());
        if (x % 2 == 0) {
            binaryFile.writeBoolean(true);

        } else {
            binaryFile.writeBoolean(true);
        }
        binaryFile.writeInt(length);
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
            boolean grav = false;
            raf.seek(0);
            lastId = raf.readInt();
            System.out.println("Ultimo id: " + lastId);
            for (int i = 0; i <= raf.length(); i += (5 + tam)) {
                grav = raf.readBoolean();
                tam = raf.readInt();
                if (grav) {
                    raf.seek(raf.getFilePointer() + 4); // ignores the id of the record
                    Anime anime = getRecord(raf);
                    anime.printAttributes();
                } else {

                    raf.seek(raf.getFilePointer() + tam);

                }
            }
            raf.close();
        } catch (Exception e) {
            // TODO: handle exception
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
            //the id search key is grater than the last record's id
        } else {
            int recordLength;
            boolean found = false;
            for(int i = 4; (i < file.length()) && (!found); i += (5 + recordLength)) {
                boolean validRecord = file.readBoolean();
                recordLength = file.readInt();
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
}