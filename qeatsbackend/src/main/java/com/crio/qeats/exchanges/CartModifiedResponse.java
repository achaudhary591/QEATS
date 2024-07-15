package com.crio.qeats.exchanges;

import com.crio.qeats.dto.Cart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartModifiedResponse {
  private Cart cart;
  private int cartResponseType;
}
