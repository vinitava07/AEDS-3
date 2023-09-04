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
//        arq.buildIndexFile();
        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin");
//        arq.buildIndexFile(index);
        index.deleteElement(1);

//        Anime anime = arq.indexSearch(310 , index);
//        if(anime != null) anime.printAttributes();
//        arq.csvToByte();
//        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin",4);
//        BPlusTreeDAO index = new BPlusTreeDAO("../resources/indexB.bin");
//        index.printTree   ();
//        BPlusTreePage bPlusTreePage = new BPlusTreePage(3);
//        index.writeNewPage(new RandomAccessFile("../resources/indexB.bin", "rw"), bPlusTreePage);



//        index.insertElement(30,300);
//        index.insertElement(10,100);
//        index.insertElement(20,200);
//        index.insertElement(3,30);
//        index.insertElement(15,150);
//        index.insertElement(35,350);
//        index.insertElement(25,250);
//        index.insertElement(5,50);
//        index.insertElement(11,110);
//        index.insertElement(12,120);
//        index.insertElement(13,130);
//        index.insertElement(16,160);

    }
}
