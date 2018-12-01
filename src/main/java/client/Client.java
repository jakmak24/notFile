package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.MessageConfig;
import data.TorrentRecordMessage;
import data.GetTorrentMessage;
import data.MetaData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeoutException;

public class Client {

    private Connection connection;
    private Channel sendChannel;
    private WebtorrentWraper webtorrentWraper= new WebtorrentWraper();
    private User user;

    public Client(User user){
        this.user = user;
    }

    public User getUser() {
        return user;
    }
    public Channel getSendChannel() {
        return sendChannel;
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
            userChannel.exchangeDeclare(MessageConfig.USER_EXCHANGE, BuiltinExchangeType.DIRECT);
            userChannel.queueBind(userQueue, MessageConfig.USER_EXCHANGE, user.getUserID());

            String groupQueue = groupChannel.queueDeclare().getQueue();
            groupChannel.exchangeDeclare(MessageConfig.GROUP_EXCHANGE, BuiltinExchangeType.DIRECT);
            groupChannel.queueBind(groupQueue, MessageConfig.GROUP_EXCHANGE, user.getGroupID());

            Consumer userConsumer = new UserMessageConsumer(userChannel,this);
            Consumer groupConsumer = new GroupMessageConsumer(userChannel,this);

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
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .replyTo(user.getUserID())
                .contentType(MessageConfig.ACTION_SEARCH)
                .build();
        try {
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverSearch, props, query.getBytes());
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

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .replyTo(user.getUserID())
                .contentType(MessageConfig.ACTION_ADD)
                .build();
        try {
            byte[] fileData = Files.readAllBytes(f.toPath());
            MetaData metaData = new MetaData(f.getName(),user.getUserID(),0,0,size);
            TorrentRecordMessage torrentRecordMessage = new TorrentRecordMessage(metaData,fileData);
            String json = new ObjectMapper().writeValueAsString(torrentRecordMessage);
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverAdd, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void getTorrent(String torrentID){
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

    public void downloadTorrent(String torrentPath){
        new Thread(()->{
            webtorrentWraper.downloadTorrent(torrentPath,Config.DOWNLOAD_FOLDER);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .replyTo(user.getUserID())
                    .contentType(MessageConfig.ACTION_TORRENT_DOWNLOADED)
                    .build();
            try {
                sendChannel.basicPublish(MessageConfig.GROUP_EXCHANGE,user.getGroupID(),props,("Downloaded"+torrentPath).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void seedTorrent(String torrentPath){
        webtorrentWraper.seedTorrent(torrentPath,Config.DOWNLOAD_FOLDER);
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
