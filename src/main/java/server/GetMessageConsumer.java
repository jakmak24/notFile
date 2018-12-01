package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.GetTorrentMessage;
import data.MessageConfig;
import data.TorrentRecordMessage;

import java.io.IOException;

public class GetMessageConsumer extends DefaultConsumer {

    private Server server;
    public GetMessageConsumer (Channel channel, Server server) {
        super(channel);
        this.server=server;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        GetTorrentMessage getTorrentMessage = objectMapper.readValue(body, GetTorrentMessage.class);

        TorrentRecordMessage requestedTorrent = server.getDatabase().getTorrent(getTorrentMessage.getId());
        String json = objectMapper.writeValueAsString(requestedTorrent);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_GET).build();

        server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                json.getBytes());
    }
}
