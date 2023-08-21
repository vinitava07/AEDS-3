import java.io.RandomAccessFile;

import dao.AnimeDAO;
import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        AnimeDAO arq = new AnimeDAO("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        arq.CsvToByte();
    }
}
