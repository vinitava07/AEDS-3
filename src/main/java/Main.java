import java.io.RandomAccessFile;

import model.Arquivo;

public class Main {
    public static void main(String[] args) throws Exception {
        Arquivo arq = new Arquivo("../resources/ListaAnime.csv", "../resources/animeBin.bin");
        arq.CsvToByte();
    }
}
