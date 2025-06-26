package com.MedilaboSolutions.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2Response {
    private String accessToken;
    private long expiresIn;
    private OAuth2UserInfo userInfo;
}