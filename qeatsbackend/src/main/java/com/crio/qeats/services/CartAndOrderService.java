package com.crio.qeats.services;

import com.crio.qeats.dto.Cart;
import com.crio.qeats.dto.Order;
import com.crio.qeats.exceptions.EmptyCartException;
import com.crio.qeats.exceptions.ItemNotFromSameRestaurantException;
import com.crio.qeats.exceptions.UserNotFoundException;
import com.crio.qeats.exchanges.CartModifiedResponse;

public interface CartAndOrderService {

  /**
   * Success Output:
   * 1. If userId is present return user's cart
   *    - If user has an active cart, then return it
   *    - otherwise return an empty cart
   * 2. If userId is not present then respond with BadHttpRequest.
   * @param userId id of the user
   * @throws UserNotFoundException - when no user by this id is found
   */
  Cart findOrCreateCart(String userId) throws UserNotFoundException;

  /**
   * COMPLETED: CRIO_TASK_MODULE_MENUAPI - Implement addItemToCart.
   * Add item to the given cart.
   *  - All items added should be from same restaurant
   *  - If the above constraint is not satisfied, throw ItemNotFromSameRestaurantException exception
   * @param itemId - id of the item to be added
   * @param cartId - id of the cart where item should be added
   * @param restaurantId - id of the restaurant where the given item comes from
   * @return - return - CartModifiedResponse
   * @throws ItemNotFromSameRestaurantException - when item to be added comes from
   *     different restaurant. You should set cartResponseType to 102(ITEM_NOT_FROM_SAME_RESTAURANT)
   */
  CartModifiedResponse addItemToCart(String itemId, String cartId, String restaurantId)
      throws ItemNotFromSameRestaurantException;

  /**
   * COMPLETED: CRIO_TASK_MODULE_MENUAPI - Implement removeItemFromCart.
   * Remove item from the given cart.
   * @param itemId - id of the item to be removed
   * @param cartId - id of the cart where item should be removed
   * @param restaurantId - id of the restaurant where the given item comes from
   * @return - return - CartModifiedResponse, set cartResponseType to 0
   */
  CartModifiedResponse removeItemFromCart(String itemId, String cartId, String restaurantId);

  /**
   * COMPLETED: CRIO_TASK_MODULE_MENUAPI - Implement postOrder.
   * Place order for the given cart
   * @param cartId - id of the cart to be converted to order
   * @return Order - return the order that was just placed
   * @throws EmptyCartException - should throw this exception when cart is empty
   */
  Order postOrder(String cartId) throws EmptyCartException;


}