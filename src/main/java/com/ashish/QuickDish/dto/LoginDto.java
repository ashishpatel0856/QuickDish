package com.ashish.QuickDish.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private Long id;
    private String email;
    private String password;

}
