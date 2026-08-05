package com.ashish.QuickDish.service;

import com.ashish.QuickDish.Entity.Order;

public interface CheckoutService {

String getCheckoutServiceSession(Order order, String successUrl, String failureUrl);

}
