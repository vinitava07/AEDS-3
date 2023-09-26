import dao.AnimeDAO;
import dao.BPlusTreeDAO;
import dao.DynamicHashingDAO;
import dao.ListaInvertidaDAO;
import menu.Menu;
import model.Anime;

import java.sql.Timestamp;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO animeDAO = new AnimeDAO("animeBin.bin" , "ListaAnime.csv");
        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO("animeBin.bin" , 8);
        DynamicHashingDAO dynamicHashingDAO = new DynamicHashingDAO("animeBin.bin" , true);

        animeDAO.loadData(bPlusTreeDAO , dynamicHashingDAO);
    }
}
