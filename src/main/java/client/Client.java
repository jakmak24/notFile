package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.TorrentRecordMessage;
import data.GetTorrentMessage;
import data.MetaData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeoutException;

public class Client {

    private final String userID;
    private final String groupID;
    private static final String serverGet = "serverGet";
    private static final String serverAdd = "serverAdd";
    private final static String serverSearch = "serverSearch";
    private Connection connection;
    private Channel sendChannel;
    private WebtorrentWraper webtorrentWraper;


    public Client(String userID, String groupID){
        this.userID = userID;
        this.groupID = groupID;
        this.webtorrentWraper = new WebtorrentWraper();
    }

    public void openConnection()  {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Config.RABBIT_MQ_HOST);

        try {
            connection = factory.newConnection();
            Channel userChannel = connection.createChannel();
            Channel groupChannel = connection.createChannel();
            sendChannel = connection.createChannel();

            String userQueue = userChannel.queueDeclare().getQueue();
            userChannel.exchangeDeclare("user", BuiltinExchangeType.DIRECT);
            userChannel.queueBind(userQueue, "user", userID);

            String groupQueue = groupChannel.queueDeclare().getQueue();
            groupChannel.exchangeDeclare("group", BuiltinExchangeType.TOPIC);
            groupChannel.queueBind(groupQueue, "group", groupID);


            Consumer userConsumer = new DefaultConsumer(userChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [USER] Received '" + message + "'");

                }
            };

            Consumer groupConsumer = new DefaultConsumer(groupChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [Group] Received '" + message + "'");
                }
            };

            new Thread(()->{
                try {
                    groupChannel.basicConsume(groupQueue, true, groupConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            new Thread(()->{
                try {
                    userChannel.basicConsume(userQueue, true, userConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }


    public void searchTorrents(String query){
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().replyTo(userID).build();
        try {
            sendChannel.basicPublish("server", serverSearch, props, query.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createTorrent(String fileName, String filePath){
        return webtorrentWraper.createTorrent(fileName,filePath,Config.TORRENT_FOLDER,
                Config.TRACKER_ANNOUNCE);
    }

    public void addTorrent(String torrent){
        File f = new File(torrent);
        long size = f.length();

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().replyTo(userID).build();
        try {
            byte[] fileData = Files.readAllBytes(f.toPath());
            TorrentRecordMessage torrentRecordMessage = new TorrentRecordMessage(new MetaData(f.getName(),userID,0,0,size),fileData);
            String json = new ObjectMapper().writeValueAsString(torrentRecordMessage);
            sendChannel.basicPublish("server", serverAdd, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getTorrent(String torrentID){
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().replyTo(userID).build();
        try {
            GetTorrentMessage getTorrentMessage = new GetTorrentMessage(Integer.parseInt(torrentID));
            String json = new ObjectMapper().writeValueAsString(getTorrentMessage);
            sendChannel.basicPublish("server", serverGet, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection()  {
        try {
            sendChannel.close();
            connection.close();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

    }
}
