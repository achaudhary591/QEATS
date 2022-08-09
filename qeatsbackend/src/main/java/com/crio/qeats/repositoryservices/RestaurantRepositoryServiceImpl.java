/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.ItemEntity;
import com.crio.qeats.models.MenuEntity;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.ItemRepository;
import com.crio.qeats.repositories.MenuRepository;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {



  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private MenuRepository menuRepository;

  @Autowired
  private ItemRepository itemRepository;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

    List<Restaurant> restaurants = null;
    if (redisConfiguration.isCacheAvailable()) {
      restaurants = findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
    } else {
      restaurants = findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
    }
    return restaurants;
    // TODO: CRIO_TASK_MODULE_REDIS
    // We want to use cache to speed things up. Write methods that perform the same functionality,
    // but using the cache if it is present and reachable.
    // Remember, you must ensure that if cache is not present, the queries are directed at the
    // database instead.


      //CHECKSTYLE:OFF
      //CHECKSTYLE:ON    
  }
  
  private List<Restaurant> findAllRestaurantsCloseFromDb(Double latitude, Double longitude, LocalTime currentTime,
      Double servingRadiusInKms) {
    ModelMapper modelMapper = modelMapperProvider.get();
    List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();
    List<Restaurant> restaurantList = new ArrayList<Restaurant>();
    for (RestaurantEntity restaurantEntity : restaurantEntities) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
          servingRadiusInKms)) {
        restaurantList.add(modelMapper.map(restaurantEntity, Restaurant.class));
      }
    }
     System.out.println("DB List====================================\n\n");
     System.out.println(restaurantList);
    return restaurantList;
  }

  private List<Restaurant> findAllRestaurantsCloseByFromCache(Double latitude, Double longitude, LocalTime currentTime,
      Double servingRadiusInKms) {
    List<Restaurant> restaurantList = new ArrayList<>();

    GeoLocation geoLocation = new GeoLocation(latitude, longitude);
    GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(), geoLocation.getLongitude(), 7);

    try (Jedis jedis = redisConfiguration.getJedisPool().getResource()) {
      String jsonStringFromCache = jedis.get(geoHash.toBase32());

      if (jsonStringFromCache == null) {
        // Cache needs to be updated.
        String createdJsonString = "";
        try {
          restaurantList = findAllRestaurantsCloseFromDb(geoLocation.getLatitude(),
              geoLocation.getLongitude(), currentTime, servingRadiusInKms);
          createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
        } catch (JsonProcessingException e) {
          e.printStackTrace();
        }

        // Do operations with jedis resource
        jedis.setex(geoHash.toBase32(), GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS,
            createdJsonString);
      } else {
        try {
          restaurantList = new ObjectMapper().readValue(jsonStringFromCache,
              new TypeReference<List<Restaurant>>() {});
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    // System.out.println("Cache List====================================\n\n");
    // System.out.println(restaurantList);

    return restaurantList;
  }  


  private List<Restaurant> getRestaurantListServingItems(Double latitude, Double longitude,
  LocalTime currentTime, Double servingRadiusInKms, List<ItemEntity> itemEntityList) {
List<String> itemIdList = itemEntityList
    .stream()
    .map(ItemEntity::getItemId)
    .collect(Collectors.toList());

Optional<List<MenuEntity>> optionalMenuEntityList
    = menuRepository.findMenusByItemsItemIdIn(itemIdList);
Optional<List<RestaurantEntity>> optionalRestaurantEntityList = Optional.empty();

if (optionalMenuEntityList.isPresent()) {
  List<MenuEntity> menuEntityList = optionalMenuEntityList.get();
  List<String> restaurantIdList = menuEntityList
      .stream()
      .map(MenuEntity::getRestaurantId)
      .collect(Collectors.toList());
  optionalRestaurantEntityList = restaurantRepository
      .findRestaurantsByRestaurantIdIn(restaurantIdList);
}

List<Restaurant> restaurantList = new ArrayList<>();
ModelMapper modelMapper = modelMapperProvider.get();
if (optionalRestaurantEntityList.isPresent()) {
  List<RestaurantEntity> restaurantEntityList = optionalRestaurantEntityList.get();

  List<RestaurantEntity> restaurantEntitiesFiltered = new ArrayList<>();

  for (RestaurantEntity restaurantEntity : restaurantEntityList) {
    if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude,
        servingRadiusInKms)) {
      restaurantEntitiesFiltered.add(restaurantEntity);
    }
  }

  restaurantList = restaurantEntitiesFiltered
      .stream()
      .map(restaurantEntity -> modelMapper.map(restaurantEntity, Restaurant.class))
      .collect(Collectors.toList());
}

return restaurantList;
}


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose names have an exact or partial match with the search query.
  @Override
  public List<Restaurant> findRestaurantsByName(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        ModelMapper modelMapper = modelMapperProvider.get();
        Set<String> restaurantSet = new HashSet<>();
        List<Restaurant> restaurantList = new ArrayList<>();
    
       
        Optional<List<RestaurantEntity>> optionalExactRestaurantEntityList
            = restaurantRepository.findRestaurantsByNameExact(searchString);
    
        if (optionalExactRestaurantEntityList.isPresent()) {
          List<RestaurantEntity> restaurantEntityList = optionalExactRestaurantEntityList.get();
          for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime,
                latitude, longitude, servingRadiusInKms)
                && !restaurantSet.contains(restaurantEntity.getRestaurantId())) {
              restaurantList.add(modelMapper.map(restaurantEntity, Restaurant.class));
              restaurantSet.add(restaurantEntity.getRestaurantId());
            }
          }
        }
    
       
        Optional<List<RestaurantEntity>> optionalInexactRestaurantEntityList
            = restaurantRepository.findRestaurantsByName(searchString);
    
        if (optionalInexactRestaurantEntityList.isPresent()) {
          List<RestaurantEntity> restaurantEntityList = optionalInexactRestaurantEntityList.get();
          for (RestaurantEntity restaurantEntity : restaurantEntityList) {
            if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime,
                latitude, longitude, servingRadiusInKms)
                && !restaurantSet.contains(restaurantEntity.getRestaurantId())) {
              restaurantList.add(modelMapper.map(restaurantEntity, Restaurant.class));
              restaurantSet.add(restaurantEntity.getRestaurantId());
            }
          }
        }
    
        return restaurantList;
    
  }


  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants whose attributes (cuisines) intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByAttributes(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        List<Pattern> patterns = Arrays
        .stream(searchString.split(" "))
        .map(attr -> Pattern.compile(attr, Pattern.CASE_INSENSITIVE))
        .collect(Collectors.toList());
    Query query = new Query();
    for (Pattern pattern : patterns) {
      query.addCriteria(
          Criteria.where("attributes").regex(pattern)
      );
    }

    List<RestaurantEntity> restaurantEntityList
        = mongoTemplate.find(query, RestaurantEntity.class);
    List<Restaurant> restaurantList = new ArrayList<>();
    ModelMapper modelMapper = modelMapperProvider.get();

    for (RestaurantEntity restaurantEntity : restaurantEntityList) {
      if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime,
          latitude, longitude, servingRadiusInKms)) {
        restaurantList.add(modelMapper.map(restaurantEntity, Restaurant.class));
      }
    }

    return restaurantList;

  }



  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose names form a complete or partial match
  // with the search query.

  @Override
  public List<Restaurant> findRestaurantsByItemName(
      Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {
        String regex = String.join("|", Arrays.asList(searchString.split(" ")));
        Optional<List<ItemEntity>> optionalExactItems
            = itemRepository.findItemsByNameExact(searchString);
        Optional<List<ItemEntity>> optionalInexactItems
            = itemRepository.findItemsByNameInexact(regex);
    
        List<ItemEntity> itemEntityList = optionalExactItems.orElseGet(ArrayList::new);
        List<ItemEntity> inexactItemEntityList = optionalInexactItems.orElseGet(ArrayList::new);
        itemEntityList.addAll(inexactItemEntityList);
    
        return getRestaurantListServingItems(latitude, longitude, currentTime, servingRadiusInKms,
            itemEntityList);
  }

  // TODO: CRIO_TASK_MODULE_RESTAURANTSEARCH
  // Objective:
  // Find restaurants which serve food items whose attributes intersect with the search query.
  @Override
  public List<Restaurant> findRestaurantsByItemAttributes(Double latitude, Double longitude,
      String searchString, LocalTime currentTime, Double servingRadiusInKms) {

        List<Pattern> patterns = Arrays
        .stream(searchString.split(" "))
        .map(attr -> Pattern.compile(attr, Pattern.CASE_INSENSITIVE))
        .collect(Collectors.toList());
    Query query = new Query();
    for (Pattern pattern : patterns) {
      query.addCriteria(
          Criteria.where("attributes").regex(pattern)
      );
    }

    List<ItemEntity> itemEntityList = mongoTemplate.find(query, ItemEntity.class);

    return getRestaurantListServingItems(latitude, longitude, currentTime, servingRadiusInKms,
        itemEntityList);

  }





  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}

