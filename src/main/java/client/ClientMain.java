package client;

import java.util.Scanner;

public class ClientMain {

    public static void main(String[] args) {

        String userID = "Kuba";
        String groupID = "AGH_Cyfronet";

        if (args.length ==2) {
            userID = args[0];
            groupID = args[1];
        }

        Client client = new Client(new User(userID, groupID));
        client.openConnection();

        Scanner scan = new Scanner(System.in);
        while (true) {

            String s = scan.nextLine();
            if (s.startsWith("quit")) break;
            if (s.startsWith("search")){
                String query = s.substring("search ".length());
                client.searchTorrents(query);
            }

            if (s.startsWith("create")){
                System.out.println(client.createTorrent(s.split("\\s+")[1],s.split("\\s+")[2]));
            }

            if (s.startsWith("add")){
                String torrent = s.split("\\s+")[1];
                client.addTorrent(torrent);
            }
            if (s.startsWith("get")){
                String torrentID = s.split("\\s+")[1];
                client.getTorrent(torrentID);
            }

            if (s.startsWith("download ")){
                String torrentID = s.split("\\s+")[1];
                client.downloadTorrent(torrentID);
            }

            if (s.startsWith("seed ")){
                String torrentID = s.split("\\s+")[1];
                client.seedTorrent(torrentID);
            }
        }

        client.closeConnection();
    }

}
