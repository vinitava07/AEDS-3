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
            raf.seek(8);
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
            if(bOrder < 2) throw new Exception("Não é possível uma árvore B de ordem menor que 2!");

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
        insertElement(element);
    }
    private void insertElement(PageElement element) {
        
    }
}
