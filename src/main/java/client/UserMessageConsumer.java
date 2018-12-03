package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.MessageConfig;
import data.MetaData;
import data.SearchResponseTorrentMessage;
import data.TorrentRecordMessage;

import java.io.IOException;

public class UserMessageConsumer extends DefaultConsumer {

    private Client client;
    public UserMessageConsumer(Channel channel,Client client) {
        super(channel);
        this.client=client;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        switch (properties.getContentType()) {
            case MessageConfig.ACTION_ADD:
                System.out.println(" [User] ADD_RESPONSE '" + message + "'");
                break;
            case MessageConfig.ACTION_GET:
                TorrentRecordMessage torrentRecordMessage = objectMapper.readValue(body,TorrentRecordMessage.class);
                System.out.println(" [User] GET_RESPONSE '" + torrentRecordMessage.getMetaData().getName() + "'");
                break;
            case MessageConfig.ACTION_SEARCH:
                SearchResponseTorrentMessage searchResponseTorrentMessage = objectMapper.readValue(body, SearchResponseTorrentMessage.class);
                System.out.println(" [User] SEARCH_RESPONSE: '");
                if(searchResponseTorrentMessage.getRecords().isEmpty()){
                    System.out.println("No results matching");
                }
                for(MetaData md: searchResponseTorrentMessage.getRecords()){
                    System.out.format("|%12s|%15d|%7d|%7d|%13s|\n", md.getName(), md.getFileLength(), md.getX(),md.getY(),md.getOwnerID());
                }
                break;
            default:
                System.out.println(" [User] Unknown '" + message + "'");
        }

    }
}
