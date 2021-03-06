package server;

import com.rabbitmq.client.*;
import data.MessageConfig;
import data.MetaData;
import data.messages.SearchResponseTorrentMessage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Server {

    private Connection connection;
    private Channel channelResponse;
    private Database database;
    private SubscriptionStorage subscriptionStorage;

    public Server(SubscriptionStorage subscriptionStorage) {
        this.subscriptionStorage = subscriptionStorage;
    }

    public Channel getChannelResponse() {
        return channelResponse;
    }

    public Database getDatabase() {
        return database;
    }

    public SubscriptionStorage getSubscriptionStorage() {
        return this.subscriptionStorage;
    }

    public void openConnection(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channelResponse = connection.createChannel();

            channel.exchangeDeclare(MessageConfig.SERVER_EXCHANGE,BuiltinExchangeType.DIRECT);
            String searchQueue = channel.queueDeclare(MessageConfig.serverSearch, false, false, false, null).getQueue();
            channel.queueBind(searchQueue,MessageConfig.SERVER_EXCHANGE,MessageConfig.serverSearch);

            String getQueue = channel.queueDeclare(MessageConfig.serverGet, false, false, false, null).getQueue();
            channel.queueBind(getQueue,MessageConfig.SERVER_EXCHANGE,MessageConfig.serverGet);

            String addQueue = channel.queueDeclare(MessageConfig.serverAdd, false, false, false, null).getQueue();
            channel.queueBind(addQueue,MessageConfig.SERVER_EXCHANGE,MessageConfig.serverAdd);

            String accessQueue = channel.queueDeclare(MessageConfig.serverAccess, false, false, false, null).getQueue();
            channel.queueBind(accessQueue,MessageConfig.SERVER_EXCHANGE,MessageConfig.serverAccess);

            String loginQueue = channel.queueDeclare(MessageConfig.serverLogin, false, false, false, null).getQueue();
            channel.queueBind(loginQueue,MessageConfig.SERVER_EXCHANGE,MessageConfig.serverLogin);

            database = new MySQLDatabase();

            System.out.println(" [SERVER] Waiting for data. To exit press CTRL+C");

            Consumer searchConsumer = new SearchMessageConsumer(channel,this);
            Consumer addConsumer = new AddMessageConsumer(channel,this);
            Consumer accessConsumer = new AccessMessageConsumer(channel,this);
            Consumer getConsumer = new GetMessageConsumer(channel,this);
            Consumer loginConsumer = new LoginMessageConsumer(channel,this);

            new Thread(()->{
                try {
                    channel.basicConsume(MessageConfig.serverSearch, true, searchConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(MessageConfig.serverGet, true, getConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(MessageConfig.serverAdd, true, addConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(MessageConfig.serverAccess, true, accessConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(MessageConfig.serverLogin, true, loginConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listDatabase(){
        System.out.format("|%7s|%12s|%15s|%7s|%7s|%6s|%13s|\n","ID","NAME", "FILE SIZE", "X","Y","ACCESS","OWNER");
        SearchResponseTorrentMessage srtm = database.getAllTorrents();
        for(int i = 0; i<srtm.getRecordsMetadata().size();i++){
            int index = srtm.getRecordsIndexes().get(i);
            MetaData md = srtm.getRecordsMetadata().get(i);
            System.out.format("|%7d|%12s|%15d|%7d|%7d|%6b|%13s|\n", index, md.getName(), md.getFileLength(), md.getX(),md.getY(),md.isAccessPublic(),md.getOwnerID());
        }

    }
}


