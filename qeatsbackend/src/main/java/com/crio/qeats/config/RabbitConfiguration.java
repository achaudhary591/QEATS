package com.crio.qeats.config;

import com.crio.qeats.globals.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitConfiguration {

  // COMPLETED: CRIO_TASK_MODULE_RABBITMQ - Setup RabbitMQ configuration.
  // To effectively use RabbitMQ, you have to setup some of the configurations in Spring Boot.
  // Feel free to use this class or do it in any other way you prefer.
  // Objective:
  // Use the queue, exchange and routing key in `GlobalConstants` to create the required beans.
  // TIP: Look in the direction of establishing a connection to RabbitMQ server,
  // serialization/de-serialization of messages.

  @Bean
  public TopicExchange topicExchange() {
    return new TopicExchange(GlobalConstants.EXCHANGE_NAME);
  }

  @Bean
  public Queue defaultParsingQueue() {
    return new Queue(GlobalConstants.QUEUE_NAME, true);
  }

  @Bean
  public Binding queueToExchangeBinding() {
    return BindingBuilder.bind(defaultParsingQueue())
        .to(topicExchange()).with(GlobalConstants.ROUTING_KEY);
  }

  @Bean
  public Jackson2JsonMessageConverter producerMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate();
    rabbitTemplate.setExchange(GlobalConstants.EXCHANGE_NAME);
    rabbitTemplate.setRoutingKey(GlobalConstants.ROUTING_KEY);
    rabbitTemplate.setMessageConverter(producerMessageConverter());
    rabbitTemplate.setConnectionFactory(connectionFactory);
    log.info("rabbitTemplate() called {}", rabbitTemplate);
    return rabbitTemplate;
  }
}
