package server;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Server {

    private final static String serverSearch = "serverSearch";


    public static void main(String[] args) throws IOException, TimeoutException {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Channel channelResponse = connection.createChannel();

        channel.exchangeDeclare("server",BuiltinExchangeType.DIRECT);
        String searchQueue = channel.queueDeclare(serverSearch, false, false, false, null).getQueue();
        channel.queueBind(searchQueue,"server",serverSearch);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");

                channelResponse.basicPublish("user",properties.getReplyTo(),null,("TorrentFile:"+message).getBytes());
            }
        };

        channel.basicConsume(serverSearch, true, consumer);

    }

}


