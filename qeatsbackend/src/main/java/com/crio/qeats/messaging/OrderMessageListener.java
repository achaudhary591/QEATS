package com.crio.qeats.messaging;

import com.crio.qeats.globals.GlobalConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderMessageListener {

  @RabbitListener(queues = GlobalConstants.QUEUE_NAME)
  public void consumeDefaultMessage(final Message message) {
    log.info("Received message {}", message);
  }
}
