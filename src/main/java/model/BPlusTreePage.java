package model;

public class BPlusTreePage {
    public int pageElements;
    public boolean isLeaf;
    public final int pageLength;
    public long[] pointers;
    public  PageElement[] elements;

    public BPlusTreePage(int bOrder) {
        this.pageElements = 0;
        this.isLeaf = true;
        this.pageLength = calcLength(bOrder);
        this.pointers = new long[bOrder];
        this.elements = new PageElement[bOrder - 1];
    }

    public void setPageElements(int pageElements) {
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
        int x = bOrder--;
        int elementsLength = (4 + 8) * x; // (int(4) + long(8)) * number of elements
        int pointersLength = 8 * (x + 1); // long(8) * ( number of elements + 1)
        return elementsLength + pointersLength + 4 + 1;// + 2 for the number of elements in the beginning + 1 boolean
    }

}
