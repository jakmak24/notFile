package pt.ul.fc.mt.notfile.client;

public class Config {
    public static final String RABBIT_MQ_HOST = "localhost";
    public static final String TORRENT_FOLDER = "./torrents";
    public static final String TRACKER_ANNOUNCE = "http://localhost:8000/announce";
    public static final String USERS_TOPIC = "users";
    public static final String GROUP_TOPIC = "groups";
    public static final String ALL_USERS_ROUTING_KEY = "all";
}
