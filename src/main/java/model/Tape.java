package model;

public class Tape {
    public Record record;
    public int filePointer;
    public boolean canRead;

    public Tape(Record r, int fp, boolean cR) {
        this.record = r;
        this.filePointer = fp;
        this.canRead = cR;
    }

    public Tape(Record r) {
        this.record = r;
        filePointer = 0;
        canRead = true;
    }


    public Tape() {
        record = null;
        filePointer = 0;
        canRead = true;
    }
}
