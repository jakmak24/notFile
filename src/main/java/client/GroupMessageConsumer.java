package client;

import com.rabbitmq.client.*;

import java.io.IOException;

public class GroupMessageConsumer extends DefaultConsumer {

    private final Client client;

    public GroupMessageConsumer(Channel channel, Client client) {
        super(channel);
        this.client=client;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [Group] Received '" + message + "'");
    }
}
