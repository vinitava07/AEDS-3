package dao;
import model.Arquivo;
import model.BPlusTreePage;
import model.PageElement;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

public class BPlusTreeDAO {
    private Arquivo indexFile;
    private int bOrder;
    private long rootPage;

    public  BPlusTreeDAO(String binFileName) {
        indexFile = new Arquivo(binFileName);
        try (RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw")) {
            if(raf.length() < 4) throw new FileNotFoundException("O arquivo de index não existe!");
            bOrder = raf.readInt();
            if(bOrder < 2) throw new NoSuchFieldException("O arquivo de index não é válido!");
        }
        catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public BPlusTreeDAO(String indexFileName , int bOrder) {
        try{
            if(bOrder < 2) throw new Exception("Não é possível uma árvore B de ordem menor que 2!");
            indexFile = new Arquivo(indexFileName);
            this.bOrder = bOrder;
            this.rootPage = 8;
            BPlusTreePage rootPage = new BPlusTreePage(bOrder);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile , "rw");
            raf.writeLong(this.rootPage);
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
        insertElement(element);
    }
    private void insertElement(PageElement element) {

    }
}
