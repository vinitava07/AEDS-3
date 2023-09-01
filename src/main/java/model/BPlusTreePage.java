package model;

public class BPlusTreePage {
    private short pageElements;
    private final int pageLength;
    private long[] pointers;
    private  PageElement[] elements;

    public BPlusTreePage(int bOrder) {
        this.pageElements = 0;
        this.pageLength = calcLength(bOrder);
        this.pointers = new long[bOrder + 1];
        elements = new PageElement[bOrder];
    }

    public short getPageElements() {
        return pageElements;
    }

    public int getPageLength() {
        return pageLength;
    }

    public long[] getPointers() {
        return pointers;
    }


    private static int calcLength(int x) {
        int elementsLength = (4 + 8) * x; // (int(4) + long(8)) * number of elements
        int pointersLength = 8 * (x + 1); // long(8) * ( number of elements + 1)
        return elementsLength + pointersLength + 2;// + 2 for the number of elements in the beginning
    }

}
