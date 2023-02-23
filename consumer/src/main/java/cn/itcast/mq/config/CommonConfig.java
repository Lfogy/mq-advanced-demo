package cn.itcast.mq.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class CommonConfig {

    /**
     * 交换机持久化
     * @return
     */
    @Bean
    public DirectExchange simpleDirect(){
        return new DirectExchange("simple.direct",true,false);
    }

    /**
     * 队列持久化
     * @return
     */
    @Bean
    public Queue simpleQueue(){
        //指定队列名称
        return QueueBuilder.durable("simple.queue").build();
    }

    //声明一个队列，并为其指定死信交换机
    @Bean
    public Queue simpleQueue2(){
        return QueueBuilder.durable("simple.queue")  //指定队列，并持久化
                .deadLetterExchange("dl.direct")   //指定死信交换机
                .build();
    }

    //声明一个死信交换机
    @Bean
    public DirectExchange dlExchange(){
        return new DirectExchange("dl.direct");
    }

    //声明死信队列
    @Bean
    public Queue dlQueue(){
        return new Queue("dl.queue",true);  //声明死信队列，并持久化
    }

    //将死信队列与死信交换机绑定
    @Bean
    public Binding dlBinding(){
        return BindingBuilder.bind(dlQueue()).to(dlExchange()).with("simple");
    }

}
