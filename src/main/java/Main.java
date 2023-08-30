import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import dao.AnimeDAO;
import dao.RecordDAO;
import model.Anime;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
//        Timestamp ts = new Timestamp(0);
        Arquivo arquivo = new Arquivo("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        RecordDAO recordDAO = new RecordDAO(arquivo);

        recordDAO.intercalacaoBalanceada(2, 4);

//        AnimeDAO animeDAO = new AnimeDAO("f0.bin");
//        animeDAO.printAllAnime(false);
//        System.out.println();
//        System.out.println();
//        animeDAO = new AnimeDAO("f1.bin");
//        animeDAO.printAllAnime(false);
//        arq.csvToByte();

//         arq.removeAnime(0);
//
//        Anime a = new Anime(
//                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASAJFDSJFJHFJDASJFSHFSDJFHASFHKLADSFDASHFLADSJLFHADSFJADSHLFJKA SDFHLASDFAASDLJJJJJJJJJFHDSLJKFHASLFJKSADF HSADJF ADSJF HJLASLF HADSFJL SHF",
//                "null", 0, "gdsfgdsfgdsfdsgdfg",
//                "nulddgfdgdfgdgdfgdsfgdgdgdfgdfgdfgdgdsggdgdfgdgdgdgdgdfgdfgdfggsdfgdsgl", 0, ts);
////         Anime a = new Anime("null", "null", 0, "null", "null", 0, ts);
//        arq.updateRecord(0, a);
//        arq.printAllAnime();
//         Anime anime = arq.searchAnimeById(0);
//         if (anime != null) {
//
//         anime.printAttributes();
//         }
    }
}
