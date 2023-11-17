package dao;

import model.Arquivo;
import model.DataBucket;
import model.DataRecords;
import model.PageElement;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.EmptyStackException;
import java.util.NoSuchElementException;

public class DynamicHashingDAO {
    private Arquivo file;

    public DynamicHashingDAO(String fileName , boolean deleteIfExists) {
        String bucketFileName = "../resources/bucket_" + fileName;
        String dirFileName = "../resources/dir_" + fileName;

        this.file = new Arquivo(bucketFileName , dirFileName);
        File file = new File(bucketFileName);
        File file1 = new File(dirFileName);
        if(file1.exists() || file.exists()) {
            if (deleteIfExists) {
                if (this.delete()) {
                    if(create()) System.out.println("Files recreated!");
                    else System.out.println("Failed to recreate Files!!");
                } else {
                    System.out.println("Failed to delete Files!!");
                }
            }
        } else {
            if(create()) System.out.println("Files successfully created!!");
            else System.out.println("Failed to create files!!");
        }
    }
    public boolean create() {
        boolean status;
        try {
            DataBucketDAO bucketDAO = new DataBucketDAO(new DataBucket(0));
            RandomAccessFile raf0 = new RandomAccessFile(this.file.binFile, "rw");
            bucketDAO.writeDataBucket(raf0);
            raf0.close();

            DataRecords dir = new DataRecords(bucketDAO.writtenAt);
            DataRecordsDAO dirDAO = new DataRecordsDAO(dir);
            RandomAccessFile raf1 = new RandomAccessFile(this.file.csvFile, "rw");
            dirDAO.writeDataRecord(raf1);
            raf1.close();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public boolean delete() {
        boolean status;
        try {
            File file1 = new File(this.file.csvFile);
            File file2 = new File(this.file.binFile);
            if(file1.exists()) file1.delete();
            if(file2.exists()) file2.delete();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public void insertElement(PageElement element) {
        try {
            File file = new File(this.file.csvFile);
            if(file.exists() == false) throw new Exception("The file: \"" + this.file.csvFile + "\" doesn't exist!!");
            else {
                DataRecordsDAO dir = new DataRecordsDAO();
                dir.readFromFile(this.file.csvFile);
                int hash = dir.dataRecords.hash(element.getId());
                long where = dir.dataRecords.getPointers()[hash];
                DataBucketDAO bucketDAO = new DataBucketDAO();
                bucketDAO.readFromFile(this.file.binFile, where);
                if(bucketDAO.bucket.search(element.getId()) != null) throw new RuntimeException("An element with the id:" + element.getId() + " already exists!");
                if (bucketDAO.bucket.getNumElements() == DataBucket.getMaxElements()) { // bucket is full!!
                    if (bucketDAO.bucket.getLocalDepth() == dir.dataRecords.getGlobalDepth()) { //increase the global depth
                        dir.dataRecords.increaseGlobalDepth();
                    }
                    bucketDAO.bucket.increaseLocalDepth();
                    DataBucket auxBucket = new DataBucket(bucketDAO.bucket.getLocalDepth());
                    int newBucketHashPos = dir.reAdjustBuckets(bucketDAO.bucket, auxBucket, hash);
                    RandomAccessFile raf = new RandomAccessFile(this.file.binFile, "rw");
                    bucketDAO.overWrite(raf);
                    raf.close();
                    //TODO : reuse bucketDAO object below
                    DataBucketDAO auxBucketDAO = new DataBucketDAO(auxBucket);
                    raf = new RandomAccessFile(this.file.binFile, "rw");
                    auxBucketDAO.writeDataBucket(raf);
                    raf.close();
                    dir.updateDataRecord(newBucketHashPos, auxBucketDAO.writtenAt);
                    raf = new RandomAccessFile(this.file.csvFile, "rw");
                    dir.writeDataRecord(raf);
                    raf.close();
                    insertElement(element);

                } else {
                    bucketDAO.bucket.insertElement(element);
                    RandomAccessFile raf = new RandomAccessFile(this.file.binFile, "rw");
                    bucketDAO.overWrite(raf);
                    raf.close();
                }

            }
        } catch (RuntimeException e) {
            System.out.println(e.getLocalizedMessage());
            System.out.println("Would you like to run an update instead?");
            //TODO: check if the user wants to update
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean removeElement(int id) { //TODO : exclude bucket if the bucket become empty -- optimize removal to decrease conflicts!!
        boolean status;
        try {
            if(id < 0) throw new NoSuchElementException("Invalid ID! There can not exists an negative ID!");
            DataBucketDAO bucketDAO = searchBucket(id);
            bucketDAO.bucket.removeElement(id);
            RandomAccessFile raf = new RandomAccessFile(this.file.binFile, "rw");
            bucketDAO.overWrite(raf);
            raf.close();
            System.err.println("Element successfully removed!");
            status = true;
        } catch (Exception e) {
            System.err.println("Failed to remove element!");
            e.printStackTrace();
            status = false;
        }

        return status;
    }
    public boolean updateElement(PageElement element) {
        boolean status;
        try {
            if(element.getId() < 0) throw new NoSuchElementException("Invalid ID! There can not exists an negative ID!");
            DataBucketDAO bucketDAO = searchBucket(element.getId());
            if(bucketDAO.bucket.updateElement(element) == false) throw new Exception("Element not found!!");
            RandomAccessFile raf = new RandomAccessFile(this.file.binFile, "rw");
            bucketDAO.overWrite(raf);
            raf.close();
            System.err.println("Element successfully updated!");
            status = true;
        } catch (EmptyStackException e) {
            System.err.println("Failed to update element!");
            status = false;
        }
        catch (Exception e) {
            System.err.println("Failed to update element!");
            e.printStackTrace();
            status = false;
        }
        return status;
    }

    public long search(int id) {
        PageElement result = new PageElement(-1 , -1);
        try {
            if(id < 0) throw new NoSuchElementException("Invalid ID! There can not exists an negative ID!");
            DataBucketDAO bucketDAO = searchBucket(id);
            result = bucketDAO.bucket.search(id);
            if(result == null) throw new NoSuchElementException("ID: " + id + " not found!");
//            System.out.println(result.getId() + " -- " + result.getPointer());
//            bucketDAO.bucket.print();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return result.getPointer();
    }

    private DataBucketDAO searchBucket(int id) throws Exception{
        DataRecordsDAO dir = new DataRecordsDAO();
        dir.readFromFile(this.file.csvFile);
        int hash = dir.dataRecords.hash(id);
        long where = dir.dataRecords.getPointers()[hash];
        DataBucketDAO bucketDAO = new DataBucketDAO();
        bucketDAO.readFromFile(this.file.binFile, where);
        if(bucketDAO.bucket.getElements()[0].getId() == -1) throw new InstantiationException("The current Hash is empty or haven't been build!!");
        return bucketDAO;
    }

    private static class DataRecordsDAO {
        private DataRecords dataRecords;
        private DataRecordsDAO(DataRecords dataRecords) {
            this.dataRecords = dataRecords;
        }
        private DataRecordsDAO(){
            this.dataRecords = new DataRecords(-1);
        }

        private void writeDataRecord(RandomAccessFile raf) {
            try {
                long originalFP = raf.getFilePointer();
                raf.writeShort(this.dataRecords.getGlobalDepth());
                long[] aux = this.dataRecords.getPointers();
                for (int i = 0; i < aux.length; i++) {
                    raf.writeLong(aux[i]);
                }
                raf.seek(originalFP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readFromFile(String filePath) {
            try (RandomAccessFile raf = new RandomAccessFile(filePath , "rw")){
                this.dataRecords.setGlobalDepth(raf.readShort());
                int dirLength = (int)(Math.pow(2 , this.dataRecords.getGlobalDepth()));
                long[] pointers = new long[dirLength];
                for (int i = 0; i < dirLength; i++) {
                    pointers[i] = raf.readLong();
                }
                this.dataRecords.setPointers(pointers);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private int reAdjustBuckets(DataBucket bucket1 , DataBucket bucket2 , int oldHash) {
            PageElement[] bucket1Elements = bucket1.getElements();
            int newHash = -1;
            for (int i = 0; i < bucket1.getNumElements(); i++) {
                if(this.dataRecords.hash(bucket1Elements[i].getId()) == oldHash) ;
                else {
                    newHash = this.dataRecords.hash(bucket1Elements[i].getId());
                    bucket2.insertElement(new PageElement(bucket1Elements[i].getId() , bucket1Elements[i].getPointer()));
                    try {
                        bucket1.removeElement(bucket1Elements[i].getId());
                    } catch (Exception e){e.printStackTrace();}
                }
            }
            return newHash;
        }
        private void updateDataRecord(int hash , long where) {
            long[] pointers = this.dataRecords.getPointers();
            pointers[hash] = where;
            this.dataRecords.setPointers(pointers);
        }
    }
    private static class DataBucketDAO {
        private DataBucket bucket;
        private long writtenAt;
        private DataBucketDAO(DataBucket bucket) {
            this.bucket = bucket;
            this.writtenAt = -1;
        }
        private DataBucketDAO() {
            this.bucket = new DataBucket(0);
            this.writtenAt = -1;
        }

        private void writeDataBucket(RandomAccessFile raf) {
            try {
                long originalFP = raf.getFilePointer();
                raf.seek(raf.length());
                this.writtenAt = raf.getFilePointer();
                raf.writeShort(this.bucket.getLocalDepth());
                raf.writeInt(this.bucket.getNumElements());
                PageElement[] elements = this.bucket.getElements();
                for (int i = 0; i < DataBucket.getMaxElements(); i++) {
                    raf.writeInt(elements[i].getId());
                    raf.writeLong(elements[i].getPointer());
                }
                raf.seek(originalFP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void overWrite(RandomAccessFile raf) {
            try {
                long originalFP = raf.getFilePointer();
                raf.seek(this.writtenAt);
                raf.writeShort(this.bucket.getLocalDepth());
                raf.writeInt(this.bucket.getNumElements());
                PageElement[] elements = this.bucket.getElements();
                for (int i = 0; i < DataBucket.getMaxElements(); i++) {
                    raf.writeInt(elements[i].getId());
                    raf.writeLong(elements[i].getPointer());
                }
                raf.seek(originalFP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void readFromFile(String filePath , long where) {
            try (RandomAccessFile raf = new RandomAccessFile(filePath , "rw")) {
                this.writtenAt = where;
                raf.seek(this.writtenAt);
                this.bucket.setLocalDepth(raf.readShort());
                this.bucket.setNumElements(raf.readInt());
                PageElement[] elements = new PageElement[DataBucket.getMaxElements()];
                for (int i = 0; i < DataBucket.getMaxElements(); i++) {
                    elements[i] = new PageElement(raf.readInt() , raf.readLong());
                }
                this.bucket.setElements(elements , this.bucket.getNumElements());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}