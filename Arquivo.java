import java.io.RandomAccessFile;
import java.util.Arrays;

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
        try {
            RandomAccessFile rafR = new RandomAccessFile(this.nameCsv, "r");
            RandomAccessFile rafW = new RandomAccessFile(this.nameBin, "rw");
            rafR.readLine();
            rafR.readLine();
            rafR.readLine();
            
            a = new Anime();
            
            // for (int i = 0; i < 10; i++) {
            
            a.parseAnime(rafR.readLine());
            // System.out.println(a.name.length());
            a.printAttributes();
            
            rafW.writeUTF(a.name);

            byte[] b = new byte[5]; //write anime type
            for (int i = 0; i < a.type.length(); i++) {
                b[i] = (byte)a.type.charAt(i);
            }
            rafW.write(b);

            rafW.writeInt(a.episodes);
            rafW.writeUTF(a.studio);
            rafW.writeUTF(a.tags);
            rafW.writeFloat(a.rating);
            rafW.writeLong(a.release_year.getTime());

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