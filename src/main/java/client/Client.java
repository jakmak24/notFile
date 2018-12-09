package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.*;

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

    public Client(User user) {
        this.user = user;
    }

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
            connection = factory.newConnection();
            userChannel = connection.createChannel();
            groupChannel = connection.createChannel();
            sendChannel = connection.createChannel();

            userQueue = userChannel.queueDeclare().getQueue();
            userChannel.exchangeDeclare(MessageConfig.USER_EXCHANGE, BuiltinExchangeType.DIRECT);
            userChannel.queueBind(userQueue, MessageConfig.USER_EXCHANGE, user.getUserID());

            groupQueue = groupChannel.queueDeclare().getQueue();
            groupChannel.exchangeDeclare(MessageConfig.GROUP_EXCHANGE, BuiltinExchangeType.DIRECT);
            groupChannel.queueBind(groupQueue, MessageConfig.GROUP_EXCHANGE, user.getGroupID());

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

    public void searchTorrents(List<Attribute> attributes) {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(user.getUserID())
            .contentType(MessageConfig.ACTION_SEARCH)
            .build();
        try {
            String json = new ObjectMapper().writeValueAsString(attributes);
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
        attributes.add(Attribute.parse("filesize = " + size));
        attributes.add(Attribute.parse("owner = " + user.getUserID()));
        // TODO: Add anything else that's needed.
        MetaData metaData = convertToMetaData(attributes);

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

    private MetaData convertToMetaData(List<Attribute> attributes) {
        MetaData.Builder builder = new MetaData.Builder();
        for (Attribute attr : attributes) {
            switch (attr.getName()) {
                case "name":
                    builder.name(attr.getValue());
                    break;
                case "owner":
                    builder.ownerID(attr.getValue());
                    break;
                case "x":
                    builder.x(Integer.parseInt(attr.getValue()));
                    break;
                case "y":
                    builder.y(Integer.parseInt(attr.getValue()));
                    break;
                case "filesize":
                    builder.fileLength(Long.parseLong(attr.getValue()));
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported attribute name: " + attr.getName());
            }
        }
        return builder.build();
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
