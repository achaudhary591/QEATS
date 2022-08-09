/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import com.mongodb.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class GetRestaurantsRequest {
  
  @NonNull
  @Min(value = -90)
  @Max(value = 90)
  private Double latitude;

  @NonNull
  @Min(value = -180)
  @Max(value = 180)
  private Double longitude;

  private String searchFor = "";

  public GetRestaurantsRequest(double latitude, double longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  
}

