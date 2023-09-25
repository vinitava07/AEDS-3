package model;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Comparator;

public class BPlusTreePage {
    /*
        [LONG pointerToRoot: (8) + INT ordTree (4)]
        BOOL         INT               LONG               INT         LONG                LONG
        isLeaf(1)    pageElements(4)   leftPointer(8)     id(4)       elementPointer(8)   rightPointer(8)

    * */
    public int numElements;
    public boolean isLeaf;
    public final int pageLength;
    public long[] pointers;
    public PageElement[] elements;

    public int Order;

    public BPlusTreePage(int bOrder) {
        this.Order = bOrder;
        this.numElements = 0;
        this.isLeaf = true;
        this.pageLength = calcLength(bOrder);
        this.pointers = new long[bOrder];
        this.elements = new PageElement[bOrder - 1];
        for (int i = 0; i < bOrder - 1; i++) {
            this.elements[i] = new PageElement(-1, -1);
            this.pointers[i] = -1;
        }
        this.pointers[bOrder - 1] = -1;
    }

    public void setNumElements(int numElements) { // TODO: SET THE PAGE POINTERS TO -1
        this.numElements = numElements;
    }

    public int getNumElements() {
        return numElements;
    }

    public int getPageLength() {
        return pageLength;
    }

    public long[] getPointers() {
        return pointers;
    }


    private static int calcLength(int bOrder) {
        int x = bOrder - 1;
        int elementsLength = (4 + 8) * x; // (int(4) + long(8)) * number of elements
        int pointersLength = 8 * (bOrder); // long(8) * ( number of elements + 1)
        return elementsLength + pointersLength + 4 + 1;// + 4 for the number of elements in the beginning + 1 boolean
    }

    public void insertPromoted(PageElement promoted, long promotedRightPointer) {
        ArrayList<PageElement> elementsList = new ArrayList<>();
        for (int i = 0; i < this.numElements; i++) {
            elementsList.add(this.elements[i]);
        }
        elementsList.add(promoted);
        elementsList.sort(Comparator.comparingInt(PageElement::getId));
        int i = 0;
        boolean flag = false;
        while (!flag) {
            if (elementsList.get(i).getId() == promoted.getId()) flag = true;
            else i++;
        }
        for (int j = this.numElements; j > i; j--) { // moves everything 1 spot to the right
            this.pointers[j + 1] = this.pointers[j];
            this.elements[j] = this.elements[j - 1];
        }
        this.elements[i] = promoted;
        this.pointers[i + 1] = promotedRightPointer;
        this.numElements++;
    }

    public void printPage() {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("leaf: " + this.isLeaf);
        strBuilder.append(" || elements: " + this.numElements);
        strBuilder.append(" || " + this.pointers[0]);
        for (int i = 0; i < this.numElements; i++) {
            strBuilder.append(" | " + this.elements[i].getId() + " - " + this.elements[i].getPointer());
            strBuilder.append(" | " + this.pointers[i + 1]);
        }
        strBuilder.append(" |");
        strBuilder.append(" Last page pointer: " + this.pointers[Order-1]);
        System.out.println(strBuilder.toString());
    }
}
