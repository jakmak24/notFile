package client;

import data.Attribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ClientMain {

    public static void main(String[] args) {

        String userID;
        String groupID;

        Client client = new Client();
        client.openConnection();

        Scanner scan = new Scanner(System.in);
        if (args.length ==2) {
            userID = args[0];
            groupID = args[1];
        } else {
            System.out.println("Insert username:");
            userID = scan.nextLine();
            System.out.println("Insert group name:");
            groupID = scan.nextLine();
        }

        if(client.login(userID, groupID,"pass")){

            System.out.println("COMMANDLINE:");
            printHelp();
            while (true) {
                String line = scan.nextLine();
                String[] s = line.split("\\s+");
                String command = s[0];

                if (command.equals("quit")) {
                    break;
                }

                switch (command) {
                    case "search":
                        // attributes in the form:
                        // <name> <relation> <value>,<name> <relation> <value>,...
                        // TODO: check format.
                        String attributeString = line.substring("search".length()).trim();
                        if(attributeString.equals("")){
                            client.searchTorrents(new ArrayList<>());
                        } else {
                            String[] attrs = attributeString.split(",");
                            List<Attribute> attributes = Arrays.stream(attrs)
                                    .map(Attribute::parse)
                                    .collect(Collectors.toList());
                            client.searchTorrents(attributes);
                        }
                        break;
                    case "create":
                        // <torrent_file_name> <data_file_path>
                        System.out.println(client.createTorrent(s[1], s[2]));
                        break;
                    case "accept":
                        // <user> <file_id>
                        client.acceptAccessRequest(s[2], s[3]);
                        break;
                    case "reject":
                        // <user> <file_id>
                        client.rejectAccessRequest(s[2], s[3]);
                        break;
                    case "add":
                        // <file_name> <attributes>
                        // attributes in the form:
                        // <attr1_name> = <attr1_value>,<attr2_name> = <attr2_value>,...
                        String[] attrs = line.substring("add ".length() + s[1].length() + 1).split(",");
                        List<Attribute> attributes = Arrays.stream(attrs)
                            .map(Attribute::parse)
                            .collect(Collectors.toList());
                        client.addTorrent(s[1], attributes);
                        break;
                    case "get":
                        // <file_id>
                        client.getTorrent(s[1]);
                        break;
                    case "download":
                        // <torrent_path>
                        client.downloadTorrent(s[1]);
                        break;
                    case "seed":
                        // <torrent_path>
                        client.seedTorrent(s[1]);
                        break;
                    default:
                        printHelp();
            }
        }}

        client.closeConnection();
    }

    private static void printHelp() {
        System.out.println("Usage: <command> <args>");
        System.out.println("Commands: quit, search, create, add, get, download, seed");
    }
}
