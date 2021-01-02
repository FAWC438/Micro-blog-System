package bupt.weibo.Listener;


import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Component
@RabbitListener(queues = "reg_email")//监听消息队列
public class RegEmailQueueListener {
    @RabbitHandler
    public void QueueReceiver(String reg_email) {
        try {
            //send email
            System.out.println("Receiver : " + reg_email);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
