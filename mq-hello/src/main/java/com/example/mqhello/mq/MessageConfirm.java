package com.example.mqhello.mq;

import com.example.mqhello.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @author sdw
 * @description:
 * @date 2023/2/22
 */
public class MessageConfirm {
    public static void main(String[] args) throws Exception {
        MessageConfirm.messageConfirmAsync();
    }

    /**
     * 单个消息确认
     * 发送一条消息，确认一条消息
     */
    public static void messageConfirmSingle() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //队列声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        long begin = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            String message = i + " ";
            channel.basicPublish("", queueName, null, message.getBytes());
            boolean b = channel.waitForConfirms();
            if (b) {
                System.out.println("消息发送成功");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("发送1000条消息总耗时" + (end - begin) + "ms");
    }

    /**
     * 批量消息确认
     * 发送100条消息，确认一次
     */
    public static void messageConfirmBatch() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //队列声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        long begin = System.currentTimeMillis();

        int batchSize = 100;

        for (int i = 0; i < 1000; i++) {
            String message = i + " ";
            channel.basicPublish("", queueName, null, message.getBytes());

            if (i % batchSize == 0) {
                channel.waitForConfirms();
                System.out.println("消息发送成功");
            }
        }

        long end = System.currentTimeMillis();
        System.out.println("批量发送1000条消息总耗时" + (end - begin) + "ms");
    }

    /**
     * 异步发布，处理未确认消息
     */
    public static void messageConfirmAsync() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        //队列声明
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
        //开启发布确认
        channel.confirmSelect();

        long begin = System.currentTimeMillis();

        /**
         * 线程安全有序的一个哈希表，适用于高并发情况下
         * key：消息序号，value：消息
         */
        ConcurrentSkipListMap<Long,String> outStandingConfirms =
                new ConcurrentSkipListMap<>();

        //消息确认成功回调函数
        ConfirmCallback ackCallBack = (deliveryTag, multiple) -> {
            if (multiple){
                //删除掉已经确认的消息，剩下的就是未确认的消息
                ConcurrentNavigableMap<Long, String> confirmed = outStandingConfirms.headMap(deliveryTag);

                //如果是批量发送的，就批量清除发送成功的消息
                confirmed.clear();
            }else {
                outStandingConfirms.remove(deliveryTag);
            }

            System.out.println("确认的消息：" + deliveryTag);
        };

        //消息确认失败回调函数
        ConfirmCallback nackCallBack = (deliveryTag, multiple) -> {
            //获取未确认的消息
            String s = outStandingConfirms.get(deliveryTag);
            System.out.println("未确认的消息：" + s);
        };

        //准备消息监听器，监听发布成功的消息和发布失败的消息
        channel.addConfirmListener(ackCallBack, nackCallBack);

        for (int i = 0; i < 1000; i++) {
            String message = "消息" + i;
            channel.basicPublish("", queueName, null, message.getBytes());
            //记录下所有要发布的消息，消息总和
            outStandingConfirms.put(channel.getNextPublishSeqNo(),message);
        }

        long end = System.currentTimeMillis();
        System.out.println("异步批量发送1000条消息总耗时" + (end - begin) + "ms");
    }
}
