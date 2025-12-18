package com.ashish.QuickDish.aspects;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspects {
    // cross cutting concern logic

    @Before("execution(* com.ashish.QuickDish.service.RestaurantService.getAllRestaurants(..))")
    public void logBeforeMethod(){
        log.info("Before  executing getAllRestaurants method");
    }

}
