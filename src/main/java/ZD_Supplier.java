import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZD_Supplier {
    static int orderId = 0;
    static String supplierName = "";

    public static void main(String[] argv) throws Exception {
        List<String> goods = new ArrayList<>();
        Channel channel = Utils.createChannel();
        supplierName = Utils.getInput("supplier name");
        String ADMIN_QUEUE = "AdminCommunicationQueue" + supplierName;
        getProducts(goods);

        channel.exchangeDeclare(Utils.ORDER_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ACK_EXCHANGE, BuiltinExchangeType.TOPIC);

        //QoS
        channel.basicQos(1);
        channel.queueDeclare(ADMIN_QUEUE, false, false, false, null).getQueue();
        channel.queueBind(ADMIN_QUEUE , Utils.ADMIN_EXCHANGE, "Suppliers");
        channel.queueBind(ADMIN_QUEUE, Utils.ADMIN_EXCHANGE, "All");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                if(envelope.getRoutingKey().equals("Suppliers") || envelope.getRoutingKey().equals("All"))
                    System.out.println("Received " + message + " from Admin");
                else{
                    System.out.println("Received order from " + message + ": " + envelope.getRoutingKey() +  "\n");
                    String key = message;
                    channel.basicPublish(Utils.ACK_EXCHANGE, key, null,
                            ("Ack for order " + key + "_sn" + orderId++ + "(" + envelope.getRoutingKey() + ") from " + supplierName)
                                    .getBytes("UTF-8"));
                }
                channel.basicAck(envelope.getDeliveryTag(), false);

            }
        };

        goods.forEach(product ->{
            try {
                String queueName = Utils.productQueue.get(product);
                channel.queueDeclare(queueName, false, false, false, null).getQueue();
                channel.queueBind(queueName, Utils.ORDER_EXCHANGE, product);
                channel.basicConsume(queueName, false, consumer);

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        channel.basicConsume(ADMIN_QUEUE, false, consumer);

        System.out.println("Waiting for messages...");

    }

    public static void getProducts(List<String> goodsList) throws IOException {
        while (true){
            String input = Utils.getInput("product (or exit)");
            if(input.equals("exit"))
                break;
            else if(Utils.availableGoods.contains(input) && !goodsList.contains(input)){
                goodsList.add(input);
                System.out.println(input + " added to list!");
            }else{
                System.out.println("Available products: " + Utils.availableGoods);
            }
        }
    }

}
