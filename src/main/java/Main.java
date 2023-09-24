import dao.AnimeDAO;
import dao.ListaInvertidaDAO;
import model.Anime;

import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");

        Timestamp ts = new Timestamp(0);
        Anime a = new Anime("abc", "TV", 3, "ufotable", "a,b,c", 3.2F, ts);

        ListaInvertidaDAO listaInvertidaTypeDAO = new ListaInvertidaDAO
                ("../resources/ListaInvertidaType.bin");

        // Anime a = new Anime(
        //         "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASAJFDSJFJHFJDASJFSHFSDJFHASFHKLADSFDASHFLADSJLFHADSFJADSHLFJKA SDFHLASDFAASDLJJJJJJJJJFHDSLJKFHASLFJKSADF HSADJF ADSJF HJLASLF HADSFJL SHF",
        //         "null", 0, "gdsfgdsfgdsfdsgdfg",
        //         "nulddgfdgdfgdgdfgdsfgdgdgdfgdfgdfgdgdsggdgdfgdgdgdgdgdfgdfgdfggsdfgdsgl", 0, ts);

    }
}
