package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.TorrentRecordMessage;
import data.GetTorrentMessage;
import data.MetaData;
import data.SearchTorrentMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

public class Server {

    private final static String serverSearch = "serverSearch";
    private final static String serverGet = "serverGet";
    private final static String serverAdd = "serverAdd";
    private Connection connection;
    private Database database;

    public void openConnection(){
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            Channel channel = connection.createChannel();
            Channel channelResponse = connection.createChannel();

            channel.exchangeDeclare("server",BuiltinExchangeType.DIRECT);
            String searchQueue = channel.queueDeclare(serverSearch, false, false, false, null).getQueue();
            channel.queueBind(searchQueue,"server",serverSearch);

            String getQueue = channel.queueDeclare(serverGet, false, false, false, null).getQueue();
            channel.queueBind(getQueue,"server",serverGet);

            String addQueue = channel.queueDeclare(serverAdd, false, false, false, null).getQueue();
            channel.queueBind(addQueue,"server",serverAdd);

            database = new Database();

            System.out.println(" [*] Waiting for data. To exit press CTRL+C");

            Consumer searchConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [Search] Received '" + message + "'");
                    ArrayList<MetaData> queryResult = database.getAllTorrents();
                    String json = new ObjectMapper().writeValueAsString(new SearchTorrentMessage(queryResult));
                    channelResponse.basicPublish("user",properties.getReplyTo(),null,json.getBytes());
                }
            };

            Consumer addConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    ObjectMapper objectMapper = new ObjectMapper();
                    TorrentRecordMessage torrentRecordMessage = objectMapper.readValue(body, TorrentRecordMessage.class);
                    System.out.println(" [Add] Received '" + torrentRecordMessage.getMetaData().getName() + "'");
                    database.addTorrent(torrentRecordMessage);
                    channelResponse.basicPublish("user",properties.getReplyTo(),null,("Add "+ torrentRecordMessage.getMetaData().getName()+" success").getBytes());
                }
            };

            Consumer getConsumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message = new String(body, "UTF-8");
                    ObjectMapper objectMapper = new ObjectMapper();
                    GetTorrentMessage getTorrentMessage = objectMapper.readValue(body, GetTorrentMessage.class);
                    TorrentRecordMessage requestedTorrent = database.getTorrent(getTorrentMessage.getId());
                    String json = objectMapper.writeValueAsString(requestedTorrent);
                    channelResponse.basicPublish("user",properties.getReplyTo(),null,json.getBytes());
                }
            };

            new Thread(()->{
                try {
                    channel.basicConsume(serverSearch, true, searchConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(serverGet, true, getConsumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(()->{
                try {
                    channel.basicConsume(serverAdd, true, addConsumer);
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
        System.out.format("|%12s|%15s|%7s|%7s|%13s|", "NAME", "FILE SIZE", "X","Y","OWNER");

        for(MetaData md: database.getAllTorrents()){
            System.out.format("|%12s|%15d|%7d|%7d|%13s|", md.getName(), md.getFileLength(), md.getX(),md.getY(),md.getOwnerID());
        }

    }

}


