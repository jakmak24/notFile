package server;

import data.TorrentRecordMessage;
import data.MetaData;

import java.util.ArrayList;


public class Database {

    ArrayList<TorrentRecordMessage> torrentTable = new ArrayList<TorrentRecordMessage>();

    public void addTorrent(TorrentRecordMessage torrentRecordMessage){
        torrentTable.add(torrentRecordMessage);
    }

    public ArrayList<MetaData> getAllTorrents (){
        ArrayList<MetaData> recordsMetadata = new ArrayList<MetaData>();
        for(TorrentRecordMessage a:torrentTable){
            recordsMetadata.add(a.getMetaData());
        }
        return recordsMetadata;
    }

    public TorrentRecordMessage getTorrent(int id){
        if(id<torrentTable.size()){
            return torrentTable.get(id);
        }
        return null;
    }

}
