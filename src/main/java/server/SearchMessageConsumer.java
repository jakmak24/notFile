package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.MessageConfig;
import data.Subscription;
import data.messages.SearchQueryMessage;
import data.messages.SearchResponseTorrentMessage;

import java.io.IOException;
import java.time.LocalDateTime;

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

        SearchQueryMessage sqm = new ObjectMapper().readValue(message, SearchQueryMessage.class);
        SearchResponseTorrentMessage queryResult = server.getDatabase().searchTorrents(sqm.getAttributes());

        if (queryResult.getRecordsIndexes().size() == 0) {
            server.getSubscriptionStorage().put(new Subscription(sqm, properties.getReplyTo(), LocalDateTime.now()));

            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_INFO).build();
            server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                ("No results found for your search criteria.\nYour query was saved and you will be notified once " +
                    "a dataset with these properties is available.").getBytes());
        } else {
            String json = new ObjectMapper().writeValueAsString(queryResult);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_SEARCH).build();
            server.getChannelResponse().basicPublish(MessageConfig.USER_EXCHANGE,properties.getReplyTo(),props,
                json.getBytes());
        }
    }
}
