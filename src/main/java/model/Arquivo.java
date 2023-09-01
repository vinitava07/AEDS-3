package model;

public class Arquivo {
    public String mainFile;
    public String auxFile;

    Arquivo() {
        this.auxFile = "";
        this.mainFile = "";
    }

    public Arquivo(String mainFile, String auxFile) {
        this.mainFile = mainFile;
        this.auxFile = auxFile;
    }

    public Arquivo(String mainFile) {
        this.mainFile = mainFile;
        this.auxFile = "";
    }
}
