package model;

public class DataRecords {
    private short globalDepth;
    private long[] pointers;
    public DataRecords(long pointer) {
        this.globalDepth = 0;
        pointers = new long[1]; // 2^0 = 1
        pointers[0] = pointer;
    }
    public DataRecords(short globalDepth) {
        this.globalDepth = globalDepth;
        pointers = new long[ (int)(Math.pow(2 , globalDepth)) ];
        for (int i = 0; i < pointers.length; i++) {
            pointers[0] = -1;
        }
    }
    public int hash(int key) {
        return (int) (key % (Math.pow(2 , globalDepth)));
    }

    public void increaseGlobalDepth() {
        long[] newPointers = new long[ (int)(Math.pow(2 , ++globalDepth)) ];
        for (int i = 0; i < this.pointers.length; i++) { //copy current bucket pointers
            newPointers[i] = this.pointers[i];
            newPointers[i + this.pointers.length] = this.pointers[i];
        }
        this.pointers = newPointers;
    }

    public short getGlobalDepth() {
        return globalDepth;
    }

    public long[] getPointers() {
        return pointers;
    }

    public void setGlobalDepth(short globalDepth) {
        this.globalDepth = globalDepth;
    }

    public void setPointers(long[] pointers) {
        this.pointers = pointers;
    }

    public long getFilePointerAt(int position) {
        return this.pointers[position];
    }

    public void printDir() {
        System.out.println(this.globalDepth);
        for (int i = 0; i < (int)(Math.pow(2 , this.globalDepth)); i++) {
            System.out.println(this.pointers[i]);
        }
    }
}
