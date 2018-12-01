package pt.ul.fc.mt.notfile.client;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String userID = "Kuba";
        String groupID = "AGH_Cyfronet";

        if (args.length == 2) {
            userID = args[0];
            groupID = args[1];
        }

        Client client = new Client(userID, groupID);
        client.openConnection();


        Scanner scan = new Scanner(System.in);
        while (true) {

            String s = scan.nextLine();
            if (s.equals("quit")) break;
            if (s.startsWith("search ")) {
                String query = s.substring("search ".length());
                client.searchTorrents(query);
            }
            if (s.startsWith("add ")) {
                String torrent = s.substring("add ".length());
                client.addTorrent(torrent);
            }
            if (s.startsWith("get ")) {
                String torrentID = s.substring("get ".length());
                client.getTorrent(torrentID);
            }
        }

        client.closeConnection();
    }

}
