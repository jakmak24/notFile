package server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.Attribute;
import data.MessageConfig;
import data.MetaData;
import data.SearchResponseTorrentMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SearchMessageConsumer extends DefaultConsumer {

    private Server server;
    public SearchMessageConsumer(Channel channel,Server server) {
        super(channel);
        this.server=server;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        System.out.println(" [Search] Received '" + message + "'");

        List<Attribute> searchAttrs = new ObjectMapper().readValue(message, new TypeReference<List<Attribute>>(){});
        // TODO: use searchAttrs to select records.
        ArrayList<MetaData> queryResult = server.getDatabase().getAllTorrents();
        String json = new ObjectMapper().writeValueAsString(new SearchResponseTorrentMessage(queryResult));

        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_SEARCH).build();
        server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                json.getBytes());
    }
}
