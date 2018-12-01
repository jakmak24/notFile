package data;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchTorrentMessage implements Serializable{

    private ArrayList<MetaData> records;

    public SearchTorrentMessage(){}

    public SearchTorrentMessage(ArrayList<MetaData> records){
        this.records = records;
    }

    public ArrayList<MetaData> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<MetaData> records) {
        this.records = records;
    }
}
