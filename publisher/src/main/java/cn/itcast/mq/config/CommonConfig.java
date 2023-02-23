package cn.itcast.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CommonConfig implements ApplicationContextAware{


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);

        //配置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            //消息发送到交换机，但是没有路由到队列
            log.error("消息发送到队列失败，响应码：{}，失败原因：{}，交换机：{}，路由key：{}，消息：{}",
                    replyCode,replyText,exchange,routingKey,message.toString());

            //消息发送失败的处理方式，重发，或者丢弃，或者通知管理员
        });
    }
}
