import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("ListaAnime.csv", "r");
        Anime a = new Anime(0, null, null,
                0, null, null, 0, 0, 0, null, null);
        raf.readLine();
        raf.readLine();
        a.parseAnime(raf.readLine());
        a.printAttributes();

    }
}
