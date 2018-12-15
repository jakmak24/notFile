package server;

import java.util.Scanner;

public class ServerMain {
    public static void main(String[] args) {

        Server server = new Server(new InMemorySubscriptionStorage());
        server.openConnection();
        Scanner scan = new Scanner(System.in);

        while (true) {
            String s = scan.nextLine();
            if (s.startsWith("quit")) break;
            if (s.startsWith("list")){
                server.listDatabase();
            }
        }

        server.closeConnection();
    }
}
