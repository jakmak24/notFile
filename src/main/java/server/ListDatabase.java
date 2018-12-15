package server;

import data.Attribute;
import data.MetaData;
import data.messages.LoginMessage;
import data.messages.SearchResponseTorrentMessage;
import data.messages.TorrentRecordMessage;

import java.util.ArrayList;
import java.util.List;


public class ListDatabase extends Database{

    List<TorrentRecordMessage> torrentTable = new ArrayList<>();

    @Override
    public String login(LoginMessage loginMessage) {
        return "OK";
    }


    public int addTorrent(TorrentRecordMessage torrentRecordMessage) {
        torrentTable.add(torrentRecordMessage);
        return torrentTable.size() - 1;
    }

    public SearchResponseTorrentMessage getAllTorrents() {
        List<MetaData> recordsMetadata = new ArrayList<>();
        List<Integer> recordsIndexes = new ArrayList<>();
        for (int i = 0; i < torrentTable.size(); i++) {
            recordsMetadata.add(torrentTable.get(i).getMetaData());
            recordsIndexes.add(i);
        }
        return new SearchResponseTorrentMessage(recordsMetadata,recordsIndexes);
    }

    public TorrentRecordMessage getTorrent(int id) {
        if (id < torrentTable.size()) {
            return torrentTable.get(id);
        }
        return null;
    }

    public SearchResponseTorrentMessage searchTorrents(List<Attribute> searchAttrs) {
        List<MetaData> recordsMetadata = new ArrayList<>();
        List<Integer> recordsIndexes = new ArrayList<>();
        for (int i = 0; i < torrentTable.size(); i++) {
            if (torrentTable.get(i).getMetaData().matchAttributes(searchAttrs)) {
                recordsMetadata.add(torrentTable.get(i).getMetaData());
                recordsIndexes.add(i);
            }
        }
        return new SearchResponseTorrentMessage(recordsMetadata,recordsIndexes);
    }
}
