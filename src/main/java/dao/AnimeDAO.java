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
            while ((animeText = csvFile.readLine()) != null) {
                // animeText = csvFile.readLine();
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
        int length = 4 + (2 + anime.name.length()) + 5 + 4 + (2 + anime.studio.length()) + (2 + anime.tags.length()) + 4
                + 8;
        int lastId = -1;

        RandomAccessFile binaryFile = new RandomAccessFile(bin, "rw");

        try {
            lastId = binaryFile.readInt() + 1;
            binaryFile.seek(0);
            binaryFile.writeInt(lastId);
        } catch (Exception e) {
            lastId = 0;
            binaryFile.writeInt(lastId);
        }

        binaryFile.seek(binaryFile.length());
        if (x % 2 == 0) {
            binaryFile.writeBoolean(true);

        } else {
            binaryFile.writeBoolean(false);
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

    public void printAnime() {
        File file = new File(this.arquivo.nameBin);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            Anime anime = new Anime();
            int ID = 0;
            byte[] type = new byte[5];
            int lastId = 0;
            int tam;
            raf.seek(0);
            long pos = 4;
            lastId = raf.readInt();
            System.out.println("Ultimo id: " + lastId);
            for (int i = 0; i <= lastId; i++) {
                if (raf.readBoolean()) {
                    tam = raf.readInt();
                    System.out.println("tam: " + tam);
                    pos += 5;

                    ID = raf.readInt();
                    pos += 4;
                    System.out.println("id: " + ID);

                    anime.name = raf.readUTF();
                    pos += anime.name.length() + 2;

                    raf.read(type, 0, 5);
                    anime.type = new String(type, StandardCharsets.UTF_8);
                    pos += 5;

                    anime.episodes = raf.readInt();
                    pos += 4;

                    anime.studio = raf.readUTF();
                    pos += anime.studio.length() + 2;

                    anime.tags = raf.readUTF();
                    pos += anime.tags.length() + 2;

                    anime.rating = raf.readFloat();
                    pos += 4;
                    anime.release_year = anime.longToTimestamp(raf.readLong());
                    System.out.println("file pointer " + raf.getFilePointer());
                    pos += 8;
                    System.out.println("posicao: " + pos);
                    anime.printAttributes();
                } else {
                    // pos++;
                    System.out.println("fp " + raf.getFilePointer());
                    System.out.println(raf.readInt());
                    raf.seek(raf.getFilePointer() - 4);
                    pos = raf.getFilePointer() + raf.readInt();
                    System.out.println("pos " + pos);
                    raf.seek(pos);

                }
            }

        } catch (Exception e) {
            // TODO: handle exception
        }

    }
}