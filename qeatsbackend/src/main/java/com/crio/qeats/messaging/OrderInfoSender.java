package com.crio.qeats.messaging;

import com.crio.qeats.dto.Order;
import com.crio.qeats.globals.GlobalConstants;
import java.nio.charset.Charset;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class OrderInfoSender implements PostOrderActions {

  private final RabbitTemplate rabbitTemplate;

  public OrderInfoSender(RabbitTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }


  @Override
  public void execute(Order order) {
    log.info("Sending Order Information for Order id " + order.getId());
    Message message = MessageBuilder.withBody("foo".getBytes(Charset.forName("UTF-8")))
        .setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN)
        .setMessageId(order.getId())
        .setHeader("bar", "baz")
        .build();
    rabbitTemplate.send(GlobalConstants.EXCHANGE_NAME, GlobalConstants.ROUTING_KEY,
        message);
  }

}
