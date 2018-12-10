package data.messages;

import data.MetaData;

import java.io.Serializable;
import java.util.List;

public class SearchResponseTorrentMessage implements Serializable {

    private List<MetaData> recordsMetadata;
    private List<Integer> recordsIndexes;

    public SearchResponseTorrentMessage() {}

    public SearchResponseTorrentMessage(List<MetaData> recordsMetadata, List<Integer> recordsIndexes) {
        this.recordsMetadata = recordsMetadata;
        this.recordsIndexes = recordsIndexes;
    }


    public List<MetaData> getRecordsMetadata() {
        return recordsMetadata;
    }

    public void setRecordsMetadata(List<MetaData> recordsMetadata) {
        this.recordsMetadata = recordsMetadata;
    }

    public List<Integer> getRecordsIndexes() {
        return recordsIndexes;
    }

    public void setRecordsIndexes(List<Integer> recordsIndexes) {
        this.recordsIndexes = recordsIndexes;
    }

}
