package com.crio.qeats.exchanges;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostOrderRequest {
  private String cartId;

  public boolean isValidRequest() {
    return StringUtils.isNotEmpty(cartId);
  }
}