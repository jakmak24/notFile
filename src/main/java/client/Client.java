package client;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Client {

    private final String userID;
    private final String groupID;
    private static final String serverSearch = "serverSearch";
    private Connection connection;
    private Channel sendChannel;


    public Client(String userID, String groupID){
        this.userID = userID;
        this.groupID = groupID;
    }

    public void openConnection()  {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

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


            new Thread(() -> {
                Consumer consumer = new DefaultConsumer(userChannel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope,
                                               AMQP.BasicProperties properties, byte[] body)
                            throws IOException {
                        String message = new String(body, "UTF-8");
                        System.out.println(" [x] User received '" + message + "'");

                        sendChannel.basicPublish("group", groupID, null, ("User:" + userID + " downloaded").getBytes());

                    }
                };

                try {
                    userChannel.basicConsume(userQueue, true, consumer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            ).start();

        new Thread(() -> {
            Consumer consumer = new DefaultConsumer(groupChannel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope,
                                           AMQP.BasicProperties properties, byte[] body)
                        throws IOException {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] Group received '" + message + "'");
                }
            };

            try {
                groupChannel.basicConsume(groupQueue, true, consumer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ).start();

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
        System.out.println(" [x] Sent '" + query + "'");
    }

    public void addTorrent(String torrent){

    }

    public void getTorrent(String torrentID){

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
