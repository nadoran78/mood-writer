package com.example.moodwriter.global.config;

import com.example.moodwriter.global.constant.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  @Bean
  public Queue notificationQueue() {
    return new Queue(RabbitMQConstants.NOTIFICATION_QUEUE.getValue(), true);
  }

  @Bean
  public TopicExchange notificationExchange() {
    return new TopicExchange(RabbitMQConstants.NOTIFICATION_EXCHANGE.getValue());
  }

  @Bean
  public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
    return BindingBuilder.bind(notificationQueue)
        .to(notificationExchange)
        .with(RabbitMQConstants.NOTIFICATION_ROUTING_KEY.getValue());
  }

}
