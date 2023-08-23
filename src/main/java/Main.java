import java.io.RandomAccessFile;

import dao.AnimeDAO;
import model.Anime;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        // arq.csvToByte();
        // arq.printAllAnime();
        Anime anime = arq.searchAnimeById(10);
        if (anime != null) {

            anime.printAttributes();
        }
    }
}
