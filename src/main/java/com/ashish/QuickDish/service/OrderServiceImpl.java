package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.*;
import com.ashish.QuickDish.Entity.enums.OrderStatus;
import com.ashish.QuickDish.Entity.enums.Role;
import com.ashish.QuickDish.dto.*;
import com.ashish.QuickDish.exceptions.ResourceNotFoundException;
import com.ashish.QuickDish.exceptions.UnAuthorisedException;
import com.ashish.QuickDish.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ashish.QuickDish.utils.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final RestaurantRepository restaurantRepository;
    private final UserService userService;
    private final FoodRepository foodRepository;
    private final OrderItemRepository orderItemRepository;
    private final CheckoutService checkoutService;

    @Override
    @Transactional
    public OrderResponseDto BookingMyOrders(OrderRequestDto orderRequestDto) {
        log.info(" Booking order for user");

        User user = getCurrentUser();
        Restaurant restaurant = restaurantRepository.findById(orderRequestDto.getRestaurantId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        double calculatedTotal = calculateTotal(orderRequestDto.getOrderItems());

        Order order = new Order();
        order.setOrderDate(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber());
        order.setRestaurant(restaurant);
        order.setCustomer(user);
        order.setTotalPrice(calculatedTotal);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(orderRequestDto.getDeliveryAddress());
        order.setNotes(orderRequestDto.getNotes());
        order.setPaid(false);
        order.setPaymentMethod(orderRequestDto.getPaymentMethod());
        order.setPaymentStatus("PENDING");

        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = createOrderItems(orderRequestDto.getOrderItems(), savedOrder);
        savedOrder.setOrderItems(orderItems);

        Order freshOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Hibernate.initialize(freshOrder.getOrderItems());

        OrderResponseDto response = modelMapper.map(freshOrder, OrderResponseDto.class);

        if ("ONLINE".equalsIgnoreCase(orderRequestDto.getPaymentMethod())) {
            String successUrl = "http://localhost:3000/payment/success?orderId=" + freshOrder.getId();
            String cancelUrl = "http://localhost:3000/payment/failed?orderId=" + freshOrder.getId();

            String checkoutUrl = checkoutService.getCheckoutServiceSession(freshOrder, successUrl, cancelUrl);
            response.setPaymentUrl(checkoutUrl);
        } else {
            freshOrder.setStatus(OrderStatus.CONFIRMED);
            freshOrder.setConfirmedAt(LocalDateTime.now());
            orderRepository.save(freshOrder);
            response.setStatus(OrderStatus.CONFIRMED);
        }

        return response;
    }

    @Override
    @Transactional
    public void markOrderAsPaid(String sessionId) {
        log.info("Marking order as paid, session: {}", sessionId);

        Order order = orderRepository.findByPaymentSessionId(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found for session"));

        order.setPaid(true);
        order.setPaymentDate(LocalDateTime.now());
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());

        orderRepository.save(order);
        log.info(" Order {} marked as PAID", order.getOrderNumber());
    }

    @Override
    @Transactional
    public OrderResponseDto updateMyOrdersStatusById(Long orderId, OrderStatus status) {
        log.info("Updating order {} to status {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = getCurrentUser();

        boolean isRestaurantOwner = order.getRestaurant().getOwner() != null &&
                order.getRestaurant().getOwner().getId().equals(user.getId());
        boolean isAdmin = isAdmin(user);

        if (!isRestaurantOwner && !isAdmin) {
            throw new UnAuthorisedException("Only restaurant owner or admin can update status");
        }

        if (!isValidStatusTransition(order.getStatus(), status)) {
            throw new RuntimeException("Invalid status transition from " + order.getStatus() + " to " + status);
        }

        order.setStatus(status);
        setStatusTimestamp(order, status);

        Order updatedOrder = orderRepository.save(order);
        Hibernate.initialize(updatedOrder.getOrderItems());

        return modelMapper.map(updatedOrder, OrderResponseDto.class);
    }

    private boolean isValidStatusTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED || next == OrderStatus.CANCELLED;
            case CONFIRMED -> next == OrderStatus.PREPARING || next == OrderStatus.CANCELLED;
            case PREPARING -> next == OrderStatus.READY_FOR_PICKUP;
            case READY_FOR_PICKUP -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            default -> false;
        };
    }

    private void setStatusTimestamp(Order order, OrderStatus status) {
        LocalDateTime now = LocalDateTime.now();
        switch (status) {
            case CONFIRMED -> order.setConfirmedAt(now);
            case PREPARING -> order.setPreparingAt(now);
            case READY_FOR_PICKUP -> order.setReadyAt(now);
            case OUT_FOR_DELIVERY -> order.setOutForDeliveryAt(now);
            case DELIVERED -> {
                order.setDeliveredAt(now);
                order.setActive(false);
            }
            default -> {}
        }
    }

    @Override
    public List<OrderResponseDto> getMyAllOrders() {
        User user = getCurrentUser();
        List<Order> orders = orderRepository.findAllByCustomer(user);
        orders.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));

        return orders.stream()
                .map(order -> {
                    order.getOrderItems().size();  // Triggers lazy loading
                    return modelMapper.map(order, OrderResponseDto.class);
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponseDto getMyOrdersById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = getCurrentUser();
        if (!order.getCustomer().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new UnAuthorisedException("Access denied");
        }

        Hibernate.initialize(order.getOrderItems());

        return modelMapper.map(order, OrderResponseDto.class);
    }

    @Override
    @Transactional
    public OrderResponseDto addMyOrders(AddOrderDto addOrderDto) {
        User user = getCurrentUser();
        Order order = orderRepository.findById(addOrderDto.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getCustomer().getId().equals(user.getId())) {
            throw new UnAuthorisedException("Not your order");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new RuntimeException("Cannot modify order - already being prepared");
        }

        List<OrderItem> newItems = createOrderItems(addOrderDto.getItems(), order);
        order.getOrderItems().addAll(newItems);

        double newTotal = order.getOrderItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(newTotal);

        if (order.isPaid()) {
            order.setPaid(false);
            order.setAdditionalAmount(newTotal - order.getTotalPrice());
            order.setPaymentStatus("ADDITIONAL_PAYMENT_PENDING");
        }

        Order saved = orderRepository.save(order);
        Hibernate.initialize(saved.getOrderItems());

        return modelMapper.map(saved, OrderResponseDto.class);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Override
    @Transactional
    public OrderResponseDto markOrderAsPaid(Long orderId) {
        log.info("Force marking order {} as paid", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaid(true);
        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        order.setPaymentDate(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        Hibernate.initialize(saved.getOrderItems());

        return modelMapper.map(saved, OrderResponseDto.class);
    }


    private double calculateTotal(List<OrderItemRequestDto> items) {
        return items.stream().mapToDouble(item -> {
            FoodItem food = foodRepository.findById(item.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food not found"));
            return food.getPrice() * item.getQuantity();
        }).sum();
    }

    private List<OrderItem> createOrderItems(List<OrderItemRequestDto> itemDtos, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequestDto itemDto : itemDtos) {
            FoodItem foodItem = foodRepository.findById(itemDto.getFoodItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Food item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setFoodItem(foodItem);
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setPrice(foodItem.getPrice());
            orderItem.setTotalPrice(foodItem.getPrice() * itemDto.getQuantity());
            orderItem.setOrder(order);

            orderItems.add(orderItem);
        }

        return orderItemRepository.saveAll(orderItems);
    }

    private String generateOrderNumber() {
        return "QD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isAdmin(User user) {
        return user.getRoles() != null && user.getRoles().contains(Role.ROLE_ADMIN);
    }



    public List<Order> getRestaurantOrders(Long restaurantId, String status) {
        if (status != null && !status.isEmpty()) {
            OrderStatus orderStatus = OrderStatus.valueOf(status);
            return orderRepository.findByRestaurantIdAndStatus(restaurantId, orderStatus);
        }
        return orderRepository.findByRestaurantId(restaurantId);
    }

    //  Update order status
    @Transactional
    public Order updateStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(newStatus);

        // Set timestamps based on status
        switch (newStatus) {
            case CONFIRMED -> order.setConfirmedAt(LocalDateTime.now());
            case PREPARING -> order.setPreparingAt(LocalDateTime.now());
            case READY_FOR_PICKUP -> order.setReadyAt(LocalDateTime.now());
            case OUT_FOR_DELIVERY -> order.setOutForDeliveryAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
        }

        return orderRepository.save(order);
    }

    //  Accept order (PENDING -> CONFIRMED)
    @Transactional
    public Order acceptOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    // Reject order with reason
    @Transactional
    public Order rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in PENDING status");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setNotes((order.getNotes() != null ? order.getNotes() : "") + " [Rejected: " + reason + "]");
        return orderRepository.save(order);
    }
}