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
import model.BPlusTreePage;

public class Main {
    public static void main(String[] args) throws Exception {
//        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin");
//        BPlusTreePage bPlusTreePage = new BPlusTreePage(3);
//        index.writeNewPage(new RandomAccessFile("../resources/indexB.bin", "rw"), bPlusTreePage);
        index.insertElement(1,10);
//        index.insertElement(2,11);
//        index.insertElement(3,13);

    }
}
