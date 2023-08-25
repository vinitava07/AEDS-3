package model;

public class Record {

    int id;
    int size;
    boolean gravyard;
    Anime anime;

    public Record() {
        id = 0;
        size = 0;
        gravyard = false;
        anime = null;
    }

    Record(int newId, int newSize, boolean newGraveyard, Anime a) {
        id = newId;
        size = newSize;
        gravyard = newGraveyard;
        anime = a;
    }

    public void setId(int nId) {
        id = nId;
    }

    public void setSize(int nSize) {
        size = nSize;
    }

    public void setGraveyard(boolean nGraveyard) {
        gravyard = nGraveyard;
    }

    public void setAnime(Anime a) {
        anime = a;
    }

    public int getId() {
        return id;
    }

    public int getSize() {
        return size;
    }

    public boolean getGraveyard() {
        return gravyard;
    }

    public Anime getAnime() {
        return anime;
    }

}
