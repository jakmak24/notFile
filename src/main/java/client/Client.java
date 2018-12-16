package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.Attribute;
import data.MessageConfig;
import data.MetaData;
import data.messages.GetTorrentMessage;
import data.messages.LoginMessage;
import data.messages.SearchQueryMessage;
import data.messages.TorrentRecordMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;
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
    private List<DownloadProcess> downloadProcesses = new LinkedList<>();
    private List<SeedProcess> seedProcesses = new LinkedList<>();

    public synchronized Boolean getLogged() {
        return isLogged;
    }

    public synchronized void setLogged(Boolean logged) {
        isLogged = logged;
    }

    private Boolean isLogged;


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
            sendChannel = connection.createChannel();

            userQueue = userChannel.queueDeclare().getQueue();
            userChannel.exchangeDeclare(MessageConfig.USER_EXCHANGE, BuiltinExchangeType.DIRECT);

            startUserConsumer();

        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void startUserConsumer() {
        Consumer userConsumer = new UserMessageConsumer(userChannel, this);

        new Thread(() -> {
            try {
                userChannel.basicConsume(userQueue, true, userConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void startGroupChannel(){

        try {
            groupChannel = connection.createChannel();
            groupQueue = groupChannel.queueDeclare().getQueue();
            groupChannel.exchangeDeclare(MessageConfig.GROUP_EXCHANGE, BuiltinExchangeType.DIRECT);
            groupChannel.queueBind(groupQueue, MessageConfig.GROUP_EXCHANGE, this.user.getGroupName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Consumer groupConsumer = new GroupMessageConsumer(groupChannel, this);
        new Thread(() -> {
            try {
                groupChannel.basicConsume(groupQueue, true, groupConsumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


    public boolean login(String userName, String groupName,String password){

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .replyTo(userQueue)
                .contentType(MessageConfig.ACTION_LOGIN)
                .build();
        try {
            String json = new ObjectMapper().writeValueAsString(new LoginMessage(userName,groupName,password));
            sendChannel.basicPublish(MessageConfig.SERVER_EXCHANGE, MessageConfig.serverLogin, props, json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("LOGIN SEND");

        while(getLogged() == null){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(!getLogged()){
            return false;
        }else {
            this.user = new User(userName, groupName);
            try {
                userChannel.queueBind(userQueue, MessageConfig.USER_EXCHANGE, this.user.getUserID());
                if(!groupName.equals("no_group")){
                    startGroupChannel();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
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
        attributes.add(new Attribute("owner", Attribute.Relation.EQ, user.getUserID(),"String"));
        if (attributes.stream().noneMatch(a -> a.getName().equals("name"))) {
            attributes.add(new Attribute("name", Attribute.Relation.EQ, f.getName().split(".")[0],"String"));
        }
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
            DownloadProcess downloadProcess = webtorrentWrapper.downloadTorrent(torrentPath, Config.DOWNLOAD_FOLDER);
            downloadProcesses.add(downloadProcess);
            downloadProcess.start();
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                .replyTo(user.getUserID())
                .contentType(MessageConfig.ACTION_TORRENT_DOWNLOADED)
                .build();
            try {
                sendChannel.basicPublish(MessageConfig.GROUP_EXCHANGE,user.getGroupName(), props, ("Downloaded " + torrentPath).getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void seedTorrent(String torrentPath) {
        new Thread(() -> {
            SeedProcess seedProcess = webtorrentWrapper.seedTorrent(torrentPath, Config.DOWNLOAD_FOLDER);
            seedProcesses.add(seedProcess);
            seedProcess.start();
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

    public void acceptAccessRequest(String userId, String fileId) {
        System.out.println(" [User] ACCESS REQUEST: Accepting.");
        this.accessRequestRespond(userId, fileId, MessageConfig.ACTION_ACCESS_REQUEST_ACCEPT);
    }

    public void rejectAccessRequest(String userId, String fileId) {
        System.out.println(" [User] ACCESS REQUEST: Rejecting.");
        this.accessRequestRespond(userId, fileId, MessageConfig.ACTION_ACCESS_REQUEST_REJECT);
    }

    private void accessRequestRespond(String userId, String fileId, String response) {
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
            .replyTo(userId)
            .contentType(response)
            .build();
        try {
            sendChannel.basicPublish(
                MessageConfig.SERVER_EXCHANGE, MessageConfig.serverAccess, props, fileId.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void status(){
        System.out.println("STATUS:");
        if(!downloadProcesses.isEmpty()) {
            System.out.println("DOWNLOADING");
            {
                for (DownloadProcess d : downloadProcesses) {
                    System.out.println(d);
                }
            }
        }
        if(!seedProcesses.isEmpty()) {
            System.out.println("SEEDING");
            {
                for (SeedProcess s : seedProcesses) {
                    System.out.println(s);
                }
            }
        }

    }
}
