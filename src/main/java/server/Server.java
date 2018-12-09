package server;

import com.rabbitmq.client.*;
import data.MessageConfig;
import data.MetaData;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Server {

    private Connection connection;
    private Channel channelResponse;
    private Database database;

    public Channel getChannelResponse() {
        return channelResponse;
    }
    public Database getDatabase() {
        return database;
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

            database = new Database();

            System.out.println(" [SERVER] Waiting for data. To exit press CTRL+C");

            Consumer searchConsumer = new SearchMessageConsumer(channel,this);
            Consumer addConsumer = new AddMessageConsumer(channel,this);
            Consumer getConsumer = new GetMessageConsumer(channel,this);

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
        System.out.format("|%12s|%15s|%7s|%7s|%13s|\n", "NAME", "FILE SIZE", "X","Y","OWNER");

        for(MetaData md: database.getAllTorrents()){
            System.out.format("|%12s|%15d|%7d|%7d|%13s|\n", md.getName(), md.getFileLength(), md.getX(),md.getY(),md.getOwnerID());
        }

    }

}


