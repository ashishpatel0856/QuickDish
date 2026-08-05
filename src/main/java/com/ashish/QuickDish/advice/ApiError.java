package com.ashish.QuickDish.advice;

import lombok.*;
import org.springframework.http.HttpStatus;


import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private HttpStatus status;
    private String message;
    private List<String> subErrors;

}
