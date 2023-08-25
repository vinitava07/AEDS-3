package dao;

import model.Arquivo;
import model.Record;

public class RecordDAO {
    Record record;
    Arquivo arquivo;

    public RecordDAO() {
        record = null;
        arquivo = null;
    }
    
    public RecordDAO(Record record , Arquivo arquivo) {
        this.record = record;
        this.arquivo = arquivo;
    }
}
