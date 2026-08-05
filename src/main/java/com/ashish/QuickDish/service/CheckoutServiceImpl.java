package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Order;
import com.ashish.QuickDish.Entity.OrderItem;
import com.ashish.QuickDish.repository.OrderRepository;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository orderRepository;

    @Override
    public String getCheckoutServiceSession(Order order, String successUrl, String cancelUrl) {
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(order.getId().toString());

            for (OrderItem item : order.getOrderItems()) {
                String foodName = item.getFoodItem().getName();
                String foodDesc = item.getFoodItem().getDescription();
                String imageUrl = item.getFoodItem().getImageUrl();

                SessionCreateParams.LineItem.PriceData.ProductData.Builder productBuilder =
                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                .setName(foodName != null ? foodName : "Food Item");

                if (foodDesc != null && !foodDesc.isEmpty()) {
                    productBuilder.setDescription(foodDesc);
                }

                if (imageUrl != null && !imageUrl.isEmpty()
                        && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
                    productBuilder.addImage(imageUrl);
                }

                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity((long) item.getQuantity())
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("inr")
                                                .setUnitAmount(BigDecimal.valueOf(item.getPrice())
                                                        .multiply(BigDecimal.valueOf(100)).longValue())
                                                .setProductData(productBuilder.build())
                                                .build()
                                )
                                .build()
                );
            }

            Session session = Session.create(paramsBuilder.build());

            order.setPaymentSessionId(session.getId());
            order.setPaymentStatus("PENDING");
            orderRepository.save(order);

            return session.getUrl();

        } catch (Exception ex) {
            throw new RuntimeException("Failed to create checkout session: " + ex.getMessage(), ex);
        }
    }
}