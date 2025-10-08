package com.ashish.QuickDish.dto;

import com.ashish.QuickDish.Entity.Address;
import com.ashish.QuickDish.Entity.User;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RestaurantDto {
    private Long id;
    private String name;
    private String email;
    private String contact;
    private String description;
    private String category;
    private String location;
    private String[] image;
    private boolean approved;
    private Address address;
   private UserDto Owner;
   private Double longitude;
   private Double latitude;
}
