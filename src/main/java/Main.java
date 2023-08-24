import java.io.RandomAccessFile;

import dao.AnimeDAO;
import model.Anime;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        arq.csvToByte();
        arq.printAllAnime();
        arq.removeAnime(99);
    //     Anime anime = arq.searchAnimeById(12);
    //     if (anime != null) {

    //         anime.printAttributes();
    //     }
    }
}
