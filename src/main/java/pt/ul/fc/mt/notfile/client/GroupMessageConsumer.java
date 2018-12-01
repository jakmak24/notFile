package pt.ul.fc.mt.notfile.client;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;

public class GroupMessageConsumer extends DefaultConsumer {
    private final Client client;

    public GroupMessageConsumer(Client client, Channel groupChannel) {
        super(groupChannel);
        this.client = client;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
        throws IOException {

        String message = new String(body, "UTF-8");
        System.out.println(" [x] Group " + client.getGroupID() + " received: '" + message + "'");
    }
}
