/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.exchanges;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestParam;

// COMPLETED: CRIO_TASK_MODULE_RESTAURANTSAPI - Implement GetRestaurantsRequest.
// Complete the class such that it is able to deserialize the incoming query params from
// REST API clients.
// For instance, if a REST client calls API
// /qeats/v1/restaurants?latitude=28.4900591&longitude=77.536386&searchFor=tamil,
// this class should be able to deserialize lat/long and optional searchFor from that.
@Data
@NoArgsConstructor
@Slf4j
public class GetRestaurantsRequest {

  Double latitude;
  Double longitude;
  String searchFor;

  public GetRestaurantsRequest(Double latitude, Double longitude, String searchFor) {
    log.info("GetRestaurantsRequest AllArgs {} ", searchFor);
    this.latitude = latitude;
    this.longitude = longitude;
    this.searchFor = searchFor;
  }

  public GetRestaurantsRequest(Double latitude, Double longitude) {
    log.info("GetRestaurantsRequest Lat,Long");
    this.latitude = latitude;
    this.longitude = longitude;
  }

  public boolean isValid() {
    if (this.latitude == null || this.longitude == null) {
      return false;
    }

    // Range: Latitude [0-90], Longitude [0-180]
    return this.latitude >= 0 && this.latitude <= 90 && this.longitude >= 0
        && this.longitude <= 180;
  }

  public boolean hasSearchQuery() {
    if (searchFor == null) {
      return false;
    }
    return !searchFor.equals("");
  }
}
