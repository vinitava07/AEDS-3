package dao;

import model.Arquivo;
import model.BPlusTreePage;
import model.PageElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;

/*
        [LONG pointerToRoot: (8) + INT ordTree (4)]
        BOOL         INT               LONG               INT         LONG                LONG
        isLeaf(1)    pageElements(4)   leftPointer(8)     id(4)       elementPointer(8)   rightPointer(8)

    * */
public class BPlusTreeDAO {
    public Arquivo indexFile;
    private int bOrder;
    private long rootPage;

    public BPlusTreeDAO(String binFileName) {
        try {
            if (new File(binFileName).exists() == false)
                throw new FileNotFoundException("The file: \"" + binFileName + "\" doesn't exist!");
            indexFile = new Arquivo(binFileName);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw");
            this.rootPage = raf.readLong();
            bOrder = raf.readInt();
            raf.close();
            if (bOrder < 2) throw new Exception("O arquivo de index não é válido!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BPlusTreeDAO(String indexFileName, int bOrder) {
        try {
            if (bOrder < 3) throw new Exception("Não é possível uma árvore B de ordem menor que 3!");
            File file = new File(indexFileName);
            if (file.exists()) {
                file.delete();
                System.out.println("Arquivo deletado!");
            }

            indexFile = new Arquivo(indexFileName);
            this.bOrder = bOrder;
            this.rootPage = 12;
            BPlusTreePage rootPage = new BPlusTreePage(bOrder);
            RandomAccessFile raf = new RandomAccessFile(indexFile.mainFile, "rw");
            raf.writeLong(this.rootPage);
            raf.writeInt(this.bOrder);
            writeNewPage(raf, rootPage);
            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertElement(int id, long pointer) {
        PageElement element = new PageElement(id, pointer);
        try (RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw")) {
            raf.seek(this.rootPage);
            if (getPage(raf).numElements == (this.bOrder - 1)) {
                splitRootPage(raf);
            }
            raf.seek(this.rootPage);
            insertElement(raf, element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertElement(RandomAccessFile raf, PageElement element) {
        try {
            BPlusTreePage page = getPage(raf);
            if (page.isLeaf) {
                ArrayList<PageElement> elementsList = new ArrayList<>();
                for (int i = 0; i < page.numElements; i++) {
                    elementsList.add(page.elements[i]);
                }
                elementsList.add(element);
                elementsList.sort(Comparator.comparingInt(PageElement::getId));
                page.numElements++;
                for (int i = 0; i < page.numElements; i++) {
                    page.elements[i] = elementsList.get(i);
                }
                overWritePage(raf, raf.getFilePointer(), page);
            } else {
                long currentPage = raf.getFilePointer();
                raf.seek(whereToGo(page, element.getId()));
                if (getPage(raf).numElements == (this.bOrder - 1)) {
                    // TODO : ANOTAÇÃO NA FOLHA
                    PageElement promoted = splitPage(raf);
                    page.insertPromoted(promoted, getPromotedRightPointer(raf));
                    overWritePage(raf, currentPage, page);
                    raf.seek(whereToGo(page, element.getId()));
                }
                insertElement(raf, element);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BPlusTreePage getPage(RandomAccessFile raf) {
        BPlusTreePage page = new BPlusTreePage(this.bOrder);
        try {
            long originalFp = raf.getFilePointer();
            page.isLeaf = raf.readBoolean();
            page.numElements = raf.readInt();

            page.pointers[0] = raf.readLong();
            for (int i = 1; i < this.bOrder; i++) {
                page.elements[i - 1].setId(raf.readInt());
                page.elements[i - 1].setPointer(raf.readLong());

                page.pointers[i] = raf.readLong();
            }
            raf.seek(originalFp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return page;
    }

    private long whereToGo(BPlusTreePage from, int id) {// seek for the next page to go
        long whereTo = from.pointers[from.numElements]; // considers element's id is greater than any page element
        boolean found = false;
        for (int i = 0; (i < from.numElements && !found); i++) {
            if (from.elements[i].getId() == id) {
                whereTo = from.elements[i].getPointer();
                found = true;
            } else {
                if (from.elements[i].getId() > id) {
                    whereTo = from.pointers[i];
                    found = true;
                }
            }
        }

        return whereTo;
    }

    private long writeNewPage(RandomAccessFile raf, BPlusTreePage bPlusTreePage) { // write a page on the final and seek fp to the original pos

        try {
            long originalFp = raf.getFilePointer();
            raf.seek(raf.length());
            long pageFp = raf.getFilePointer();
            raf.writeBoolean(bPlusTreePage.isLeaf);
            raf.writeInt(bPlusTreePage.numElements);
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

    private void overWritePage(RandomAccessFile raf, long where, BPlusTreePage page) {
        try {
            long originalFp = raf.getFilePointer();
            raf.seek(where);
            raf.writeBoolean(page.isLeaf);
            raf.writeInt(page.numElements);
            raf.writeLong(page.pointers[0]);
            for (int i = 1; i < this.bOrder; i++) {
                raf.writeInt(page.elements[i - 1].getId());
                raf.writeLong(page.elements[i - 1].getPointer());
                raf.writeLong(page.pointers[i]);
            }
            raf.seek(originalFp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitRootPage(RandomAccessFile raf) {
        try {
            long originalFp = raf.getFilePointer();
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            if (page.isLeaf) {
                int mid = page.numElements / 2;
                int newPageElements = page.numElements - mid; // elementos na pagina da direita copiando o elemento
                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);
                BPlusTreePage newRoot = new BPlusTreePage(this.bOrder);
                newRoot.setNumElements(1);
                for (int i = 0; i < newPageElements; i++) {
                    rightPage.elements[i] = page.elements[mid + i];
                }
                page.setNumElements(mid);
                rightPage.setNumElements(newPageElements);
                newRoot.pointers[0] = this.rootPage;
                newRoot.pointers[1] = writeNewPage(raf, rightPage);
                page.pointers[this.bOrder - 1] = newRoot.pointers[1];
                overWritePage(raf, this.rootPage, page);
                PageElement promoted = new PageElement(page.elements[mid].getId(), newRoot.pointers[1]);
                newRoot.elements[0] = promoted;
                newRoot.isLeaf = false;
                this.rootPage = writeNewPage(raf, newRoot);
                raf.seek(0);
                raf.writeLong(this.rootPage);
            } else {
                int mid = page.numElements / 2;
                int newPageElements = page.numElements - mid - 1; // elementos na pagina da direita não copiando o elemento
                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);
                BPlusTreePage newRoot = new BPlusTreePage(this.bOrder);
                newRoot.setNumElements(1);

                for (int i = 0; i < newPageElements; i++) {
                    rightPage.elements[i] = page.elements[mid + i + 1];
                    rightPage.pointers[i] = page.pointers[mid + i + 1];
                }
                rightPage.pointers[newPageElements] = page.pointers[mid + 1 + newPageElements];

                page.setNumElements(mid);
                overWritePage(raf, this.rootPage, page);
                rightPage.setNumElements(newPageElements);
                rightPage.isLeaf = false;
                newRoot.pointers[0] = this.rootPage;
                newRoot.pointers[1] = writeNewPage(raf, rightPage);
                PageElement promoted = new PageElement(page.elements[mid].getId(), page.elements[mid].getPointer());
                newRoot.elements[0] = promoted;
                newRoot.isLeaf = false;
                this.rootPage = writeNewPage(raf, newRoot);
                raf.seek(0);
                raf.writeLong(this.rootPage);
            }

            raf.seek(originalFp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PageElement splitPage(RandomAccessFile raf) { // SHOULD RETURN THE PROMOTED ELEMENT
        PageElement promoted = null;
        try {
            long originalFp = raf.getFilePointer();

            BPlusTreePage page = getPage(raf);
            if (page.isLeaf) {
                int mid = page.numElements / 2;
                int newPageElements = page.numElements - mid; // elementos na pagina da direita copiando o elemento
                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);
                for (int i = 0; i < newPageElements; i++) {
                    rightPage.elements[i] = page.elements[mid + i];
                }
                page.setNumElements(mid);
                rightPage.setNumElements(newPageElements);
                rightPage.pointers[this.bOrder - 1] = page.pointers[this.bOrder - 1];
                page.pointers[this.bOrder - 1] = writeNewPage(raf, rightPage);
                overWritePage(raf, raf.getFilePointer(), page);
                promoted = new PageElement(page.elements[mid].getId(), page.pointers[this.bOrder - 1]);
            } else {
                int mid = page.numElements / 2;
                int newPageElements = page.numElements - mid - 1; // elementos na pagina da direita não copiando o elemento
                BPlusTreePage rightPage = new BPlusTreePage(this.bOrder);

                for (int i = 0; i < newPageElements; i++) {
                    rightPage.elements[i] = page.elements[mid + i + 1];
                    rightPage.pointers[i] = page.pointers[mid + i + 1];
                }
                rightPage.pointers[newPageElements] = page.pointers[mid + 1 + newPageElements];

                page.setNumElements(mid);
                rightPage.setNumElements(newPageElements);
                rightPage.isLeaf = false;
                page.pointers[this.bOrder - 1] = writeNewPage(raf, rightPage);
                overWritePage(raf, raf.getFilePointer(), page);
                promoted = new PageElement(page.elements[mid].getId(), page.elements[mid].getPointer());
            }

            raf.seek(originalFp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return promoted;
    }

    private long getPromotedRightPointer(RandomAccessFile raf) {
        long pointer = -1;
        try {
            BPlusTreePage page = getPage(raf);
            pointer = page.pointers[this.bOrder - 1];
            page.pointers[this.bOrder - 1] = -1;
            overWritePage(raf, raf.getFilePointer(), page);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pointer;
    }

    public long search(int id) {
        long result = -1;
        try (RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw")) {
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            boolean found = false;
            for (int i = 0; (i < page.numElements && !found); i++) {
                if (page.elements[i].getId() == id) {
                    if (page.isLeaf) {
                        result = page.elements[i].getPointer();
                        found = true;
                    } else {
                        raf.seek(page.elements[i].getPointer());
                        result = search(raf, id);
                    }
                }
            }
            if (!found) {
                raf.seek(whereToGo(page, id));
                result = search(raf, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private long search(RandomAccessFile raf, int id) {
        long result = -1;
        try {
            BPlusTreePage page = getPage(raf);
            boolean found = false;
            for (int i = 0; (i < page.numElements && !found); i++) {
                if (page.elements[i].getId() == id) {
                    if (page.isLeaf) {
                        result = page.elements[i].getPointer();
                        found = true;
                    } else {
                        raf.seek(page.elements[i].getPointer());
                        result = search(raf, id);
                    }
                }
            }
            if (!found) {
                raf.seek(whereToGo(page, id));
                result = search(raf, id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void printTree(RandomAccessFile raf) {
//        RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw")
        try {
//            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
//            raf.seek(page.pointers[4]);
//            BPlusTreePage page2 = getPage(raf);
//            raf.seek(page.pointers[1]);
//            BPlusTreePage page3 = getPage(raf);

            page.printPage();
//            System.out.println();
//            page2.printPage();
//            page3.printPage();
            System.out.println();
//            raf.seek(page2.pointers[0]);
//            getPage(raf).printPage();

//            raf.seek(page2.pointers[1]);
//            getPage(raf).printPage();
//            raf.seek(page2.pointers[2]);
//            getPage(raf).printPage();
//            raf.seek(page3.pointers[0]);
//            getPage(raf).printPage();
//            raf.seek(page3.pointers[1]);
//            getPage(raf).printPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteElement(int id) {

        try {
            RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw");
            long root = raf.readLong();
            raf.readInt(); //skip the Order;
            raf.seek(root);
            BPlusTreePage bPlusTreePage = getPage(raf);
            deleteElement(raf, id);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void deleteElement(RandomAccessFile raf, int id) {
        try {

            long leafPage = getLeafPage(raf, id);
            long upperPage = getUpperPage(raf, id);
            long firstAppearence = getFirstAppearence(raf, id, upperPage);
            BPlusTreePage page;
            int minElements = (int) Math.ceil((((double) this.bOrder / 2) - 1));
            if (firstAppearence == leafPage) {
                System.out.println("aaaa");
                raf.seek(firstAppearence);
                page = getPage(raf);
                if (page.numElements - 1 >= minElements) { // APENAS NA FOLHA E ELA NAO FICA COM TAMANHO REDUZIDO
                    boolean found = false;
                    int elementToShift = 0;
                    for (int i = 0; i < page.numElements && !found; i++) {
                        if (page.elements[i].getId() == id) {
                            elementToShift = i;
                            found = true;
                        }
                    }
//                    System.out.println("etf" + elementToShift);
                    leftShiftPage(raf, elementToShift);
                    System.out.println("REMOVIDO: " + id);
                    System.out.println(raf.getFilePointer());
                    printTree(raf);

                } else {
                    raf.seek(upperPage);
                    int wichBrother;
                    wichBrother = wichBrotherCanTake(raf, id);
                    System.out.println(wichBrother);
                    if (wichBrother == 1) {
                        takeFromRight(raf, leafPage);
                    }

                }

//                printTree(raf);
//            printTree(raf);
            }
//            raf.seek(upperPage);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void leftShiftPage(RandomAccessFile raf, int elementToShift) {
        try {
            BPlusTreePage page = getPage(raf);
            for (int i = elementToShift; i < page.numElements - 1; i++) {
                page.elements[i] = page.elements[i + 1];
            }
            page.numElements--;
            overWritePage(raf, raf.getFilePointer(), page);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void takeFromRight(RandomAccessFile raf, long leaf) {

    }

    private int wichBrotherCanTake(RandomAccessFile raf, int id) {
        int result = -1;
        //-1 FOR NONE
        // 0 FOR ONLY LEFT
        // 1 FOR RIGHT OR BOTH
        try {
            BPlusTreePage page = getPage(raf);
            boolean found = false;
            long leftBrother = -1;
            long rightBrother = -1;
            for (int i = 0; i < page.numElements && !found; i++) {
                if (page.elements[i].getId() > id) {
                    leftBrother = page.pointers[i];
                    rightBrother = page.pointers[i + 1];
                    found = true;
                }
            }
            if (leftBrother == -1 && rightBrother != -1) {
                result = 1;
            } else if (leftBrother != -1 && rightBrother == -1) {
                result = 0;
            } else if (leftBrother != -1) {//AND RIGHT BROTHER IS AVALIBLE
                result = 1;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    private long getFirstAppearence(RandomAccessFile raf, int id, long upperPage) {
        long result = -1;
        try {
            long originalFP = raf.getFilePointer();
            raf.seek(upperPage);
            BPlusTreePage page = getPage(raf);
            boolean found = false;
            result = page.pointers[page.numElements];
            for (int i = 0; i < page.numElements && !found; i++) {
                if (page.elements[i].getId() > id) {
                    result = page.pointers[i];
                    found = true;
                }
            }
            raf.seek(originalFP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private long getUpperPage(RandomAccessFile raf, int id) { // GET THE PAGE BEFORE THE FIRST OCCURENCE OF THE ID
        long filePos = -1;
        long initialPointer = 0;
        try {

            BPlusTreePage page = getPage(raf);
            boolean found = false;
            initialPointer = raf.getFilePointer();
            for (int i = 0; i < page.numElements && !found; i++) {
                if (page.elements[i].getId() == id) {
                    filePos = raf.getFilePointer();
                    found = true;

                }
            }
            if (!found) {
                boolean isNext;
//                raf.seek(whereToGo(page, id));
                filePos = raf.getFilePointer();
                raf.seek(whereToGo(page, id));
                isNext = checkPage(getPage(raf), id);
                if (!isNext) {
                    filePos = getUpperPage(raf, id);
                }
            }
            raf.seek(initialPointer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePos;
    }

    private boolean checkPage(BPlusTreePage bPlusTreePage, int id) {//check if the Id is in the page passed by parameter
        boolean found = false;
//        PageElement pageElement = bPlusTreePage.elements[0];
        for (int i = 0; i < bPlusTreePage.numElements && !found; i++) {
            if (bPlusTreePage.elements[i].getId() == id) {
                found = true;
            }
        }
        return found;
    }

    private long getLeafPage(RandomAccessFile raf, int id) {
        long result = -1;
        try {
            long originalFP = raf.getFilePointer();
            BPlusTreePage page = getPage(raf);
            boolean found = false;
            for (int i = 0; (i < page.numElements && !found); i++) {
                if (page.elements[i].getId() == id) {
                    if (page.isLeaf) {
                        result = raf.getFilePointer();
                        found = true;
                    } else {
                        raf.seek(page.elements[i].getPointer());
                        result = getLeafPage(raf, id);
                        found = true;
                    }
                }
            }
            if (!found) {
                raf.seek(whereToGo(page, id));
                result = getLeafPage(raf, id);
            }
            raf.seek(originalFP);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}
