package dao;

import model.Anime;
import model.Arquivo;
import model.ListaInvertida;
import model.Record;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;

public class ListaInvertidaTypeDAO {
    //  qtdindex + AllElements  + Lapide +    qtd Ponteiros + Elementos + nPonteiros
    //  int(4)        varchar     Boolean(1)    int(4)          UTF(N)      qtdPonteiros * 8
    Arquivo arquivo;
    int qtdIndex;
    ArrayList<String> listaIndices;

    public ListaInvertidaTypeDAO(String fileName, boolean create) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        this.arquivo = new Arquivo(fileName);
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.nameBin, "rw")) {
            this.listaIndices = new ArrayList<>();
            qtdIndex = 0;
            raf.writeInt(qtdIndex);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ListaInvertidaTypeDAO(String fileName) {
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {
            this.qtdIndex = raf.readInt();
            this.arquivo = new Arquivo(fileName);
            listaIndices = new ArrayList<>();
            for (int i = 0; i < qtdIndex; i++) {
                listaIndices.add(raf.readUTF());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        qtdIndex = 0;
    }

    public void addToList(Anime anime, long animePointer) {
        insertType(anime.type, animePointer);
    }

    public ArrayList<Long> insertType(String type, long animePointer) {
        ArrayList<Long> arrayList = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.nameBin, "rw")) {
            int qtdTypes = raf.readInt();
            boolean isGraveyard = false;
            boolean found = false;
            long filePointerBefore = 0;
            if (qtdTypes == 0) return arrayList;
            ListaInvertida listaInvertida = new ListaInvertida();
            for (int i = 0; i < qtdTypes && !found; i++) {
                filePointerBefore = raf.getFilePointer();
                isGraveyard = raf.readBoolean();
                listaInvertida.setGraveyard(isGraveyard);
                if (!isGraveyard) {
                    System.out.println("passou por um registro movido");
                    listaInvertida.setGraveyard(raf.readBoolean());
                    listaInvertida = getListaInvertida(raf);
                }
                if (type.equals(listaInvertida.getElement())) {
                    System.out.println("o type ja existe");
                    found = true;
                    raf.seek(filePointerBefore);
                    raf.writeBoolean(true);
                    listaInvertida.addPointer(listaInvertida.getQtdPointers(), animePointer);
//                    listaInvertida.setQtdPointers(listaInvertida.getQtdPointers() + 1);
                    writeNewList(raf, listaInvertida, false);
                }

            }
            if (!found) {
                System.out.println("nao existe, vai ser criado");
                listaInvertida.setElement(type);
                listaInvertida.addPointer(0, animePointer);
//                listaInvertida.setQtdPointers(1);
                writeNewList(raf, listaInvertida, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public void writeNewList(RandomAccessFile raf, ListaInvertida lista, boolean newList) {
        try {
            raf.seek(raf.length());
            raf.writeBoolean(false);//lapide
            raf.writeInt(lista.getQtdPointers());//qtd ponteiros
            raf.writeUTF(lista.getElement());//elemetno
            ArrayList<Long> pointers = lista.getPointers();
            for (int i = 0; i < lista.getQtdPointers(); i++) {
                raf.writeLong(pointers.get(i));
            }
//            if (newList) {
//                this.qtdIndex++;
//                raf.seek(0);
//                raf.writeInt(qtdIndex);
//            }
            System.out.println("Lista Inserida no arquivo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ListaInvertida getListaInvertida(RandomAccessFile raf) {
        ListaInvertida listaInvertida = new ListaInvertida();
        try {
            long originalFP = raf.getFilePointer();
            listaInvertida.setQtdPointers(raf.readInt());
            listaInvertida.setElement(raf.readUTF());
            for (int i = 0; i < listaInvertida.getQtdPointers(); i++) {
                listaInvertida.addPointer(i, raf.readLong());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaInvertida;
    }

    public void printIndex(AnimeDAO animeDAO) {
        try (RandomAccessFile raf = new RandomAccessFile(this.arquivo.nameBin, "rw")) {
            System.out.println("Digite o número índice a ser mostrado: ");
            for (int i = 0; i < this.listaIndices.size(); i++) {
                System.out.println("(" + i + ") " + this.listaIndices.get(i));
            }
            ArrayList<Long> animePointers = searchIndex(raf, this.listaIndices.get(0));
            Anime anime = new Anime();
            int animeSize;
            int animeID;
            RandomAccessFile rafAnime = new RandomAccessFile(animeDAO.arquivo.nameBin, "rw");
            for (int i = 0; i < animePointers.size(); i++) {
                rafAnime.seek(animePointers.get(i));
                animeSize = rafAnime.readInt();
                animeID = rafAnime.readInt();
                anime.name = rafAnime.readUTF();
                byte[] type = new byte[5];
                rafAnime.read(type, 0, 5);
                anime.type = new String(type, StandardCharsets.UTF_8);
                anime.episodes = rafAnime.readInt();
                anime.studio = rafAnime.readUTF();
                anime.tags = rafAnime.readUTF();
                anime.rating = rafAnime.readFloat();
                anime.release_year = anime.longToTimestamp(rafAnime.readLong());
                System.out.println("ID: " + animeID);
                anime.printAttributes();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private ArrayList<Long> searchIndex(RandomAccessFile raf, String element) {
        ArrayList<Long> pointers = new ArrayList<>();

        try {
            raf.seek(0);
            int numberIndex = raf.readInt();
            for (int i = 0; i < numberIndex; i++) {
                raf.readUTF();
            }
            boolean notAvailable;
            int qtdPointers;
            String fileElement;
            boolean found = false;
            for (int i = 0; i < numberIndex && !found; i++) {
                notAvailable = raf.readBoolean();
                qtdPointers = raf.readInt();
                fileElement = raf.readUTF();
                long currentFP = raf.getFilePointer();
                if (!notAvailable) {
                    if (fileElement.equals(element)) {
                        for (int j = 0; j < qtdPointers; j++) {
                            pointers.add(raf.readLong());
                            found = true;
                        }
                    } else {
                        raf.seek(currentFP + (qtdPointers * 8L));
                    }
                } else {
                    raf.seek(currentFP + (qtdPointers * 8L));

                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return pointers;
    }

    public void writeIndices(RandomAccessFile raf) {
        try {
            this.qtdIndex = listaIndices.size();
            raf.writeInt(qtdIndex);
            for (int i = 0; i < this.qtdIndex; i++) {
                raf.writeUTF(this.listaIndices.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
