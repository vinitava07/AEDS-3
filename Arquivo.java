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
        try {
            RandomAccessFile rafR = new RandomAccessFile(this.nameCsv, "r");
            RandomAccessFile rafW = new RandomAccessFile(this.nameBin, "rw");
            rafR.readLine();
            
            a = new Anime();
            
            int id = -1;

            for (int i = 0; i < 10; i++) {
            
            a.parseAnime(rafR.readLine());
            // System.out.println(a.name.length());
            a.printAttributes();

            int length = 4 + 2 * 3 + a.name.length() + 5 + 4 + a.studio.length() + a.tags.length() + 4 + 8;


            rafW.writeInt(length);

            id++;
            rafW.writeInt(id);
            
            rafW.writeUTF(a.name);

            byte[] b = new byte[5]; //write anime type
            for (int j = 0; j < a.type.length(); j++) {
                b[j] = (byte)a.type.charAt(j);
            }
            rafW.write(b);

            rafW.writeInt(a.episodes);
            rafW.writeUTF(a.studio);
            rafW.writeUTF(a.tags);
            rafW.writeFloat(a.rating);
            rafW.writeLong(a.release_year.getTime());

            }
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