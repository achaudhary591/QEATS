package com.crio.qeats.exchanges;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddCartRequest {
  private String cartId;
  private String itemId;
  private String restaurantId;

  public boolean isValidRequest() {
    return StringUtils.isNotEmpty(cartId)
        && StringUtils.isNotEmpty(itemId)
        && StringUtils.isNotEmpty(restaurantId);
  }
}
