import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

public class ZD_Admin {

    public static void main(String[] argv) throws Exception {
        String ADMIN_QUEUE = "AdminQueue";
        Channel channel = Utils.createChannel();

        channel.exchangeDeclare(Utils.ADMIN_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ORDER_EXCHANGE, BuiltinExchangeType.DIRECT);
        channel.exchangeDeclare(Utils.ACK_EXCHANGE, BuiltinExchangeType.TOPIC);


        channel.queueDeclare(ADMIN_QUEUE, false, false, false, null).getQueue();
        channel.queueBind(ADMIN_QUEUE, Utils.ORDER_EXCHANGE, "tlen");
        channel.queueBind(ADMIN_QUEUE, Utils.ORDER_EXCHANGE, "buty");
        channel.queueBind(ADMIN_QUEUE, Utils.ORDER_EXCHANGE, "plecak");
        channel.queueBind(ADMIN_QUEUE, Utils.ACK_EXCHANGE, "#");

        Consumer consumer = Utils.createConsumer(channel);

        System.out.println("Waiting for messages...");
        channel.basicConsume(ADMIN_QUEUE, false, consumer);
        String destination, message;
        while(true){
            destination = Utils.getInput("destination(Crews/Suppliers/All)");
            message = Utils.getInput("your message");
            channel.basicPublish(Utils.ADMIN_EXCHANGE, destination, null, message
                    .getBytes("UTF-8"));
            System.out.println("Sending '" + message + "' to " + destination);

        }


    }






}
