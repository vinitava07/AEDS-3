package model;

import java.util.ArrayList;

public class ListaInvertida {

    int qtdPointers;
    String element;
    boolean graveyard;
    ArrayList<Long> pointers;

    public ListaInvertida() {
        this.element = null;
        this.pointers = new ArrayList<>();
        this.qtdPointers = 0;
        this.graveyard = false;
    }

    public ListaInvertida(String element, long pointer) {
        this.element = element;
        this.pointers = new ArrayList<>();
        this.pointers.add(pointer);
        this.qtdPointers = 1;
        this.graveyard = false;
    }

    public void setElement(String element) {
        this.element = element;
    }

    public void setPointers(ArrayList<Long> pointers) {
        this.pointers = pointers;
        qtdPointers = pointers.size();
    }

    public void addPointer(int index, long pointer) {
        this.pointers.set(index, pointer);
        this.qtdPointers++;
    }

    public int getQtdPointers() {
        return qtdPointers;
    }

    public void setQtdPointers(int qtdPointers) {
        this.qtdPointers = qtdPointers;
    }

    public boolean isGraveyard() {
        return graveyard;
    }

    public void setGraveyard(boolean graveyard) {
        this.graveyard = graveyard;
    }

    public ArrayList<Long> getPointers() {
        return pointers;
    }

    public String getElement() {
        return element;
    }
}
