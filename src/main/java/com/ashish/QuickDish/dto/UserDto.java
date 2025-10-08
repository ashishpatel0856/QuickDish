package com.ashish.QuickDish.dto;
import com.ashish.QuickDish.Entity.Address;
import com.ashish.QuickDish.Entity.enums.Role;
import lombok.*;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String  name;
    private Address address;
    private Set<Role> role;





}
