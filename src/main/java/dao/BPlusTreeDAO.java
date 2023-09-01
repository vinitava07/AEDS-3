package dao;
import model.Arquivo;
import model.BPlusTreePage;
import model.PageElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class BPlusTreeDAO {
    private Arquivo indexFile;
    private int bOrder;
    private long rootPage;

    public  BPlusTreeDAO(String binFileName) {
        try {
            if(new File(binFileName).exists() == false) throw new FileNotFoundException("O arquivo de index não existe!");
            indexFile = new Arquivo(binFileName);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw");
            this.rootPage = raf.readLong();
            bOrder = raf.readInt();
            raf.close();
            if(bOrder < 2) throw new Exception("O arquivo de index não é válido!");
        }
        catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public BPlusTreeDAO(String indexFileName , int bOrder) {
        try{
            if(bOrder < 3) throw new Exception("Não é possível uma árvore B de ordem menor que 3!");

            // TODO : check if the file already exists and, if so, ask if the user wants to overwrite and create a new one

            indexFile = new Arquivo(indexFileName);
            this.bOrder = bOrder;
            this.rootPage = 12;
            BPlusTreePage rootPage = new BPlusTreePage(bOrder);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile , "rw");
            raf.writeLong(this.rootPage);
            raf.writeInt(this.bOrder);
            for (int i = 0; i < rootPage.getPageLength(); i++) {
                raf.writeByte(0);
            }
            raf.close();
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public void insertElement(int id , long pointer) {
        PageElement element = new PageElement(id , pointer);
        try (RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile , "rw")) {
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            if(page.pageElements == (this.bOrder-1)) {
                page.isLeaf = true;
                int mid = page.pageElements / 2;
                int newPageElements = page.pageElements - mid;
                BPlusTreePage leftPage = new BPlusTreePage(this.bOrder);
                for (int i = 0; i < newPageElements; i++) {
                    leftPage.elements[i].setId(page.elements[mid + i].getId());
                    leftPage.elements[i].setPointer(page.elements[mid + i].getPointer());
                }
                page.setPageElements(mid);
                leftPage.setPageElements(newPageElements);
                PageElement promoted;

                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);
            }

            insertElement(raf , element);
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }
    private void insertElement(RandomAccessFile raf , PageElement element) {
        try {
            BPlusTreePage page = getPage(raf);
            if(page.pageElements == (this.bOrder-1)) {

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
            page.isLeaf = raf.readBoolean();
            page.pageElements = raf.readInt();
            page.pointers[0] = raf.readLong();
            for (int i = 1; i < this.bOrder; i++) {
                page.elements[i - 1].setId(raf.readInt());
                page.elements[i - 1].setPointer(raf.readLong());
                page.pointers[i] = raf.readLong();
            }
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }

        return page;
    }
}
