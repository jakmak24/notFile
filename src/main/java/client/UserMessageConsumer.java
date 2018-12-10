package client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.MessageConfig;
import data.MetaData;
import data.messages.AccessRequestMessage;
import data.messages.SearchResponseTorrentMessage;
import data.messages.TorrentRecordMessage;

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
            case MessageConfig.ACTION_INFO:
                System.out.println(" [User] INFO: '" + message + "'");
                break;
            case MessageConfig.ACTION_ACCESS:
                AccessRequestMessage msg = objectMapper.readValue(body, AccessRequestMessage.class);
                System.out.println(" [User] ACCESS REQUESTED: 'User " + msg.getUserId()
                    + " requested access to dataset " + msg.getId() + "'");
                // TODO: Handle accepting/rejecting.
                // client.askForPermission();
                break;
            case MessageConfig.ACTION_GET:
                TorrentRecordMessage torrentRecordMessage = objectMapper.readValue(body,TorrentRecordMessage.class);
                System.out.println(" [User] GET_RESPONSE '" + torrentRecordMessage.getMetaData().getName() + "'");
                break;
            case MessageConfig.ACTION_SEARCH:
                SearchResponseTorrentMessage srtm = objectMapper.readValue(body, SearchResponseTorrentMessage.class);
                System.out.println(" [User] SEARCH_RESPONSE: '");
                if(srtm.getRecordsMetadata().isEmpty()){
                    System.out.println("No results matching");
                }
                System.out.format("|%7s|%12s|%15s|%7s|%7s|%6s|%13s|\n","ID","NAME", "FILE SIZE", "X","Y","ACCESS","OWNER");
                for(int i = 0; i<srtm.getRecordsMetadata().size();i++){
                    int index = srtm.getRecordsIndexes().get(i);
                    MetaData md = srtm.getRecordsMetadata().get(i);
                    System.out.format("|%7d|%12s|%15d|%7d|%7d|%6b|%13s|\n", index, md.getName(), md.getFileLength(), md.getX(),md.getY(),md.isAccessPublic(),md.getOwnerID());
                }
                break;
            case MessageConfig.ACTION_LOGIN:
                String response = objectMapper.readValue(body,String.class);
                if(response.equals("OK")){
                    System.out.println("[User] LOGIN: Success");
                    client.setLogged(true);
                }else if (response.equals("NEW")){
                    System.out.println("[User] LOGIN: New user created");
                    client.setLogged(true);
                }else if (response.equals("ACCESS_DENIED")) {
                    System.out.println("[User] LOGIN: access denied");
                    client.setLogged(false);
                }
                break;
            default:
                System.out.println(" [User] Unknown '" + message + "'");
        }

    }
}
