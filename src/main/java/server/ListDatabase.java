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


    public void addTorrent(TorrentRecordMessage torrentRecordMessage) {
        torrentTable.add(torrentRecordMessage);
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
            if (matchAttributes(torrentTable.get(i).getMetaData(), searchAttrs)) {
                recordsMetadata.add(torrentTable.get(i).getMetaData());
                recordsIndexes.add(i);
            }
        }
        return new SearchResponseTorrentMessage(recordsMetadata,recordsIndexes);
    }

    private boolean matchAttributes(MetaData record, List<Attribute> attributes) {
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "name":
                    if (!attr.match(record.getName())) return false;
                    break;
                case "owner":
                    if (!attr.match(record.getOwnerID())) return false;
                    break;
                case "x":
                    if (!attr.match(String.valueOf(record.getX()))) return false;
                    break;
                case "y":
                    if (!attr.match(String.valueOf(record.getY()))) return false;
                    break;
                case "filesize":
                    if (!attr.match(String.valueOf(record.getFileLength()))) return false;
                    break;
                case "public":
                    if (!attr.match(String.valueOf(record.isAccessPublic()))) return false;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported attribute name: " + attr.getName());
            }
        }
        return true;
    }


}
