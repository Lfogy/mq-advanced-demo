package cn.itcast.mq.spring;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringAmqpTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMessage2SimpleQueue() throws InterruptedException {

        String msg = "hello,spring amqp";

        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        //准备confirmCallback
        correlationData.getFuture().addCallback(result->{

            System.out.println(result.isAck());
            //判断消息是否到交换机
            if (result.isAck()){
                log.debug("消息成功投递到交换机！消息ID:{}",correlationData.getId());
            }else {
                log.error("消息投递到交换机失败！消息ID:{}"+correlationData.getId());
            }

        },ex->{
            //记录日志
            log.error("消息发送失败,"+ex);
            //重发消息
        });

        //发送消息
        rabbitTemplate.convertAndSend("amq.topic","simple.test",msg,correlationData);

        Thread.sleep(3000);
    }

    @Test
    public void testDurableMessage(){
        Message message = MessageBuilder.withBody("hello spring".getBytes(StandardCharsets.UTF_8))
                //消息持久化
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .build();

        rabbitTemplate.convertAndSend("simple.queue",message);
    }
}
