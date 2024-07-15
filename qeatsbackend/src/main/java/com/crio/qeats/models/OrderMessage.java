package com.crio.qeats.models;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderMessage implements Serializable {
  private String text;
}
