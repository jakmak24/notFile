package data;

import java.io.Serializable;
import java.util.ArrayList;

public class SearchResponseTorrentMessage implements Serializable{

    private ArrayList<MetaData> records;

    public SearchResponseTorrentMessage(){}

    public SearchResponseTorrentMessage(ArrayList<MetaData> records){
        this.records = records;
    }

    public ArrayList<MetaData> getRecords() {
        return records;
    }

    public void setRecords(ArrayList<MetaData> records) {
        this.records = records;
    }
}
