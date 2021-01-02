package bupt.weibo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author longzhonghua
 * @data 2019/02/03 11:07
 * 队列配置
 */
@Configuration
public class RabbitConfig {
    @Bean
    public Queue regQueue() {
        return new Queue("reg_email");
    }
}
