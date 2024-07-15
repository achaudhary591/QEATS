package com.crio.qeats.globals;

import lombok.Getter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class GlobalConstants {

  // COMPLETED: CRIO_TASK_MODULE_REDIS
  // The Jedis client for Redis goes through some initialization steps before you can
  // start using it as a cache.
  // Objective:
  // Some methods are empty or partially filled. Make it into a working implementation.
  public static final String REDIS_HOST = "localhost";
  public static final int REDIS_PORT = 6379;

  // Amount of time after which the redis entries should expire.
  public static final int REDIS_ENTRY_EXPIRY_IN_SECONDS = 3600;

  // TIP(MODULE_RABBITMQ): RabbitMQ related configs.
  public static final String EXCHANGE_NAME = "rabbitmq-exchange";
  public static final String QUEUE_NAME = "rabbitmq-queue";
  public static final String ROUTING_KEY = "qeats.postorder";

  @Getter
  private static JedisPool jedisPool;

  /**
   * Initializes the cache to be used in the code.
   * TIP: Look in the direction of `JedisPool`.
   */
  public static void initCache() {
    jedisPool = new JedisPool(
        new JedisPoolConfig(), REDIS_HOST, REDIS_PORT, REDIS_ENTRY_EXPIRY_IN_SECONDS);
  }


  /**
   * Checks is cache is intiailized and available.
   * TIP: This would generally mean checking via {@link JedisPool}
   *
   * @return true / false if cache is available or not.
   */
  public static boolean isCacheAvailable() {
    if (jedisPool == null) {
      return false;
    }

    return !jedisPool.isClosed();
  }

  /**
   * Destroy the cache.
   * TIP: This is useful if cache is stale or while performing tests.
   */
  public static void destroyCache() {
    try {
      jedisPool.getResource().flushAll();
    } catch (JedisConnectionException e) {
      System.out.println("Error");
    }
    jedisPool.destroy();
  }
}
