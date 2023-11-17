package model;

public class Arquivo {
    public String binFile;
    public String csvFile;

    Arquivo() {
        this.csvFile = "";
        this.binFile = "";
    }

    public Arquivo(String bin, String csv) {
        this.binFile = bin;
        this.csvFile = csv;
    }

    public Arquivo(String mainFile) {
        this.binFile = mainFile;
        this.csvFile = "";
    }
}
