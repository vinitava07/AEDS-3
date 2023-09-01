import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

import dao.AnimeDAO;
import dao.BPlusTreeDAO;
import dao.RecordDAO;
import model.Anime;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
//        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");

        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin" , 5);
    }
}
