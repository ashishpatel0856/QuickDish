package com.ashish.QuickDish.dto;
import lombok.*;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String role;



}
