package model;

public class Arquivo {
    public String nameCsv;
    public String nameBin;

    Arquivo() {
        this.nameCsv = "";
        this.nameBin = "";
    }

    public Arquivo(String csvName, String binName) {
        this.nameCsv = csvName;
        this.nameBin = binName;
    }

}
