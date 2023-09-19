import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import dao.AnimeDAO;
import dao.ListaInvertidaTypeDAO;
import model.Anime;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        // Timestamp ts = new Timestamp(0);
//        arq.csvToByte();
        ListaInvertidaTypeDAO listaInvertidaTypeDAO = new ListaInvertidaTypeDAO
                ("../resources/ListaInvertidaType.bin");
        arq.criarListaInvertidaType(listaInvertidaTypeDAO);
        listaInvertidaTypeDAO.printIndex(arq);
        // arq.removeAnime(0);

        // Anime a = new Anime(
        //         "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASAJFDSJFJHFJDASJFSHFSDJFHASFHKLADSFDASHFLADSJLFHADSFJADSHLFJKA SDFHLASDFAASDLJJJJJJJJJFHDSLJKFHASLFJKSADF HSADJF ADSJF HJLASLF HADSFJL SHF",
        //         "null", 0, "gdsfgdsfgdsfdsgdfg",
        //         "nulddgfdgdfgdgdfgdsfgdgdgdfgdfgdfgdgdsggdgdfgdgdgdgdgdfgdfgdfggsdfgdsgl", 0, ts);

    }
}
