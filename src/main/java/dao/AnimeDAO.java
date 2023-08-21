package dao;

import java.io.File;

/**lapide       length   id      name        type        episodes    studio     tags       rating      release_year
 * boolean(1)   int(4)  int(4)   varchar     char(5)      int(4)     varchar    varchar    float(4)    long(8)
 */

import java.io.RandomAccessFile;

import model.Anime;
import model.Arquivo;

public class AnimeDAO {

    Anime anime;
    Arquivo arquivo;

    public AnimeDAO(String csv, String bin) {
        anime = new Anime();
        arquivo = new Arquivo(csv, bin);
    }

    public void CsvToByte() {
        File csv = new File(arquivo.nameCsv);
        File bin = new File(arquivo.nameBin);
        String animeText;
        if (bin.exists()) {
            System.out.println("Arquivo recriado!");
            bin.delete();
        }
        try {
            RandomAccessFile csvFile = new RandomAccessFile(csv, "r");
            csvFile.readLine(); // csv file header
            anime = new Anime();
            while ((animeText = csvFile.readLine()) != null) {
                anime.parseAnime(animeText);
                // System.out.println(animeText);
                // anime.printAttributes();
                writeAnimeBytes(anime, bin);

            }
            csvFile.close();

        } catch (Exception e) {

            // TODO: handle exception
        }
    }

    private void writeAnimeBytes(Anime anime, File bin) throws Exception {

        /*
         * length = id size(int) + (writeUTF extra 2 bytes * number of uses) + name size
         * + type fixed size of 5 +
         * episodes size(int) + studio size + tags size + rating size(float) +
         * release_year size(Timestamp)
         */
        int length = 4 + 2 * 3 + anime.name.length() + 5 + 4 + anime.studio.length() + anime.tags.length() + 4 + 8;
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
        binaryFile.writeBoolean(true);
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
}