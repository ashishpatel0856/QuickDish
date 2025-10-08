package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Cart;
import com.ashish.QuickDish.Entity.CartItem;
import com.ashish.QuickDish.Entity.FoodItem;
import com.ashish.QuickDish.Entity.User;
import com.ashish.QuickDish.dto.CartItemResponseDto;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.CartItemRepository;
import com.ashish.QuickDish.repository.CartRepository;
import com.ashish.QuickDish.repository.FoodRepository;
import com.ashish.QuickDish.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {
    private final CartItemRepository cartItemRepository;
    private final FoodRepository foodRepository;
    private final ModelMapper modelMapper;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final  UserService userService;

    private static final Logger log = Logger.getLogger(CartItemServiceImpl.class.getName());

    @Override
    @Transactional
    public CartItemResponseDto addToCart(CartItemResponseDto cartItemResponseDto, Long userId) {
        log.info("add cartitem for food");

    FoodItem foodItem = foodRepository.findById(cartItemResponseDto.getFoodItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with food id"));


        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        CartItem cartItem = new CartItem();
        cartItem.setFoodItem(foodItem);
        cartItem.setQuantity(cartItemResponseDto.getQuantity());
        cartItem.setCart(cart);
        cartItem.setUserId(userId);
        cartItem.setUnitPrice(foodItem.getPrice());
        cartItem.setTotalPrice(foodItem.getPrice() * cartItem.getQuantity());
        CartItem savedCartItem = cartItemRepository.save(cartItem);


        CartItemResponseDto responseDto = new CartItemResponseDto();
        responseDto.setId(savedCartItem.getId());
        responseDto.setFoodItemId(foodItem.getId());
        responseDto.setCartId(cart.getId());
        responseDto.setFoodName(foodItem.getName());
        responseDto.setUnitPrice(foodItem.getPrice());
        responseDto.setQuantity(savedCartItem.getQuantity());
        responseDto.setTotalPrice(foodItem.getPrice() * savedCartItem.getQuantity());
        return responseDto;
    }


    @Override
    public CartItemResponseDto updateCartItemQuantity(Long cartItemId, int quantity) {
        log.info("update the cartitem quantity");
        CartItem cartitem = cartItemRepository.findById(cartItemId).orElseThrow(
                ()->new ResourceNotFoundException("cartitem are not found with cartitem id"));

//        User user = getCurrentUser();
//      if (cartitem.getOwner() == null || !cartitem.getOwner().getId().equals(user.getId())) {
//            throw new UnAuthorisedException("YOU ARE NOT UNAUTHORIZED PERSON");
//        }
        cartitem.setQuantity(quantity);
        double unitPrice = cartitem.getFoodItem().getPrice();
        cartitem.setUnitPrice(unitPrice);
        cartitem.setCart(cartRepository.findById(cartItemId).get());
        cartitem.setUserId(cartitem.getUserId());
        cartitem.setTotalPrice(unitPrice * quantity);
        CartItem saved = cartItemRepository.save(cartitem);
        return modelMapper.map(saved, CartItemResponseDto.class);
    }

    @Override
    public List<CartItemResponseDto> getAllCartItemsByUser(Long userId) {
        log.info("get all cartitems by user");
        List<CartItem> cartItems = (List<CartItem>) cartItemRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user id not found "));
        return cartItems.stream()
                .map(ele ->modelMapper.map(ele,CartItemResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCartItem(Long cartItemId) {
        log.info("delete cart items by cartItemId");
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(() ->
                new ResourceNotFoundException("cartitem are not found with cartitem id"));
        cartItemRepository.delete(cartItem);


    }
}
