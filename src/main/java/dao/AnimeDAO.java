package dao;

import java.io.File;
/* ULTIMO ID 
    INT(4) */

/* lapide       length   id      name        type        episodes    studio     tags       rating      release_year
 * boolean(  int(4))    int(4)  varchar  char(5)      int(4)     varchar    varchar    float(4)    long(8)
 */

import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicLong;

import model.*;
import model.Record;
import util.ProgressMonitor;

public class AnimeDAO {

    Anime a;
    Arquivo arquivo;
    BigInteger rsaKey;
    String uncBin;
    RSA rsa;
    String publicKey;

    public AnimeDAO(String bin, String csv) {
        a = new Anime();
        String binFile = "../resources/" + bin;
        String csvFile = "../resources/" + csv;
        uncBin = "../resources/unc" + bin;

        rsa = new RSA();
        arquivo = new Arquivo(binFile, csvFile);
    }

    public AnimeDAO(String bin) {
        a = new Anime();
        arquivo = new Arquivo(("../resources/" + bin));
    }

    public AnimeDAO() {
        rsa = new RSA();
        arquivo = null;
        a = null;
    }

    public void csvToByte() {
        File csv = new File(arquivo.csvFile);
        Scanner sc = new Scanner(System.in);
        File bin = new File(uncBin);
        String animeText;
        Anime anime;
        System.out.println("Digite sua chave RSA gerada: ");
        this.publicKey = sc.nextLine();
        Record r = new Record();
        // (animeText = csvFile.readLine()) != null
        if (bin.exists()) {
            System.out.println("Arquivo recriado!");
            bin.delete();
            File f1 = new File("../resources/ListaAnimeC.bin");
            File f2 = new File("../resources/ListaAnimeBin.bin");
            f1.delete();
            f2.delete();
        }
        AtomicLong contador = new AtomicLong(0);
        try {
            RandomAccessFile csvFile = new RandomAccessFile(csv, "r");
            RandomAccessFile binFile = new RandomAccessFile(bin, "rw");

            csvFile.readLine(); // read csv file header
            anime = new Anime();
            long amountOfRecords = 100; // total amount of records: 18495
            ProgressMonitor progressMonitor = new ProgressMonitor("Building Bin FILE", contador, amountOfRecords);
            progressMonitor.start();
            while (contador.getAndIncrement() < amountOfRecords) {
                animeText = csvFile.readLine();
                anime.parseAnime(animeText);
                r.setAnime(anime);
                // System.out.println(animeText);
                // anime.printAttributes();
                writeAnimeBytes(r, binFile, false);
            }
            progressMonitor.endProcess();
            progressMonitor.join();
            cipherFile(binFile);
            csvFile.close();
            binFile.close();
            bin.delete();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cipherFile(RandomAccessFile rafBin) {
        try {
            rafBin.seek(0);
            File cFile = new File("ListaAnimeC.bin");
            if (cFile.exists()) {
                cFile.delete();
            }
            RandomAccessFile raf = new RandomAccessFile("../resources/ListaAnimeC.bin", "rw");
            Scanner sc = new Scanner(System.in);
            BigInteger b = new BigInteger(this.publicKey);
            byte[] bArray;
            String line;
            StringBuilder sb = new StringBuilder();
            rafBin.seek(0);
            System.out.println();
            byte[] bytes = new byte[(int) rafBin.length()];
            int j = 0;
            rafBin.read(bytes, 0, (int) rafBin.length());
//            while (rafBin.getFilePointer() < rafBin.length()) {
//                bytes[j] = rafBin.readByte();
//                System.out.println(String.format("0x%08X", bytes[j++]));
//            }
            String message = new String(bytes, StandardCharsets.UTF_8);
            rsa.cipherMessage(b, bytes);
            ProgressMonitor progressMonitor = new ProgressMonitor("Escrevendo arquivo criptografado: ");
            progressMonitor.start();

            for (int i = 0; i < rsa.getcMessageSize(); i++) {
                bArray = rsa.getcMessage()[i].toByteArray();
                raf.write((byte) bArray.length);
                raf.write(rsa.getcMessage()[i].toByteArray());
            }
            raf.write(-1);
            progressMonitor.endProcess();
            progressMonitor.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void uncipherFile() {
        try {
            System.out.println("Descriptografando aquivo: ");
            RandomAccessFile rafBin = new RandomAccessFile("../resources/ListaAnimeBin.bin", "rw");
            RandomAccessFile rafCiph = new RandomAccessFile("../resources/ListaAnimeC.bin", "rw");
            ArrayList<BigInteger> arrayList = new ArrayList<>();
            byte size;
            byte[] bigInt;
            rafCiph.seek(0);
            while ((size = rafCiph.readByte()) != -1) {
//                size = rafCiph.readByte();
                bigInt = new byte[size];
                rafCiph.read(bigInt, 0, size);
                arrayList.add(new BigInteger(bigInt));

            }
            byte[] uncString;
            BigInteger[] n = new BigInteger[arrayList.size()];
            for (int i = 0; i < arrayList.size(); i++) {
                n[i] = arrayList.get(i);
            }
            uncString = rsa.uncipherMessage((n));
            rafBin.seek(0);
            for (int i = 0; i < n.length; i++) {
                rafBin.write(uncString[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected long writeAnimeBytes(Record r, RandomAccessFile raf, boolean update) throws Exception {
        long res = 0;
        int length = r.getAnime().getByteLength();
        int lastId = -1;
        if (update) {
            res = raf.getFilePointer();
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
            res = raf.length();
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
        return res;
    }

    public long createAnime(Anime anime) throws Exception {
        uncipherFile();
        File file = new File(this.arquivo.binFile);
        long pos;
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        Record r = new Record();
        r.setAnime(anime);
        pos = writeAnimeBytes(r, raf, false);
        cipherFile(raf);
        raf.close();
        return pos;
    }

    public void printAllAnime() {
        File file = new File(this.arquivo.binFile);
        uncipherFile();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            int id = 0;
            lastId = raf.readInt();
//            System.out.println("Ultimo id: " + lastId);
            for (long i = 0; i < raf.length() - 4; i += (4 + recordLength)) {
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                long filePointer = raf.getFilePointer();
                if (validRecord) {
                    id = raf.readInt();
                    System.out.println("ID: " + id);
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
        file.delete();

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
        uncipherFile();
        Anime result = null;
        int recordLength;
        boolean found = false;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int animeID;
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.binFile, "rw")) {

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
//                            System.out.println("Registro econtrado!");

                        }
                    }
                    raf.seek(raf.getFilePointer() + (recordLength - 4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!found) {
            System.out.println("O registro não existe!");
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return result;
    }

    public Anime removeAnime(int id) throws Exception {
        return sequentialDelete(id);
    }

    private Anime sequentialDelete(int id) throws Exception {

        Anime deletedRecord = null;
        uncipherFile();

        try (RandomAccessFile raf = new RandomAccessFile(arquivo.binFile, "rw")) {
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
            cipherFile(raf);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
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
        uncipherFile();
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
        try (RandomAccessFile raf = new RandomAccessFile(arquivo.binFile, "rw")) {
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
//                                System.out.println("menor");
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
            cipherFile(raf);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();

    }

    public void buildBPlusTreeIndexFile(BPlusTreeDAO index) {
//        BPlusTreeDAO indexFile = new BPlusTreeDAO("../resources/indexB.bin",8);
//        BPlusTreeDAO bPlusTreeDAO = new BPlusTreeDAO("../resources/indexB.bin",8);

        uncipherFile();
        File file = new File(this.arquivo.binFile);
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            int lastId = 0;
            int recordLength;
            boolean validRecord;
            byte[] byteArray = new byte[4];
            raf.seek(0);
            lastId = raf.readInt();

            AtomicLong i = new AtomicLong(0);
            ProgressMonitor progressMonitor = new ProgressMonitor("Building B+ Tree", i, raf.length() - 4);
            progressMonitor.start();
            while (i.get() < raf.length() - 4) {
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
                i.addAndGet((4 + recordLength));
            }
            progressMonitor.endProcess();
            progressMonitor.join();
            cipherFile(raf);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        file.delete();
    }

    public void buildHashIndexFile(DynamicHashingDAO index) {
        try {
            uncipherFile();
            if (index.delete()) {
                System.out.println("Hash successfully deleted!!");
                if (index.create()) System.out.println("Hash successfully recreated!!");
            }
            File file = new File(this.arquivo.binFile);
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                int recordLength;
                boolean validRecord;
                byte[] byteArray = new byte[4];
                raf.seek(4);
                AtomicLong i = new AtomicLong(0);
                ProgressMonitor progressMonitor = new ProgressMonitor("Building Hash", i, raf.length() - 4);
                progressMonitor.start();
                while (i.get() < raf.length() - 4) {
                    long dataFilePosition = raf.getFilePointer();
                    raf.read(byteArray, 0, 4);
                    validRecord = isValidRecord(byteArray[0]);
                    recordLength = getRecordLength(byteArray, validRecord);
                    long filePointer = raf.getFilePointer();
                    if (validRecord) {
                        int id = raf.readInt();
                        index.insertElement(new PageElement(id, dataFilePosition));
                        raf.seek(filePointer + recordLength);
                    } else {
                        raf.seek(filePointer + recordLength);
                    }
                    i.addAndGet((4 + recordLength));
                }
                progressMonitor.endProcess();
                progressMonitor.join();
                cipherFile(raf);
                raf.close();
                file.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void indexInsertInBplusTree(long pos, BPlusTreeDAO indexFile) {
        uncipherFile();
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {
            int id = raf.readInt();
            indexFile.insertElement(id, pos + 8);
            cipherFile(raf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
    }

    public Anime indexSearchInBPlusTree(int id, BPlusTreeDAO indexFile) {
        uncipherFile();
        long pointer = indexFile.search(id);
//        System.out.println("B+: " + pointer);
        Anime result = null;
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {
            if (pointer > 0) {
                raf.seek(pointer - 8);
                byte[] t = new byte[4];
                raf.read(t, 0, 4);
                boolean valid = isValidRecord(t[0]);
                if (valid) {
                    raf.seek(pointer);
                    result = getAnime(raf);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return result;
    }

    public long removeAnimeWithBPlusTree(int id, BPlusTreeDAO indexFile) {
        uncipherFile();
        Anime result = null;
        long pointer = indexFile.search(id);
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "rw")) {
            raf.seek(pointer - 8);
            byte[] bytes = new byte[4];
            raf.read(bytes, 0, 4);
            raf.seek(pointer - 8);
            changeGraveyard(raf, bytes);
            cipherFile(raf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return pointer - 8;
    }

    public void indexInsertInHash(long pos, DynamicHashingDAO indexFile) {
        uncipherFile();
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {
            int id = raf.readInt();
            PageElement pageElement = new PageElement(id, pos);
            indexFile.insertElement(pageElement);
            cipherFile(raf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
    }


    public Anime indexSearchInHash(int id, DynamicHashingDAO index) {
        uncipherFile();
        long pointer = index.search(id);
//        System.out.println("Hash: " + pointer);
        Anime result = null;
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {

            if (pointer > 0) {
                raf.seek(pointer);
                byte[] t = new byte[4];
                raf.read(t, 0, 4);
                boolean valid = isValidRecord(t[0]);
                if (valid) {
                    raf.seek(pointer + 8);
                    result = getAnime(raf);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return result;
    }

    public long removeAnimeWithHash(int id, DynamicHashingDAO index) {
        uncipherFile();
        boolean status = false;
        long pointer = index.search(id);
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "rw")) {
            byte[] bytes = new byte[4];
            raf.seek(pointer);
            raf.read(bytes, 0, 4);
            boolean validRecord = isValidRecord(bytes[0]);
            if (validRecord) {
                raf.seek(raf.getFilePointer() - 4);
                changeGraveyard(raf, bytes);
                index.removeElement(id);
                cipherFile(raf);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();

        return pointer - 8;
    }

    public ArrayList<String> criarListaInvertidaType(ListaInvertidaDAO listFile) {
        boolean result = true;
        ArrayList<String> types = new ArrayList<>();
        uncipherFile();
        try {
            RandomAccessFile animeRaf = new RandomAccessFile(this.arquivo.binFile, "rw");
            RandomAccessFile listaRaf = new RandomAccessFile(listFile.arquivo.binFile, "rw");
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
            AtomicLong j = new AtomicLong(0);
            ProgressMonitor progressMonitor = new ProgressMonitor("Building Lista Invertida Type", j, types.size());
            progressMonitor.start();
            while (j.get() < types.size()) {

                for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {
                    recordPointer = animeRaf.getFilePointer();
                    animeRaf.read(bytes, 0, 4);
                    isValid = isValidRecord(bytes[0]);
                    animeLength = getRecordLength(bytes, isValid);
                    animeID = animeRaf.readInt();
                    if (isValid) {
                        anime = getAnime(animeRaf);
                        if (types.get((int) j.get()).equals(anime.type)) {
                            pointers.add(recordPointer);
//                            anime.printAttributes();
                        }

                    } else {
                        animeRaf.seek(recordPointer + animeLength + 4);
                    }
                }
                listaInvertida.setPointers(pointers);
                listaInvertida.setElement(types.get((int) j.get()).trim());
                listFile.writeNewList(listaRaf, listaInvertida, true);
                pointers = new ArrayList<>();
                animeRaf.seek(4);
                j.getAndIncrement();
            }
            progressMonitor.endProcess();
            progressMonitor.join();
            cipherFile(animeRaf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return types;
    }

    public void criarListaInvertidaStudio(ListaInvertidaDAO listFile) {
        uncipherFile();
        boolean result = true;
        ArrayList<String> studios = new ArrayList<>();
        try {
            RandomAccessFile animeRaf = new RandomAccessFile(this.arquivo.binFile, "rw");
            RandomAccessFile listaRaf = new RandomAccessFile(listFile.arquivo.binFile, "rw");
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

            AtomicLong j = new AtomicLong(0);
            ProgressMonitor progressMonitor = new ProgressMonitor("Building Lista invertida Studio", j, studios.size());
            progressMonitor.start();
            while (j.get() < studios.size()) {
                for (int i = 4; (i < animeRaf.length()); i += (4 + animeLength)) {
                    recordPointer = animeRaf.getFilePointer();
                    animeRaf.read(bytes, 0, 4);
                    isValid = isValidRecord(bytes[0]);
                    animeLength = getRecordLength(bytes, isValid);
                    animeID = animeRaf.readInt();
                    if (isValid) {
                        anime = getAnime(animeRaf);
                        if (studios.get((int) j.get()).equals(anime.studio)) {
                            pointers.add(recordPointer);
//                            anime.printAttributes();
                        }

                    } else {
                        animeRaf.seek(recordPointer + animeLength + 4);
                    }
                }
                listaInvertida.setPointers(pointers);
                listaInvertida.setElement(studios.get((int) j.get()).trim());
                listFile.writeNewList(listaRaf, listaInvertida, true);
                pointers = new ArrayList<>();
                animeRaf.seek(4);
                j.getAndIncrement();
            }
            progressMonitor.endProcess();
            progressMonitor.join();
            cipherFile(animeRaf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();
    }

    public void removeListaInvertidaType(int id, long pos, ListaInvertidaDAO listaInvertidaDAO, BPlusTreeDAO index) {
        uncipherFile();
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {
            raf.seek(pos + 8);
            Anime a = getAnime(raf);
            listaInvertidaDAO.deleteIndice(a.type, pos);
            cipherFile(raf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();

    }

    public void removeListaInvertidaStudio(int id, long pos, ListaInvertidaDAO listaInvertidaDAO, BPlusTreeDAO index) {
        uncipherFile();
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.binFile, "r")) {
            raf.seek(pos + 8);
            Anime a = getAnime(raf);
            listaInvertidaDAO.deleteIndice(a.studio, pos);
            cipherFile(raf);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File f = new File(arquivo.binFile);
        f.delete();

    }


    public long updateWithBPlus(int id, Anime anime, BPlusTreeDAO index) {
        uncipherFile();
        boolean status = false;
        int recordLength;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int recordId;
        int newAnimeSize = anime.getByteLength();
        Record r = new Record();
        r.setAnime(anime);
        long where = index.search(id);
        long newPosition = where;
        if (where != -1) {
            try (RandomAccessFile raf = new RandomAccessFile(arquivo.binFile, "rw")) {
                raf.seek(where - 8);
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                recordId = raf.readInt();
//                System.out.println("------ " + recordId + " ------- " + id + " -------");
                r.setGraveyard(validRecord);
                r.setSize(newAnimeSize);
                r.setId(recordId);
                r.setAnime(anime);
                if (recordLength >= newAnimeSize) {
//                    System.out.println("menor");
                    writeAnimeBytes(r, raf, true);
                    //No need to update the Tree!!
                } else {
                    raf.seek(where - 8);
                    changeGraveyard(raf, byteArray);

                    newPosition = raf.length() + 8; //The updated record will be at the end of the file + id(4) + length(4)
                    if (index.updateElement(new PageElement(id, newPosition)))
                        System.out.println("Anime successfully updated!!");
                    else System.out.println("Failed to update Anime!!");

//                    System.out.println("730: Where:" + where);
//                    System.out.println("731: newPosition :" + newPosition);
                    raf.seek(where);
                    writeAnimeBytes(r, raf, false);
                }
//                System.out.println("Registro atualizado!");
                cipherFile(raf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return newPosition;
    }

    public long updateWithDynamicHash(int id, Anime anime, DynamicHashingDAO index) {
        uncipherFile();
        boolean status = false;
        int recordLength;
        boolean validRecord;
        byte[] byteArray = new byte[4];
        int recordId;
        int newAnimeSize = anime.getByteLength();
        Record r = new Record();
        r.setAnime(anime);
        long where = index.search(id);
//        System.out.println(where);
        long newPosition = where + 8;
        if (where != -1) {
            try (RandomAccessFile raf = new RandomAccessFile(arquivo.binFile, "rw")) {
                raf.seek(where);
                raf.read(byteArray, 0, 4);
                validRecord = isValidRecord(byteArray[0]);
                recordLength = getRecordLength(byteArray, validRecord);
                recordId = raf.readInt();
                r.setGraveyard(validRecord);
                r.setSize(newAnimeSize);
                r.setId(recordId);
                r.setAnime(anime);
                if (recordLength >= newAnimeSize) {
//                    System.out.println("menor");
                    writeAnimeBytes(r, raf, true);
                    //No need to update the Tree!!
                } else {
                    raf.seek(where);
                    changeGraveyard(raf, byteArray);

                    newPosition = raf.length() + 8; //The updated record will be at the end of the file + id(4) + length(4)
                    if (index.updateElement(new PageElement(id, newPosition - 8)))
                        System.out.println("Anime successfully updated!!");
                    else System.out.println("Failed to update Anime!!");

//                    System.out.println("730: Where:" + where);
//                    System.out.println("731: newPosition :" + newPosition);
                    raf.seek(where);
                    writeAnimeBytes(r, raf, false);
                }
                cipherFile(raf);
//                System.out.println("Registro atualizado!");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        File f = new File(arquivo.binFile);
        f.delete();
        return newPosition;
    }

    public void intercalation(int caminho, int bloco) {
        uncipherFile();
        System.out.println(arquivo.binFile);
        RecordDAO recordDAO = new RecordDAO(arquivo.binFile);
        File f = new File(arquivo.binFile);
        recordDAO.intercalacaoBalanceada(caminho, bloco);
        try (RandomAccessFile raf = new RandomAccessFile(f, "rw")) {
            cipherFile(raf);
            f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}