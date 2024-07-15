/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.models.CartEntity;
import com.crio.qeats.models.OrderEntity;
import com.crio.qeats.repositories.CartRepository;
import com.crio.qeats.repositories.OrderRepository;
import java.time.LocalTime;
import javax.inject.Provider;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderRepositoryServiceImpl implements OrderRepositoryService {

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private CartRepository cartRepository;

  @Override
  public Order placeOrder(Cart cart) {
    final ModelMapper mapper = modelMapperProvider.get();
    OrderEntity orderEntity = new OrderEntity();
    orderEntity.setRestaurantId(cart.getRestaurantId());
    orderEntity.setUserId(cart.getUserId());
    orderEntity.setTotal(cart.getTotal());
    orderEntity.setPlacedTime(LocalTime.now().toString());
    log.info("Order Entity {}", orderEntity);
    Order order = mapper.map(orderRepository.save(orderEntity), Order.class);
    CartEntity cartEntity = mapper.map(cart, CartEntity.class);
    cartEntity.clearCart();
    cartRepository.save(cartEntity);
    return order;
  }
}