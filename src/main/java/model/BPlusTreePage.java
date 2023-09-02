package model;

public class BPlusTreePage {
    /*
        [LONG pointerToRoot: (8) + INT ordTree (4)]
        BOOL         INT               LONG               INT         LONG                LONG
        isLeaf(1)    pageElements(4)   leftPointer(8)     id(4)       elementPointer(8)   rightPointer(8)

    * */
    public int pageElements;
    public boolean isLeaf;
    public final int pageLength;
    public long[] pointers;
    public PageElement[] elements;

    public BPlusTreePage(int bOrder) {
        this.pageElements = 0;
        this.isLeaf = true;
        this.pageLength = calcLength(bOrder);
        this.pointers = new long[bOrder];
        this.elements = new PageElement[bOrder - 1];
        for (int i = 0; i < bOrder - 1; i++) {
            this.elements[i] = new PageElement(-1, -1);
        }

    }

    public void setPageElements(int pageElements) { // TODO: SET THE PAGE POINTERS TO -1
        for (int i = pageElements; i < this.pageElements; i++) {
            elements[i].setId(-1);
            elements[i].setPointer(-1);
        }
        this.pageElements = pageElements;
    }

    public int getPageElements() {
        return pageElements;
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

}
