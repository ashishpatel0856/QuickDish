package com.ashish.QuickDish.Entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class Address {


    private String street;
    private String city;
    private String state;
    private String pinCode;


}
