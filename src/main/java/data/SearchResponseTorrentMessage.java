package data;

import java.io.Serializable;
import java.util.List;

public class SearchResponseTorrentMessage implements Serializable {

    private List<MetaData> records;

    public SearchResponseTorrentMessage() {}

    public SearchResponseTorrentMessage(List<MetaData> records) {
        this.records = records;
    }

    public List<MetaData> getRecords() {
        return records;
    }

    public void setRecords(List<MetaData> records) {
        this.records = records;
    }
}
