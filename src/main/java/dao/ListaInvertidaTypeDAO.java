package dao;

import model.Anime;
import model.Arquivo;
import model.ListaInvertida;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class ListaInvertidaTypeDAO<TYPE> {

    Arquivo arquivo;

    int qtdIndex;
    ArrayList<ListaInvertida> listaInvertidas;

    public ListaInvertidaTypeDAO(String fileName, boolean create) {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        this.arquivo = new Arquivo(fileName);
        this.listaInvertidas = new ArrayList<>();
        qtdIndex = 0;

    }

    public ListaInvertidaTypeDAO(String fileName) {
        try (RandomAccessFile raf = new RandomAccessFile(fileName, "rw")) {

        } catch (Exception e) {
            e.printStackTrace();
        }
        qtdIndex = 0;
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
                    listaInvertida.setPointersIndex(listaInvertida.getQtdPointers(), animePointer);
                    listaInvertida.setQtdPointers(listaInvertida.getQtdPointers() + 1);
                    writeNewList(raf, listaInvertida);
                }

            }
            if (!found) {
                System.out.println("nao existe, vai ser criado");
                listaInvertida.setElement(type);
                listaInvertida.setPointersIndex(0, animePointer);
                listaInvertida.setQtdPointers(1);
                writeNewList(raf, listaInvertida);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return arrayList;
    }

    public void writeNewList(RandomAccessFile raf, ListaInvertida lista) {
        try {
            raf.seek(raf.length());
            raf.writeBoolean(false);
            raf.writeInt(lista.getQtdPointers());
            ArrayList<Long> pointers = lista.getPointers();
            for (int i = 0; i < lista.getQtdPointers(); i++) {
                raf.writeLong(pointers.get(i));
            }
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
                listaInvertida.setPointersIndex(i, raf.readLong());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaInvertida;
    }

}
