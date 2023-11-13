package model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

public class DataBucket {
    private static final int maxElements = 50; //(int) (Math.ceil((0.05 * 18495))); // 5% of 18.495 -> 925
    private short localDepth;
    private int numElements;
    private PageElement[] elements;

    public DataBucket(int numElements) {
        this.localDepth = 0;
        this.numElements = numElements;
        this.elements = new PageElement[maxElements];
        for (int i = 0; i < maxElements; i++) {
            elements[i] = new PageElement(-1 , -1);
        }
    }
    public DataBucket(short localDepth) {
        this.localDepth = localDepth;
        this.numElements = 0;
        this.elements = new PageElement[maxElements];
        for (int i = 0; i < maxElements; i++) {
            elements[i] = new PageElement(-1 , -1);
        }
    }
    public short getLocalDepth() {
        return localDepth;
    }

    public PageElement[] getElements() {
        return elements;
    }

    public int getNumElements() {
        return numElements;
    }

    public void setLocalDepth(short localDepth) {
        this.localDepth = localDepth;
    }

    public void setElements(PageElement[] elements , int numElements) {
        for (int i = 0; i < elements.length; i++) {
            this.elements[i] = elements[i];
        }
        this.numElements = numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public static int getMaxElements() {
        return maxElements;
    }

    private void orderBucket() {
        ArrayList<PageElement> array = new ArrayList<>();
        for (int i = 0; i < maxElements; i++) {
            array.add(elements[i]);
            this.elements[i] = new PageElement(-1 , -1);
        }
        array.sort(Comparator.comparingInt(PageElement::getId));
        int i = (DataBucket.getMaxElements() - 1);
        int j = 0;
        while((i >= 0) && (array.get(i).getId() != -1)) {
            this.elements[j++] = array.get(i--);
        }
    }

    public void insertElement(PageElement element) {
        this.elements[maxElements - 1] = element;
        this.numElements++;
        orderBucket();
    }

    public boolean removeElement(int id) throws Exception{
        boolean status = false;
        try {
            int where = binarySearch(id);
            this.elements[where].setId(-1);
            this.elements[where].setPointer(-1);
            this.numElements--;
            orderBucket();
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean updateElement(PageElement element) {
        boolean status = false;
        try {
            if(element.getPointer() < 0) throw new InterruptedException(("Can not set file pointer to: " + element.getPointer() + " !!"));
            int where = binarySearch(element.getId());
            this.elements[where].setId(element.getId());
            this.elements[where].setPointer(element.getPointer());
            status = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status;
    }

    private int binarySearch(int id) {
        int where = -1;
        int left = 0;
        int right = numElements - 1;
        boolean found = false;
        while(!found && (left <= right)) {
            int half = ((left + right) / 2);
            if (id == elements[half].getId()) {
                where = half;
                found = true;
            } else {
                if (id > elements[half].getId()) {
                    right = half - 1;
                }
                else {
                    left = half + 1;
                }
            }
        }
        return where;
    }

    public PageElement search(int id) {
        int where = binarySearch(id);
        PageElement result = null;
        if(where != -1) result = new PageElement(elements[where].getId() , elements[where].getPointer());
        return result;
    }

    public void increaseLocalDepth() {
        this.localDepth++;
    }

    public void print() {
        System.out.print("local depth: " + this.getLocalDepth() + " | numElements: " + this.getNumElements() + "\t\t");
        for (int i = numElements - 1; i >= 0; i--) {
            System.out.print(elements[i].getId() + " -- " + elements[i].getPointer() + "||");
        }
        System.out.println();
    }
}
