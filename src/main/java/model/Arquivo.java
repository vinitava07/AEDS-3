package model;

public class Arquivo {
    public String binFile;
    public String csvFile;

    Arquivo() {
        this.csvFile = "";
        this.binFile = "";
    }

    public Arquivo(String mainFile, String auxFile) {
        this.binFile = mainFile;
        this.csvFile = auxFile;
    }

    public Arquivo(String mainFile) {
        this.binFile = mainFile;
        this.csvFile = "";
    }
}
