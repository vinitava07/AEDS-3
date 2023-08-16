import java.io.RandomAccessFile;

public class Main {
    public static void main(String[] args) throws Exception {
        RandomAccessFile raf = new RandomAccessFile("ListaAnime.csv", "r");
        Anime a = new Anime(null, null,
                0, null, null, 0, 0);
    
        for (int i = 0; i < 10; i++) {
            a.parseAnime(raf.readLine());
            a.printAttributes();
            System.out.println();
        }

    }
}
