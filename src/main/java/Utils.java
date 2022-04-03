import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

public class Utils {

    static String ORDER_EXCHANGE = "ORDER_EXCHANGE";
    static String ADMIN_EXCHANGE = "ADMIN_EXCHANGE";
    static String ACK_EXCHANGE = "ACK_EXCHANGE";
    static List<String> availableGoods = List.of("buty", "plecak", "tlen");
    static String[] order = {"tlen", "tlen", "buty", "buty", "plecak", "plecak"};
    static Map<String, String> productQueue =
            Map.of("tlen", "O2Queue", "buty", "shoesQueue", "plecak", "backpackQueue");

    public static Consumer createConsumer(Channel channel){
        return new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

                String message = new String(body, "UTF-8");
                System.out.println("Received: " + message + "\n");
                channel.basicAck(envelope.getDeliveryTag(), false);
            }
        };
    }
    public static String getInput(String k) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter " + k + ": ");
        return br.readLine();
    }

    public static Channel createChannel() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        return connection.createChannel();
    }

}
