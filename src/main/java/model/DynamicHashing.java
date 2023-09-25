package model;

public class DynamicHashing {
    private DataRecords dir;
    private DataBucket bucket;

    public DynamicHashing(int numElements) {
        this.dir = new DataRecords(-1);
        this.bucket = new DataBucket(numElements);
    }

    public void setBucket(DataBucket bucket) {
        this.bucket = bucket;
    }

    public void setDir(DataRecords dir) {
        this.dir = dir;
    }

    public DataBucket getBucket() {
        return bucket;
    }

    public DataRecords getDir() {
        return dir;
    }
}
