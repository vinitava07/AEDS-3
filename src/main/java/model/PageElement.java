package model;

public class PageElement {
    private int id;
    private long pointer;

    public PageElement (int id , long pointer) {
        this.id = id;
        this.pointer = pointer;
    }

    public int getId() {
        return id;
    }

    public long getPointer() {
        return pointer;
    }
}
