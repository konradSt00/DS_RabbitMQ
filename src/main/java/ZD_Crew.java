import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ZD_Crew {
    static String crewName = "";


    public static void main(String[] argv) throws Exception {
        crewName = Utils.getInput("crew name");
        String CREW_QUEUE_IN = crewName+"QueueIn";

        Channel channel = Utils.createChannel();
        channel.exchangeDeclare(Utils.ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ORDER_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ACK_EXCHANGE, BuiltinExchangeType.TOPIC);

        channel.queueDeclare(CREW_QUEUE_IN, false, false, false, null).getQueue();
        channel.queueBind(CREW_QUEUE_IN, Utils.ADMIN_EXCHANGE, "Crews");
        channel.queueBind(CREW_QUEUE_IN, Utils.ADMIN_EXCHANGE, "All");
        channel.queueBind(CREW_QUEUE_IN, Utils.ACK_EXCHANGE, crewName);

        Consumer consumer = Utils.createConsumer(channel);
        channel.basicConsume(CREW_QUEUE_IN, false, consumer);
        communicate(channel);

    }

    public static void communicate(Channel channel) throws IOException {
        while (true) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Type 'start' to get order: ");
            String message = br.readLine();

            if ("exit".equals(message)) {
                break;
            } else if ("start".equals(message)) {
                getOrder(channel);
            }else {
                //do nothing
            }
        }
    }

    public static void getOrder(Channel channel){
        String[] keys = Utils.order;

        Arrays.stream(keys).forEach(
                key -> {
                    try {
                        channel.basicPublish(Utils.ORDER_EXCHANGE, key, null, crewName
                                        .getBytes("UTF-8"));
                        System.out.println("Crew " + crewName + " ordered " + key);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
        );

    }



}
