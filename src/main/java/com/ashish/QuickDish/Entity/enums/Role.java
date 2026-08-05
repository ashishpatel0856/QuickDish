package com.ashish.QuickDish.Entity.enums;

public enum Role {
    ROLE_CUSTOMER,
    ROLE_RESTAURANT_OWNER,
    ROLE_ADMIN,
    ROLE_USER,
    ROLE_RIDER;

    public String getName() {
        return this.name();
    }
}
