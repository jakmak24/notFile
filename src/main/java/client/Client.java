package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.*;
import data.messages.GetTorrentMessage;
import data.messages.SearchQueryMessage;
import data.messages.TorrentRecordMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class Client {

    private Connection connection;
    private Channel sendChannel;
    private Channel userChannel;
    private Channel groupChannel;
    private WebtorrentWrapper webtorrentWrapper = new WebtorrentWrapper();
    private User user;
    private String groupQueue;
    private String userQueue;

    public Boolean getLogged() {
        return isLogged;
    }

    public void setLogged(Boolean logged) {
        isLogged = logged;
    }

    private Boolean isLogged = false;


    public User getUser() {
        return user;
    }

    public Channel getSendChannel() {
        return sendChannel;
    }

    public void openConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Config.RABBIT_MQ_HOST);

        try {
            System.out.println("Connecting...");
            factory.setUsername("test");
            factory.setPassword("test");
            connection = factory.newConnection();
            userChannel = connection.createChannel();
            groupChannel = connection.createChannel();
            sendChannel = connection.createChannel();

            userQueue = userChannel.queueDeclare().getQueue();
            userChannel.exchangeDeclare(MessageConfig.USER_EXCHANGE, BuiltinExchangeType.DIRECT);

            groupQueue = groupChannel.queueDeclare().getQueue();
            groupChannel.exchangeDeclare(MessageConfig.GROUP_EXCHANGE, BuiltinExchangeType.DIRECT);


            startConsumers();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void startConsumers() {
        Consumer userConsumer = new UserMessageConsumer(userChannel, this);
        Consumer groupConsumer = new GroupMessageConsumer(userChannel, this);

        new Thread(() -> {
            try {
                groupChannel.basicConsume(groupQueue, true, groupConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                userChannel.basicConsume(userQueue, true, userConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public void login(String userID, String groupID){

        while(!isLogged){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        this.user = new User(userID,groupID);
        try {
            groupChannel.queueBind(groupQueue, MessageConfig.GROUP_EXCHANGE, this.user.getGroupID());
            userChannel.queueBind(userQueue, MessageConfig.USER_EXCHANGE, this.user.getUserID());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void searchTorrents(List<Attribute> attributes) {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(user.getUserID())
            .contentType(MessageConfig.ACTION_SEARCH)
            .build();
        try {
            String json = new ObjectMapper().writeValueAsString(new SearchQueryMessage(attributes));
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverSearch, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String createTorrent(String fileName, String filePath) {
        return webtorrentWrapper.createTorrent(fileName, filePath, Config.TORRENT_FOLDER,
            Config.TRACKER_ANNOUNCE);
    }

    public void addTorrent(String torrent, List<Attribute> attributes) {
        File f = new File(torrent);
        long size = f.length();
        attributes.add(new Attribute("filesize", Attribute.Relation.EQ,String.valueOf(size),"Long"));
        attributes.add(new Attribute("owner", Attribute.Relation.EQ,user.getUserID(),"String"));
        // TODO: Add anything else that's needed.
        MetaData metaData = Attribute.convertToMetaData(attributes);

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(user.getUserID())
            .contentType(MessageConfig.ACTION_ADD)
            .build();
        try {
            byte[] fileData = Files.readAllBytes(f.toPath());
            TorrentRecordMessage torrentRecordMessage = new TorrentRecordMessage(metaData, fileData);
            String json = new ObjectMapper().writeValueAsString(torrentRecordMessage);
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverAdd, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getTorrent(String torrentID) {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(user.getUserID())
            .contentType(MessageConfig.ACTION_GET)
            .build();
        try {
            GetTorrentMessage getTorrentMessage = new GetTorrentMessage(Integer.parseInt(torrentID));
            String json = new ObjectMapper().writeValueAsString(getTorrentMessage);
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverGet, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadTorrent(String torrentPath) {
        new Thread(() -> {
            webtorrentWrapper.downloadTorrent(torrentPath, Config.DOWNLOAD_FOLDER);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .replyTo(user.getUserID())
                .contentType(MessageConfig.ACTION_TORRENT_DOWNLOADED)
                .build();
            try {
                sendChannel.basicPublish(MessageConfig.GROUP_EXCHANGE, user.getGroupID(), props, ("Downloaded" + torrentPath).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void seedTorrent(String torrentPath) {
        new Thread(() -> {
            webtorrentWrapper.seedTorrent(torrentPath, Config.DOWNLOAD_FOLDER);
        }).start();
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
