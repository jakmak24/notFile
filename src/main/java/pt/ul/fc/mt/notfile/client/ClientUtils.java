package pt.ul.fc.mt.notfile.client;

import java.util.AbstractMap.SimpleEntry;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientUtils {
    private static final Map<NotFileAction, String> actions = Stream.of(
        new SimpleEntry<>(NotFileAction.FILE_DOWNLOAD, "FD"),
        new SimpleEntry<>(NotFileAction.USER_JOINED, "UJ"),
        new SimpleEntry<>(NotFileAction.MATCH_FOUND, "MF"),
        new SimpleEntry<>(NotFileAction.ACCESS_REQUEST, "AREQ"),
        new SimpleEntry<>(NotFileAction.ACCESS_RESPONSE, "ARES"))
        .collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));

    public static String getRoutingKey(String id, NotFileAction action) {
        return id + "." + actions.get(action);
    }
}
