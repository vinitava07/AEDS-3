import java.io.RandomAccessFile;

class Arquivo {

    public String nameCsv;
    public String nameBin;

    Arquivo() {
        this.nameCsv = "";
        this.nameBin = "";
    }

    Arquivo(String csvName, String binName) {
        this.nameCsv = csvName;
        this.nameBin = binName;
    }

    public void CsvToByte() {
        String line;
        Anime a;
        Byte[] b;
        try {
            RandomAccessFile rafR = new RandomAccessFile(this.nameCsv, "r");
            RandomAccessFile rafW = new RandomAccessFile(this.nameBin, "rw");
            rafR.readLine();
            rafR.readLine();

            a = new Anime();

            // for (int i = 0; i < 10; i++) {
            a.parseAnime(rafR.readLine());
            System.out.println(a.name.length());
            a.printAttributes();
            rafW.writeUTF(a.name);
            rafW.writeUTF(a.type);
            rafW.writeInt(a.episodes);
            rafW.writeUTF(a.studio);
            rafW.writeUTF(a.tags);
            rafW.writeFloat(a.rating);

            // }
            // while ((line = rafR.readLine()) != null) {

            // }
            rafR.close();
            rafW.close();

        } catch (

        Exception e) {
            // TODO: handle exception
        }
    }

}