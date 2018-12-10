package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import data.MessageConfig;
import data.messages.GetTorrentMessage;
import data.messages.LoginMessage;
import data.messages.TorrentRecordMessage;

import java.io.IOException;

public class LoginMessageConsumer extends DefaultConsumer{

    private Server server;
    public LoginMessageConsumer (Channel channel, Server server) {
        super(channel);
        this.server=server;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        LoginMessage loginMessage = objectMapper.readValue(body, LoginMessage.class);

        String response = "OK";
        String json = objectMapper.writeValueAsString(response);
        AMQP.BasicProperties props = new AMQP.BasicProperties.Builder().contentType(MessageConfig.ACTION_LOGIN).build();

        server.getChannelResponse().basicPublish("",properties.getReplyTo(),props, json.getBytes());
    }


}
