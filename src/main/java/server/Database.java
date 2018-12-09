package server;

import data.Attribute;
import data.MetaData;
import data.TorrentRecordMessage;

import java.util.ArrayList;
import java.util.List;


public class Database {

    List<TorrentRecordMessage> torrentTable = new ArrayList<>();

    public void addTorrent(TorrentRecordMessage torrentRecordMessage) {
        torrentTable.add(torrentRecordMessage);
    }

    public List<MetaData> getAllTorrents() {
        List<MetaData> recordsMetadata = new ArrayList<>();
        for (TorrentRecordMessage a : torrentTable) {
            recordsMetadata.add(a.getMetaData());
        }
        return recordsMetadata;
    }

    public TorrentRecordMessage getTorrent(int id) {
        if (id < torrentTable.size()) {
            return torrentTable.get(id);
        }
        return null;
    }

    public List<MetaData> searchTorrents(List<Attribute> searchAttrs) {
        List<MetaData> recordsMetadata = new ArrayList<>();
        for (TorrentRecordMessage record : torrentTable) {
            if (matchAttributes(record.getMetaData(), searchAttrs)) {
                recordsMetadata.add(record.getMetaData());
            }
        }
        return recordsMetadata;
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
                default:
                    throw new IllegalArgumentException("Unsupported attribute name: " + attr.getName());
            }
        }
        return true;
    }
}
