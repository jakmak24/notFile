package pt.ul.fc.mt.notfile.client;

import com.rabbitmq.client.*;

import java.io.IOException;

public class UserMessageConsumer extends DefaultConsumer {
    private final Client client;
    private final Channel sendChannel;

    public UserMessageConsumer(Client client, Channel userChannel, Channel sendChannel) {
        super(userChannel);
        this.client = client;
        this.sendChannel = sendChannel;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
        throws IOException {

        String message = new String(body, "UTF-8");
        System.out.println(" [x] User " + client.getUserID() + " received: '" + message + "'");

        sendChannel.basicPublish(Config.GROUP_TOPIC, ClientUtils.getRoutingKey(client.getGroupID(), NotFileAction.FILE_DOWNLOAD),
            null, ("User:" + client.getUserID() + " downloaded.").getBytes());
    }
}
