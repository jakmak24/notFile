package server;

import data.Attribute;
import data.messages.SearchResponseTorrentMessage;
import data.messages.TorrentRecordMessage;

import java.util.List;

public abstract class Database {

    public abstract void addTorrent(TorrentRecordMessage torrentRecordMessage);
    public abstract TorrentRecordMessage getTorrent(int id);
    public abstract SearchResponseTorrentMessage searchTorrents(List<Attribute> searchAttrs);
    public abstract SearchResponseTorrentMessage getAllTorrents();

}
