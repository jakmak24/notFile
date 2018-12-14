package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import data.MessageConfig;
import data.messages.TorrentRecordMessage;

import java.io.IOException;

public class AccessMessageConsumer extends DefaultConsumer {

    private Server server;
    public AccessMessageConsumer(Channel channel, Server server) {
        super(channel);
        this.server=server;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        String fileId = new String(body);
        if (properties.getContentType().equals(MessageConfig.ACTION_ACCESS_REQUEST_ACCEPT)) {
            System.out.println(" [ACCESS] Access to file " + fileId + " ACCEPTED.");

            ObjectMapper objectMapper = new ObjectMapper();
            TorrentRecordMessage torrent = server.getDatabase().getTorrent(Integer.parseInt(fileId));
            String json = objectMapper.writeValueAsString(torrent);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_GET).build();
            server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE, properties.getReplyTo(), props,
                json.getBytes());
        } else {
            String message = " [ACCESS] Access to file " + fileId + " REJECTED.";
            System.out.println(message);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_INFO).build();
            server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE, properties.getReplyTo(), props,
                message.getBytes("UTF-8"));
        }
    }
}