package com.ashish.QuickDish.controller;

import com.ashish.QuickDish.advice.ApiResponse;
import com.ashish.QuickDish.service.OrderService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@Slf4j
public class WebHookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final OrderService orderService;

    public WebHookController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> capturePayments(
            @RequestBody(required = false) String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signature) {

        try {
            if (payload == null || signature == null) {
                log.error("Missing payload or signature");
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>("Missing payload or Stripe-Signature"));
            }

            Event event = Webhook.constructEvent(payload, signature, endpointSecret);
            log.info(" Webhook received: {}", event.getType());

            switch (event.getType()) {
                case "checkout.session.completed" -> {
                    Session session = (Session) event.getDataObjectDeserializer()
                            .getObject().orElse(null);

                    if (session != null) {
                        log.info(" Payment completed for session: {}", session.getId());
                        orderService.markOrderAsPaid(session.getId());
                    }
                }
                case "checkout.session.expired" -> {
                    log.warn(" Payment expired");
                }
                case "payment_intent.payment_failed" -> {
                log.error(" Payment failed");
                }
            }

            return ResponseEntity.ok(new ApiResponse<>("Webhook processed"));

        } catch (Exception e) {
            log.error("Webhook error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("Webhook error: " + e.getMessage()));
        }
    }
}