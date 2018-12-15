package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.Attribute;
import data.MessageConfig;
import data.Subscription;
import data.messages.SubscriptionMatchMessage;
import data.messages.TorrentRecordMessage;

import java.io.IOException;
import java.util.List;

public class AddMessageConsumer extends DefaultConsumer{

    private Server server;
    public AddMessageConsumer(Channel channel, Server server) {
        super(channel);
        this.server=server;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        TorrentRecordMessage torrentRecordMessage = objectMapper.readValue(body, TorrentRecordMessage.class);
        int torrentId = server.getDatabase().addTorrent(torrentRecordMessage);
        System.out.println(" [ADD] Added '" + torrentRecordMessage.getMetaData().getName() + "'");

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_ADD).build();
        server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                torrentRecordMessage.getMetaData().getName().getBytes());

        // Notify subscribers if found.
        List<Subscription> matchingSubscriptions =
            server.getSubscriptionStorage().findMatching(torrentRecordMessage.getMetaData());
        if (matchingSubscriptions.size() == 0) {
            System.out.println(" [ADD] No matching subscriptions found.");
        }
        for (Subscription subscription : matchingSubscriptions) {
            System.out.format(" [ADD] Found matching subscription by user: %s (%s).",
                subscription.getUserId(), Attribute.print(subscription.getQuery().getAttributes()));
            SubscriptionMatchMessage smm = new SubscriptionMatchMessage(subscription, torrentId);
            props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_MATCH_FOUND).build();

            objectMapper = objectMapper.registerModule(new JavaTimeModule());
            server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE, subscription.getUserId(), props,
                objectMapper.writeValueAsBytes(smm));
        }
    }
}
