package dao;

import model.Arquivo;
import model.BPlusTreePage;
import model.PageElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
/*
        [LONG pointerToRoot: (8) + INT ordTree (4)]
        BOOL         INT               LONG               INT         LONG                LONG
        isLeaf(1)    pageElements(4)   leftPointer(8)     id(4)       elementPointer(8)   rightPointer(8)

    * */
public class BPlusTreeDAO {
    private Arquivo indexFile;
    private int bOrder;
    private long rootPage;

    public BPlusTreeDAO(String binFileName) {
        try {
            if (new File(binFileName).exists() == false)
                throw new FileNotFoundException("O arquivo de index não existe!");
            indexFile = new Arquivo(binFileName);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw");
            this.rootPage = raf.readLong();
            bOrder = raf.readInt();
            raf.close();
            if (bOrder < 2) throw new Exception("O arquivo de index não é válido!");
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public BPlusTreeDAO(String indexFileName, int bOrder) {
        try {
            if (bOrder < 3) throw new Exception("Não é possível uma árvore B de ordem menor que 3!");

            // TODO : check if the file already exists and, if so, ask if the user wants to overwrite and create a new one

            indexFile = new Arquivo(indexFileName);
            this.bOrder = bOrder;
            this.rootPage = 12;
            BPlusTreePage rootPage = new BPlusTreePage(bOrder);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw");
            raf.writeLong(this.rootPage);
            raf.writeInt(this.bOrder);
            System.out.println(rootPage.getPageLength());
            for (int i = 0; i < rootPage.getPageLength(); i++) {
                raf.writeByte(0);
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void insertElement(int id, long pointer) { // TODO: A RAIZ TEM QUE TER 2 METODOS DE QUEBRA
        PageElement element = new PageElement(id, pointer);
        try (RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw")) {
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            if (page.pageElements == (this.bOrder - 1)) {
//                page.isLeaf = true;
                int mid = page.pageElements / 2;
                int newPageElements = page.pageElements - mid; // elementos na pagina da direita
                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);
                BPlusTreePage newRoot = new BPlusTreePage(this.bOrder);
                PageElement promoted = new PageElement(page.elements[mid].getId(), page.pointers[mid]);
                newRoot.elements[0] = promoted;
                for (int i = 0; i < newPageElements; i++) {
                    rightPage.elements[i] = page.elements[mid + i];

//                    rightPage.pointers[i] = page.pointers[mid + i]; COPIAR ISSO QUANDO NAO FOR FOLHA
                }
                page.setPageElements(mid);
                rightPage.setPageElements(newPageElements);
                newRoot.pointers[0] = this.rootPage;
                newRoot.pointers[1] = writeNewPage(raf, rightPage);
                newRoot.isLeaf = false;
                this.rootPage = writeNewPage(raf, newRoot);
            }
//            insertElement(raf, element);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void insertElement(RandomAccessFile raf, PageElement element) {
        try {
            BPlusTreePage page = getPage(raf);
            if (page.pageElements == (this.bOrder - 1)) {

            }
//            if(page.isLeaf) {
//                for (int i = 0; i < page.pageElements; i++) {
//                    if(page.elements[i].getId() < element.getId())
//                }
//            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private BPlusTreePage getPage(RandomAccessFile raf) {
        BPlusTreePage page = new BPlusTreePage(this.bOrder);
        try {
            long pointer = raf.getFilePointer();
            page.isLeaf = raf.readBoolean();
            page.pageElements = raf.readInt();

            page.pointers[0] = raf.readLong();
            for (int i = 1; i < this.bOrder; i++) {
                page.elements[i - 1].setId(raf.readInt());
                page.elements[i - 1].setPointer(raf.readLong());

                page.pointers[i] = raf.readLong();
            }
            raf.seek(pointer);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }

        return page;
    }

    public long writeNewPage(RandomAccessFile raf, BPlusTreePage bPlusTreePage) { // write a page on the final and seek fp to the original pos

        try {
            long originalFp = raf.getFilePointer();
            raf.seek(raf.length());
            long pageFp = raf.getFilePointer();
            raf.writeBoolean(bPlusTreePage.isLeaf);
            raf.writeInt(bPlusTreePage.pageElements);
            raf.writeLong(bPlusTreePage.pointers[0]);
            for (int i = 1; i < this.bOrder; i++) {
                raf.writeInt(bPlusTreePage.elements[i - 1].getId());
                raf.writeLong(bPlusTreePage.elements[i - 1].getPointer());
                raf.writeLong(bPlusTreePage.pointers[i]);
            }
            raf.seek(originalFp);
            return pageFp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
