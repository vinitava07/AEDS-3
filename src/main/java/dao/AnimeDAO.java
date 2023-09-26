package dao;

import java.io.File;
/* ULTIMO ID 
    INT(4) */

/* lapide       length   id      name        type        episodes    studio     tags       rating      release_year
 * boolean(  int(4))    int(4)  varchar  char(5)      int(4)     varchar    varchar    float(4)    long(8)
 */

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import model.Anime;
import model.Arquivo;
import model.PageElement;
import model.ListaInvertida;
import model.Record;
import util.ProgressBar;

public class AnimeDAO {

    Anime a;
    Arquivo arquivo;

    public AnimeDAO(String bin, String csv) {
        a = new Anime();
        String binFile = "../resources/" + bin;
        String csvFile = "../resources/" + csv;
        arquivo = new Arquivo(binFile, csvFile);
    }

    public AnimeDAO(String bin) {
        a = new Anime();
        arquivo = new Arquivo(("../resources/" + bin));
    }

    public AnimeDAO() {
        arquivo = null;
        a = null;
    }

    public void csvToByte() {
        File csv = new File(arquivo.auxFile);
        File bin = new File(arquivo.mainFile);
        String animeText;
        int contador = 0;
        Anime anime;
        Record r = new Record();
        // (animeText = csvFile.readLine()) != null
        if (bin.exists()) {
            System.out.println("Arquivo recriado!");
            bin.delete();
        }
        try {
            RandomAccessFile csvFile = new RandomAccessFile(csv, "r");
            RandomAccessFile binFile = new RandomAccessFile(bin, "rw");

            csvFile.readLine(); // read csv file header
            anime = new Anime();
            while (contador < 10000) {
                animeText = csvFile.readLine();
                anime.parseAnime(animeText);
                r.setAnime(anime);
                // System.out.println(animeText);
                // anime.printAttributes();
                writeAnimeBytes(r, binFile, false);

                contador++;

            }
            csvFile.close();
            binFile.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    protected void writeAnimeBytes(Record r, RandomAccessFile raf, boolean update) throws Exception {

        int length = r.getAnime().getByteLength();
        int lastId = -1;
        if (update) {
            System.out.println(raf.getFilePointer());
            raf.writeUTF(r.getAnime().name);
            byte[] type = new byte[5]; // write r.getAnime() type
            for (int j = 0; j < r.getAnime().type.length(); j++) {
                type[j] = (byte) r.getAnime().type.charAt(j);
            }
            raf.write(type);

            raf.writeInt(r.getAnime().episodes);
            raf.writeUTF(r.getAnime().studio);
            raf.writeUTF(r.getAnime().tags);
            raf.writeFloat(r.getAnime().rating);
            raf.writeLong(r.getAnime().release_year.getTime());
        } else {
            if (r.getSize() == 0) { // if the record only has a anime and no more informations is a new record to be Add
                try {
                    raf.seek(0);// set the file pointer to pos 0
                    lastId = raf.readInt() + 1;
                    raf.seek(0);
                    raf.writeInt(lastId);
                } catch (Exception e) {
                    // empty file
                    lastId = 0;
                    raf.writeInt(lastId);
                }
                r.setId(lastId);
            }
            raf.seek(raf.length());

            /**
             * the gravestone is the most significant bit of the most significant byte of
             * the record length
             */
            raf.writeInt(length); // this 4 bytes contains the record length and the gravestone
            raf.writeInt(r.getId());
            raf.writeUTF(r.getAnime().name);

            byte[] type = new byte[5]; // write r.getAnime() type
            for (int j = 0; j < r.getAnime().type.length(); j++) {
                type[j] = (byte) r.getAnime().type.charAt(j);
            }
            raf.write(type);

            raf.writeInt(r.getAnime().episodes);
            raf.writeUTF(r.getAnime().studio);
            raf.writeUTF(r.getAnime().tags);
            raf.writeFloat(r.getAnime().rating);
            raf.writeLong(r.getAnime().release_year.getTime());
        }

    }

    public void createAnime(Anime anime) throws Exception {
        File file = new File(this.arquivo.mainFile);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        Record r = new Record();
        r.setAnime(anime);
        writeAnimeBytes(r, raf, false);
        raf.close();
    }

    public void printAllAnime(boolean lastID) {
        File file = new File(this.arquivo.mainFile);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            //lastId = raf.readInt();
            for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                System.out.println("len: " + recordLength);
                long filePointer = raf.getFilePointer();
                System.out.println("id: " + raf.readInt());
                if (validRecord) {
//                    raf.seek(filePointer + 4); // ignores the id of the record
                    Anime anime = getAnime(raf);
                    System.out.println(anime.name);
                    anime.printAttributes();
                    raf.seek(filePointer + recordLength);
                } else {
                    raf.seek(filePointer + recordLength);
                }
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }

    }

    public void printAllAnime() {
        File file = new File(this.arquivo.mainFile);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            lastId = raf.readInt();
            System.out.println("Ultimo id: " + lastId);
            for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                long filePointer = raf.getFilePointer();
                if (validRecord) {
                    raf.seek(filePointer + 4); // ignores the id of the record
                    Anime anime = getAnime(raf);
                    anime.printAttributes();
                    raf.seek(filePointer + recordLength);
                } else {
                    raf.seek(filePointer + recordLength);
                }
            }
            raf.close();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    protected Anime getAnime(RandomAccessFile raf) throws Exception {
        Anime anime = new Anime();

        anime.name = raf.readUTF();

        byte[] type = new byte[5];
        raf.read(type, 0, 5);
        anime.type = new String(type, StandardCharsets.UTF_8);

        anime.episodes = raf.readInt();

        anime.studio = raf.readUTF();

        anime.tags = raf.readUTF();

        anime.rating = raf.readFloat();

        anime.release_year = anime.longToTimestamp(raf.readLong());

        return anime;
    }

    public Anime searchAnimeById(int id) throws Exception {

        return sequentialSearch(id);
    }

    private Anime sequentialSearch(int id) throws Exception {
        Anime result = null;
        int recordLength;
        boolean found = false;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int animeID;
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.mainFile, "rw")) {

            if (raf.readInt() < id) {
                System.out.println("ID maior do que os cadastrados!");
                // the id search key is grater than the last recorded id
            } else {
                for (int i = 4; (i < raf.length()) && (!found); i += (4 + recordLength)) {
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    animeID = raf.readInt();
                    if (validRecord) {
                        if (animeID == id) {
                            found = true;
                            result = getAnime(raf);
                            System.out.println("Registro econtrado!");

                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!found) {
            System.out.println("O registro não existe!");
        }
        return result;
    }

    public Anime removeAnime(int id) throws Exception {
        return sequentialDelete(id);
    }

    private Anime sequentialDelete(int id) throws Exception {

        Anime deletedRecord = null;

        try (RandomAccessFile raf = new RandomAccessFile(arquivo.mainFile, "rw")) {
            byte[] byteArray = new byte[4];
            boolean validRecord;
            int animeID;
            if (raf.readInt() < id) {
                System.out.println("O ID não existe");
                // the id search key is grater than the last recorded id
            } else {
                int recordLength;
                boolean found = false;

                for (int i = 4; (i < raf.length()) && (!found); i += (5 + recordLength)) {

                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    animeID = raf.readInt();
                    if (id == animeID) {
                        if (validRecord) {
                            found = true;
                            long filePointer = raf.getFilePointer();
                            raf.seek(filePointer - 8);
                            changeGraveyard(raf, byteArray);
                            raf.seek(filePointer);
                            deletedRecord = getAnime(raf);
                            System.out.println("Registro deletado!");
                        } else {
                            System.out.println("O registro já foi deletado");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
                if (!found) {
                    System.out.println("Registro não encontrado!");
                }
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return deletedRecord;
    }

    private void changeGraveyard(RandomAccessFile file, byte[] b) throws Exception {
        // System.out.println(ByteBuffer.wrap(b).getInt());
        b[0] ^= (byte) (1 << 7); // sets the signal bit to 1, logicaly (removing) switching the record
        // System.out.println(ByteBuffer.wrap(b).getInt());
        file.write(b, 0, 4);
    }

    boolean isValidRecord(byte b) {
        // since the most significant bit of this byte is the gravestone, if b is
        // negative then the record is not valid
        return (b >= 0);
    }

    protected int getRecordLength(byte[] byteArray, boolean isGraveyard) {
        int length = 0;
        if (isGraveyard) {
            length = ByteBuffer.wrap(byteArray).getInt();
        } else {
            /*
             * since the record is not valid it means the signal bit is 1
             * to revert this without losing the byte data
             * an XOR operation is made with the byte: 0b10000000
             */
            byteArray[0] ^= (1 << 7);
            length = ByteBuffer.wrap(byteArray).getInt();
        }

        return length;
    }

    public void updateRecord(int id, Anime a) {
        sequentialUpdate(id, a);
    }

    private void sequentialUpdate(int id, Anime a) {
        Anime result = null;
        int recordLength;
        boolean found = false;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int recordId;
        int newAnimeSize = a.getByteLength();
        Record r = new Record();
        long filePointer;
        r.setAnime(a);
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.mainFile, "rw")) {
            if (raf.readInt() < id) {
                System.out.println("ID maior do que os cadastrados!");
                // the id search key is grater than the last recorded id
            } else {
                for (int i = 4; (i < raf.length()) && (!found); i += (4 + recordLength)) {

                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    recordId = raf.readInt();
                    filePointer = raf.getFilePointer();
                    if (id == recordId) {
                        if (validRecord) {
                            found = true;
                            r.setGraveyard(validRecord);
                            r.setSize(newAnimeSize);
                            r.setId(recordId);
                            r.setAnime(a);
                            if (recordLength >= newAnimeSize) {
                                System.out.println("menor");
                                writeAnimeBytes(r, raf, true);

                            } else {
                                raf.seek(filePointer - 8);
                                changeGraveyard(raf, byteArray);
                                raf.seek(filePointer);
                                writeAnimeBytes(r, raf, false);

                            }
                            System.out.println("Registro atualizado!");

                        } else {
                            System.out.println("Registro foi excluido anteriormente!");
                            found = true; // found the id but the record not valid and should not return a record
                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void buildBPlusTreeIndexFile(BPlusTreeDAO index) {
//        BPlusTreeDAO indexFile = new BPlusTreeDAO("../resources/indexB.bin",8);
//        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO("../resources/indexB.bin",8);


        File file = new File(this.arquivo.mainFile);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            lastId = raf.readInt();
            for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                long filePointer = raf.getFilePointer();
                if (validRecord) {
                    int id = raf.readInt();
                    long dataFilePosition = raf.getFilePointer();
                    index.insertElement(id, dataFilePosition);
                    raf.seek(filePointer + recordLength);
//                    System.out.println(id);
                } else {
                    raf.seek(filePointer + recordLength);
                }
            }
            raf.close();
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void buildHashIndexFile(DynamicHashingDAO index) {
        try {
            if(index.delete()) {
                System.out.println("Hash successfully deleted!!");
                if(index.create()) System.out.println("Hash successfully recreated!!");
            }
            File file = new File(this.arquivo.mainFile);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                int recordLength;
                boolean validRecord;
                byte[] byteArray = new byte[4];
                raf.seek(4);
                ProgressBar progressBar = new ProgressBar(raf.length());
                for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                    long dataFilePosition = raf.getFilePointer();
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    long filePointer = raf.getFilePointer();
                    if (validRecord) {
                        int id = raf.readInt();
                        index.insertElement(new PageElement(id , dataFilePosition));
                        raf.seek(filePointer + recordLength);
                    } else {
                        raf.seek(filePointer + recordLength);
                    }
                    progressBar.updateStatus(i);
                }
                progressBar.done();
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Anime indexSearchInBPlusTree(int id, BPlusTreeDAO indexFile) {
        long pointer = indexFile.search(id);
        Anime result = null;
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.mainFile, "r")) {
            if (pointer > 0) {
                raf.seek(pointer);
                result = getAnime(raf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public Anime indexSearchInHash(int id , DynamicHashingDAO index) {
        long pointer = index.search(id);
        Anime result = null;
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.mainFile, "r")) {
            if (pointer > 0) {
                raf.seek(pointer + 8);
                result = getAnime(raf);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean removeAnimeWithHash(int id , DynamicHashingDAO index) {
        boolean status = false;
        long pointer = index.search(id);
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.mainFile , "rw")) {
            byte[] bytes = new byte[4];
            raf.seek(pointer);
            raf.read(bytes , 0 ,4);
            boolean validRecord = isValidRecord(bytes[0]);
            if(validRecord) {
                raf.seek(raf.getFilePointer() - 4);
                changeGraveyard(raf, bytes);
                index.removeElement(id);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return status;
    }
    public ArrayList<String> criarListaInvertidaType(ListaInvertidaDAO listFile) {
        boolean result = true;
        ArrayList<String> types = new ArrayList<>();
        try {
            RandomAccessFile animeRaf = new RandomAccessFile(this.arquivo.mainFile, "rw");
            RandomAccessFile listaRaf = new RandomAccessFile(listFile.arquivo.mainFile, "rw");
            ArrayList<Long> pointers = new ArrayList<>();
            int lastID = animeRaf.readInt();
            boolean isValid;
            int animeLength = 0;
            int animeID = 0;
            long recordPointer;
            byte[] bytes = new byte[4];
            Anime anime;
            ListaInvertida listaInvertida = new ListaInvertida();
            for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {//PEGA TODOS OS TYPES DISPONIVEIS
                recordPointer = animeRaf.getFilePointer();
                animeRaf.read(bytes, 0, 4);
                isValid = isValidRecord(bytes[0]);
                animeLength = getRecordLength(bytes, isValid);
                animeID = animeRaf.readInt();
                if (isValid) {
                    anime = getAnime(animeRaf);
                    if (!types.contains(anime.type)) {
                        types.add(anime.type);
                    }
                } else {
                    animeRaf.seek(recordPointer + animeLength + 4);
                }


            }
            listFile.listaIndices = types;
            listFile.writeIndices(listaRaf);
            animeRaf.seek(4);
            for (int j = 0; j < types.size(); j++) {

                for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {
                    recordPointer = animeRaf.getFilePointer();
                    animeRaf.read(bytes, 0, 4);
                    isValid = isValidRecord(bytes[0]);
                    animeLength = getRecordLength(bytes, isValid);
                    animeID = animeRaf.readInt();
                    if (isValid) {
                        anime = getAnime(animeRaf);
                        if (types.get(j).equals(anime.type)) {
                            pointers.add(recordPointer);
//                            anime.printAttributes();
                        }

                    } else {
                        animeRaf.seek(recordPointer + animeLength + 4);
                    }
                }
                listaInvertida.setPointers(pointers);
                listaInvertida.setElement(types.get(j).trim());
                System.out.println("lista: " + j);
                listFile.writeNewList(listaRaf, listaInvertida, true);
                pointers = new ArrayList<>();
                animeRaf.seek(4);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return types;
    }

    public void criarListaInvertidaStudio(ListaInvertidaDAO listFile) {
        boolean result = true;
        ArrayList<String> studios = new ArrayList<>();
        try {
            RandomAccessFile animeRaf = new RandomAccessFile(this.arquivo.mainFile, "rw");
            RandomAccessFile listaRaf = new RandomAccessFile(listFile.arquivo.mainFile, "rw");
            ArrayList<Long> pointers = new ArrayList<>();
            int lastID = animeRaf.readInt();
            boolean isValid;
            int animeLength = 0;
            int animeID = 0;
            long recordPointer;
            byte[] bytes = new byte[4];
            Anime anime;
            ListaInvertida listaInvertida = new ListaInvertida();
            for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {//PEGA TODOS OS TYPES DISPONIVEIS
                recordPointer = animeRaf.getFilePointer();
                animeRaf.read(bytes, 0, 4);
                isValid = isValidRecord(bytes[0]);
                animeLength = getRecordLength(bytes, isValid);
                animeID = animeRaf.readInt();
                if (isValid) {
                    anime = getAnime(animeRaf);
                    if (!studios.contains(anime.studio)) {
                        studios.add(anime.studio);
                    }
                } else {
                    animeRaf.seek(recordPointer + animeLength + 4);
                }


            }
            listFile.listaIndices = studios;
            listFile.writeIndices(listaRaf);
            animeRaf.seek(4);
            for (int j = 0; j < studios.size(); j++) {
                for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {
                    recordPointer = animeRaf.getFilePointer();
                    animeRaf.read(bytes, 0, 4);
                    isValid = isValidRecord(bytes[0]);
                    animeLength = getRecordLength(bytes, isValid);
                    animeID = animeRaf.readInt();
                    if (isValid) {
                        anime = getAnime(animeRaf);
                        if (studios.get(j).equals(anime.studio)) {
                            pointers.add(recordPointer);
//                            anime.printAttributes();
                        }

                    } else {
                        animeRaf.seek(recordPointer + animeLength + 4);
                    }
                }
                listaInvertida.setPointers(pointers);
                listaInvertida.setElement(studios.get(j).trim());
                System.out.println("lista : " + j);
                listFile.writeNewList(listaRaf, listaInvertida, true);
                pointers = new ArrayList<>();
                animeRaf.seek(4);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}