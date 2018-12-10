package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.MessageConfig;
import data.messages.TorrentRecordMessage;

import java.io.IOException;

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
        server.getDatabase().addTorrent(torrentRecordMessage);
        System.out.println(" [ADD] Added '" + torrentRecordMessage.getMetaData().getName() + "'");

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_ADD).build();
        server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                torrentRecordMessage.getMetaData().getName().getBytes());
    }
}
