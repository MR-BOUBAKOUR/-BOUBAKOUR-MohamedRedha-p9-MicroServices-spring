package com.MedilaboSolutions.gateway.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private long expiresIn;

    private String email;
    private String username;
    private String pictureUrl;
}