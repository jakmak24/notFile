package pt.ul.fc.mt.notfile.client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ClientApi {
    private static final String serverGet = "serverGet";
    private final Client client;

    private Connection connection;
    private Channel sendChannel;
    private WebtorrentWrapper webtorrentWrapper;

    public ClientApi(Client client) {
        this.client = client;
        this.webtorrentWrapper = new WebtorrentWrapper();
    }

    public void openConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Config.RABBIT_MQ_HOST);
        try {
            connection = factory.newConnection();
            Channel userChannel = connection.createChannel();
            Channel groupChannel = connection.createChannel();
            sendChannel = connection.createChannel();

            Thread userChannelThread = new Thread(() -> {
                try {
                    String userQueue = setUpUserChannel(userChannel);
                    Consumer consumer = new UserMessageConsumer(client, userChannel, sendChannel);
                    userChannel.basicConsume(userQueue, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            userChannelThread.start();

            new Thread(() -> {
                try {
                    String groupQueue = setUpGroupChannel(groupChannel);
                    Consumer consumer = new GroupMessageConsumer(client, groupChannel);
                    groupChannel.basicConsume(groupQueue, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    private String setUpUserChannel(Channel userChannel) throws IOException {
        String userQueue = userChannel.queueDeclare().getQueue();
        userChannel.exchangeDeclare(Config.USERS_TOPIC, BuiltinExchangeType.TOPIC);
        // The binding key will be in the form: <"all"|userID>.<MATCH_FOUND|ACCESS_REQ|ACCESS_RESPONSE|...>
        userChannel.queueBind(userQueue, Config.USERS_TOPIC, client.getUserID() + ".*");
        userChannel.queueBind(userQueue, Config.USERS_TOPIC, Config.ALL_USERS_ROUTING_KEY + ".*");
        return userQueue;
    }

    private String setUpGroupChannel(Channel groupChannel) throws IOException {
        String groupQueue = groupChannel.queueDeclare().getQueue();
        groupChannel.exchangeDeclare(Config.GROUP_TOPIC, BuiltinExchangeType.TOPIC);
        // The binding key will be in the form: <groupID>.<FILE_DOWNLOADED|USER_JOINED|...>
        groupChannel.queueBind(groupQueue, Config.GROUP_TOPIC, client.getGroupID() + ".*");
        return groupQueue;
    }

    public void searchTorrents(String query) {
        //TODO send torrent file to server search for torrent
    }

    public void addTorrent(String torrent) {
        //TODO send torrent file to server
    }

    public void getTorrent(String torrentID) {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().replyTo(client.getUserID()).build();
        try {
            sendChannel.basicPublish("server", serverGet, props, torrentID.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(" [x] Sent '" + torrentID + "'");
    }

    public void closeConnection() {
        try {
            sendChannel.close();
            connection.close();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
