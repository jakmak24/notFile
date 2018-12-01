package data;

import java.io.Serializable;

public class TorrentRecordMessage implements Serializable {

    private  MetaData metaData;
    private byte[] torrentFileData;

    public TorrentRecordMessage(){}

    public TorrentRecordMessage(MetaData metaData , byte[] torrentFileData){
        this.metaData=metaData;
        this.torrentFileData = torrentFileData;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }

    public byte[] getTorrentFileData() {
        return torrentFileData;
    }

    public void setTorrentFileData(byte[] torrentFileData) {
        this.torrentFileData = torrentFileData;
    }
}
