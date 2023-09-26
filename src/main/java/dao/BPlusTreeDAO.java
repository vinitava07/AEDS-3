package dao;

import model.Arquivo;
import model.BPlusTreePage;
import model.PageElement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;

/*
        [LONG pointerToRoot: (8) + INT ordTree (4)]
        BOOL         INT               LONG               INT         LONG                LONG
        isLeaf(1)    pageElements(4)   leftPointer(8)     id(4)       elementPointer(8)   rightPointer(8)

    * */
public class BPlusTreeDAO {
    public Arquivo indexFile;
    public int bOrder;
    public long rootPage;
    int minElements;

    public enum operation {
        cantRemove, cantShift, cantConcede, canRemove, canShift, canConcede, success;

    }

    enum wichBrother {
        left, right, none;
    }

    public BPlusTreeDAO(String fileName) {
//        this.minElements = (int) Math.ceil((((double) bOrder / 2) - 1));
//        System.out.println("min:" +bOrder);
        String binFileName = "../resources/indexB_" + fileName;
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

    public BPlusTreeDAO(String fileName, int bOrder) {
        String indexFileName = "../resources/indexB_" + fileName;
        try {
            if (bOrder < 3) throw new Exception("Não é possível uma árvore B de ordem menor que 3!");
            File file = new File(indexFileName);
            if (file.exists()) {
                file.delete();
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
            insertElement(raf, element, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertElement(RandomAccessFile raf, PageElement element, long nextPagePointer) {
        try {
            BPlusTreePage page = getPage(raf);
            if (page.isLeaf) {
                long originalFP = raf.getFilePointer();
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
                overWritePage(raf, originalFP, page);
            } else {
                long currentPage = raf.getFilePointer();
                long nextPage = -1;
                raf.seek(whereToGo(page, element.getId()));
                if (getPage(raf).numElements == (this.bOrder - 1)) {
                    // TODO : ANOTAÇÃO NA FOLHA
                    PageElement promoted = splitPage(raf);
//                    BPlusTreePage aux = getPage(raf);
                    page.insertPromoted(promoted, getPromotedRightPointer(raf));
                    overWritePage(raf, currentPage, page);
//                    overWritePage(raf, raf.getFilePointer(), aux);
                    raf.seek(whereToGo(page, element.getId()));
                    nextPage = raf.getFilePointer();
                }
                insertElement(raf, element, nextPage);
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
//                page.pointers[this.bOrder - 1] = 34123123;
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
            if(page.isLeaf == false) page.pointers[this.bOrder - 1] = -1;
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
                long whereToGo = whereToGo(page, id);
                if(whereToGo== -1) throw new NoSuchElementException("Anime not found!");
                raf.seek(whereToGo);
                result = search(raf, id);
            }
        } catch (NoSuchElementException e) {
            System.err.println(e.getLocalizedMessage());
        }
        catch (Exception e) {
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

    public void printAllPages() {
        try (RandomAccessFile raf = new RandomAccessFile(this.indexFile.mainFile, "rw")) {
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            printAllPages(raf, page, this.rootPage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printAllPages(RandomAccessFile raf, BPlusTreePage page, long returnPage) {
        try {
            int i = 0;
            BPlusTreePage aux;
            page.printPage();
            if (page.isLeaf) {
                raf.seek(returnPage);
                return;
            }
            long pos = raf.getFilePointer();
            for (int j = 0; j < page.numElements + 1; j++) {
                if (page.pointers[j] != -1) {
                    System.out.println();
                    raf.seek(page.pointers[j]);
                    System.out.println("POS: " + raf.getFilePointer());
                    aux = getPage(raf);
                    raf.seek(pos);
                    printAllPages(raf, aux, pos);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int deleteElement(RandomAccessFile raf, int id) {
        try {
            this.minElements = (int) Math.ceil((((double) bOrder / 2) - 1));

//            boolean onlyLeaf = false;
//            long firstAppearence = getFirstAppearence(raf, id);
//            raf.seek(firstAppearence);
//            BPlusTreePage page = getPage(raf);
//            raf.seek(this.rootPage);
//            onlyLeaf = page.isLeaf;
//            printTree(raf);
//            System.out.println(firstAppearence);
            deleteRecursion(raf, id, false);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    private operation deleteRecursion(RandomAccessFile raf, int id, boolean onlyLeaf) {

        try {
            BPlusTreePage currentPage = getPage(raf);
            long currentPageFP = raf.getFilePointer();
            boolean found = false;
            boolean nextPage = true;
            long pagePointer;
            operation op;
            long nextPagePointer = currentPage.pointers[currentPage.numElements];
            int elementPosition = 0;
            boolean loop = true;

            for (int i = 0; i < currentPage.numElements && loop; i++) {
                if (currentPage.elements[i].getId() == id) {
                    if (currentPage.isLeaf) {
                        found = true;
                        loop = false;
                    } else {
                        nextPagePointer = currentPage.pointers[i + 1];
                        loop = false;
                    }
                    elementPosition = i;
                } else if (currentPage.elements[i].getId() > id) {
                    if (currentPage.isLeaf) {
                        found = false;
                        nextPage = false;
                        loop = false;
                    } else {
                        nextPagePointer = currentPage.pointers[i];
                        loop = false;
                    }

                }
            }
            if (found) {
                System.out.println("achou FOUND = TRUE");
                if (currentPage.isLeaf) {
                    System.out.println("ACHOU NA FOLHA");
                    if (currentPage.numElements > minElements) {
                        System.out.println("A FOLHA PODE REMOVER");
                        changeFirstOcurrence(raf, id);
                        leftShiftPage(raf, elementPosition, false);
                        return operation.success;
                    } else {
                        System.out.println("A FOLHA NAO PODE REMOVER");
                        return operation.cantRemove;

                    }
                }
            } else if (nextPage) {
                System.out.println("NEXT PAGE");
                raf.seek(nextPagePointer);
                op = deleteRecursion(raf, id, onlyLeaf);
                raf.seek(currentPageFP);
                System.out.println("SAIU DA RECURSIVIDADE");
//                System.out.println("root: " + this.rootPage);
                System.out.println("current: " + raf.getFilePointer());
//                if ((currentPageFP != this.rootPage)) {
                if (op == operation.cantRemove) {
                    System.out.println("NAO PODE REMOVER, O QUE FAZER");
                    wichBrother whichConcede;
                    whichConcede = whichBrotherCanTake(raf, id);
                    if (whichConcede == wichBrother.left) {
                        System.out.println("TAKE FROM LEFT");
                        takeFromLeft(raf, id);
                        return operation.success;
                    } else if (whichConcede == wichBrother.right) {
                        System.out.println("TAKE FROM RIGHT");
                        takeFromRight(raf, id);
                        return operation.success;
                    } else {
                        System.out.println("NÃO PODE PEGAR EMPRESTADO, FAZER MERGE");
                        return operation.cantConcede;
                        //FUDEU TEM QUE FAZER MERGE
                    }
                }
                else if (op == operation.cantConcede && currentPageFP == this.rootPage) {
                    // FUDEU O MAXIMO POSSIVEL
                }
                else if (op == operation.cantConcede) {
                    //VERIFICAR SHIFHT
                    if (currentPage.numElements > minElements) {
                        //shift e merge
                    }
                }

//                }
            } else {
                System.out.println("não encontrado");
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        return operation.success;
    }

    public void takeFromRight(RandomAccessFile raf, int id) {// pega um elemento d afolha da direita e joga pra a esquerda
        try {
            long originalFP = raf.getFilePointer();
            BPlusTreePage fatherPage = getPage(raf);
            BPlusTreePage rightBrother;
            BPlusTreePage elementPage;
            int pos = 0;
            long elementPagePointer = -1;
            long rightBrotherPointer = -1;
            boolean found = false;
            long left;
            for (int i = 0; i < fatherPage.numElements && !found; i++) {
                if (fatherPage.elements[i].getId() > id) {
                    elementPagePointer = fatherPage.pointers[i];
                    pos = i;
                    found = true;
                }
            }
            raf.seek(elementPagePointer); // PEGA O IRMAO DA DIREITA
            elementPage = getPage(raf);
            rightBrotherPointer = elementPage.pointers[bOrder - 1];
            raf.seek(rightBrotherPointer);
            rightBrother = getPage(raf);

            PageElement rightElement = rightBrother.elements[0];// PEGA O 1 ELEMENTO E O 2
            PageElement secondRightElement = rightBrother.elements[1];

            changeFirstOcurrence(raf, rightElement.getId()); //TROCA AS APARICOES DO 1 ELEMENTO

            fatherPage.elements[pos] = secondRightElement;
            fatherPage.elements[pos].setPointer(rightBrotherPointer);
            overWritePage(raf, originalFP, fatherPage);// CONFIGURA A PAGINA DE PAI DA MANEIRA CERTA

            int positionOnElementPage = 0;
            for (int i = 0; i < elementPage.numElements; i++) {
                if (elementPage.elements[i].getId() == id) {
                    positionOnElementPage = i;
                    break;
                }
            }
            raf.seek(elementPagePointer);//DA UM SHIFT NA PAGINA QUE FOI REMOVIDO E REESCREVE ELA CERTO
            changeFirstOcurrence(raf, id);
            leftShiftPage(raf, positionOnElementPage, false);
            elementPage = getPage(raf);
            elementPage.elements[elementPage.numElements] = rightElement;
            elementPage.numElements++;
            overWritePage(raf, elementPagePointer, elementPage);

//            raf.seek(this.rootPage);
//            long firstApp = getFirstAppearence(raf, rightBrother.elements[0].getId());
//            raf.seek(firstApp);
//            BPlusTreePage firsAppPage = getPage(raf);
//            for (int i = 0; i < firsAppPage.numElements; i++) {
//                if (firsAppPage.elements[i].getId() == rightElement.getId()) {
//                    firsAppPage.elements[i].setId(secondRightElement.getId());
//                    firsAppPage.elements[i].setPointer(rightBrotherPointer);
//                    overWritePage(raf, firstApp, firsAppPage);
//                }
//            }
            raf.seek(rightBrotherPointer);
            leftShiftPage(raf, 0, false);// DA UM SHIFT NA PAGINA QUE EMPRESTOU
            raf.seek(originalFP);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void takeFromLeft(RandomAccessFile raf, int id) {
        try {
            long originalFP = raf.getFilePointer();
            BPlusTreePage fatherPage = getPage(raf);
            BPlusTreePage leftBrother;
            BPlusTreePage focusPage;
            int pos = 0;
            long leftBrotherPointer = -1;
            boolean found = false;

            for (int i = 0; i < fatherPage.numElements && !found; i++) {
                if (fatherPage.elements[i].getId() > id) {
                    pos = i;
                    found = true;
                }
            }
            if (pos == 0) {// se a pagina estiver entre uma divisao de paginas superior
                raf.seek(getLeftBrother(raf, 0));
                leftBrother = getPage(raf);
            } else {
                leftBrotherPointer = fatherPage.pointers[pos - 1];
                raf.seek(leftBrotherPointer);
                leftBrother = getPage(raf);
            }
            int leftBrotherID = leftBrother.elements[leftBrother.numElements - 1].getId();
            raf.seek(leftBrother.pointers[bOrder]);
//            focusPage = getPage(raf);
            raf.seek(this.rootPage);
//            long firstAppearence = getFirstAppearence(raf, leftBrotherID);
//            raf.seek(firstAppearence);
//            BPlusTreePage firstAppearencePage = getPage(raf);
//            if (firstAppearencePage.isLeaf) {
            fatherPage.elements[fatherPage.numElements - 1].setId(leftBrotherID);
            fatherPage.elements[fatherPage.numElements - 1].setPointer(leftBrother.pointers[bOrder]);
            overWritePage(raf, originalFP, fatherPage);
            raf.seek(leftBrother.pointers[bOrder]);
            rightShiftPage(raf, leftBrother.elements[leftBrother.numElements - 1], leftBrother.pointers[leftBrother.numElements]);
//            }
            leftBrother.numElements--;
            overWritePage(raf, leftBrotherPointer, leftBrother);

            raf.seek(originalFP);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    private void rightShiftPage(RandomAccessFile raf, PageElement element, long pointer) {
        try {
            BPlusTreePage pageToShift = getPage(raf);
            for (int i = 0; i < pageToShift.numElements; i++) {
                pageToShift.elements[i + 1] = pageToShift.elements[i];
                pageToShift.pointers[i + 1] = pageToShift.pointers[i];
            }
            pageToShift.pointers[0] = pointer;
            pageToShift.elements[0] = element;
            pageToShift.numElements++;
            overWritePage(raf, raf.getFilePointer(), pageToShift);

        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    private void changeFirstOcurrence(RandomAccessFile raf, int id) {// change first occurences of a element for his next in the leave
        try {
            long originalFp = raf.getFilePointer();
            BPlusTreePage currentPage = getPage(raf);
            PageElement element = new PageElement(-1, -1);
            raf.seek(rootPage);
            long firstApp = getFirstAppearence(raf, id);
            raf.seek(firstApp);
            BPlusTreePage firstAppPage = getPage(raf);
            if (firstAppPage.isLeaf) {
                return;
            }
            for (int i = 0; i < currentPage.numElements; i++) {
                if (currentPage.elements[i].getId() == id) {
                    element = currentPage.elements[i + 1];
                }
            }
            for (int i = 0; i < firstAppPage.numElements; i++) {
                if (firstAppPage.elements[i].getId() == id) {
                    firstAppPage.elements[i] = element;
                    firstAppPage.elements[i].setPointer(originalFp);
                }
            }
            overWritePage(raf, firstApp, firstAppPage);


            raf.seek(originalFp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void leftShiftPage(RandomAccessFile raf, int elementToShift, boolean moveLast) {
        try {
            BPlusTreePage page = getPage(raf);

            for (int i = elementToShift; i < page.numElements - 1; i++) {
                page.elements[i] = page.elements[i + 1];
                page.pointers[i] = page.pointers[i + 1];
            }
            if (moveLast) {
                page.pointers[page.numElements - 1] = page.pointers[page.numElements];
            }
            page.numElements--;
            overWritePage(raf, raf.getFilePointer(), page);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void takeFromRight(RandomAccessFile raf, long leaf) {

    }

    private wichBrother whichBrotherCanTake(RandomAccessFile raf, int id) {
        wichBrother result = wichBrother.none;
        //-1 FOR NONE
        // 0 FOR ONLY LEFT
        // 1 FOR RIGHT OR BOTH
        try {
            BPlusTreePage page = getPage(raf);
            long originalFP = raf.getFilePointer();
            boolean found = false;
            long leftBrotherPointer = -1;
            long rightBrotherPointer = -1;
            BPlusTreePage leftBrother;
            BPlusTreePage rightBrother = new BPlusTreePage(this.bOrder);
            int pos = 0;
            for (int i = 0; i < page.numElements && !found; i++) {
                if (page.elements[i].getId() > id) {
                    leftBrotherPointer = page.pointers[i];
                    pos = i;
                    found = true;
                }
            }
            raf.seek(leftBrotherPointer);
            leftBrother = getPage(raf);
//            raf.seek(leftBrother.pointers[bOrder - 1]);
            BPlusTreePage focusPage = getPage(raf);
            boolean haveRight;
            haveRight = haveRightBrohter(raf, focusPage.elements[focusPage.numElements - 1].getId());
            if (haveRight) {
                rightBrotherPointer = focusPage.pointers[bOrder - 1];
                raf.seek(rightBrotherPointer);
                rightBrother = getPage(raf);
            }
            long haveLeft = -1;
            int leftId = leftBrother.elements[0].getId();
            if (pos == 0) {
                int leftNumber = leftBrother.elements[0].getId();
                haveLeft = getLeftBrother(raf, leftNumber);
                raf.seek(haveLeft);
                leftBrother = getPage(raf);
                if (leftBrother.elements[0].getId() == leftId) {
                    haveLeft = -1;
                }

            }
            if (leftBrother.numElements > minElements && haveLeft != -1) {
                result = wichBrother.left;
            } else if (haveRight && rightBrother.numElements > minElements) {
                result = wichBrother.right;
            } else {
                result = wichBrother.none;// apenas para deixar mais claro para o programador
            }
            raf.seek(originalFP);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    public boolean haveRightBrohter(RandomAccessFile raf, int id) {
        boolean result = true;
        try {
            long originalFP = raf.getFilePointer();
//            raf.seek(rootPage);
            BPlusTreePage page = getPage(raf);
//            while (!page.isLeaf) {
//                raf.seek(page.pointers[page.numElements]);
//                page = getPage(raf);
//
//            }
            if (page.pointers[bOrder - 1] == -1) {
                result = false;
            }
            raf.seek(originalFP);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public long getLeftBrother(RandomAccessFile raf, int id) {
        long result = -1;
        try {
            long originalFP = raf.getFilePointer();
//            long upperPagePointer = getFirstAppearence(raf, id);
            raf.seek(rootPage);
            BPlusTreePage page = getPage(raf);
            raf.seek(page.pointers[0]);
            for (int i = 0; i < page.numElements; i++) {
                if (page.elements[i].getId() == id && i != 0) {
                    raf.seek(page.pointers[i - 1]);
                } else if (page.elements[i].getId() > id && i != 0) {
                    raf.seek(page.pointers[i - 1]);
                }
            }
            System.out.println(raf.getFilePointer());
            page = getPage(raf);
            while (!page.isLeaf) {
                raf.seek(page.pointers[page.numElements]);
                page = getPage(raf);
            }
            result = raf.getFilePointer();
            raf.seek(originalFP);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }

    private long getFirstAppearence(RandomAccessFile raf, int id) {
        long result = -1;
        try {
            long originalFP = raf.getFilePointer();
            raf.seek(this.rootPage);
            BPlusTreePage page = getPage(raf);
            boolean loop = true;
            boolean loopFor = true;
            long next = 0;
            while (loop) {
                next = page.pointers[page.numElements];
                for (int i = 0; i < page.numElements && loopFor; i++) {
                    if (page.elements[i].getId() == id) {
                        loop = false;
                        loopFor = false;
                        result = raf.getFilePointer();
                    } else if (page.elements[i].getId() > id) {
                        next = page.pointers[i];
                        loopFor = false;
                    }
                }
                loopFor = true;
                if (next != -1) {
                    raf.seek(next);
                    page = getPage(raf);
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
