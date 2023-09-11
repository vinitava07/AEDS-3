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
        AnimeDAO arq = new AnimeDAO("../resources/animeBin.bin", "../resources/ListaAnime.csv");
//        arq.csvToByte();
//        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin",8);
        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin");
        arq.buildIndexFile(index);
        index.deleteElement(6);
        System.out.println("===============================");
        index.printAllPages();
        System.out.println("===============================");

//        for (int i = 0; i < 30; i++) {

//        }

//        Anime anime = arq.indexSearch(3 , index);
//        if(anime != null) anime.printAttributes();
//        arq.csvToByte();
//        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin",4);
//        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin");
//        index.printTree   ();
//        BPlusTreePage bPlusTreePage = new BPlusTreePage(3);
//        index.writeNewPage(new RandomAccessFile("../resources/indexB.bin", "rw"), bPlusTreePage);


    }
}
