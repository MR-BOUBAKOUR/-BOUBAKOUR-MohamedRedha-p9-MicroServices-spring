package com.MedilaboSolutions.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    private String email;
    private String name;
    private String pictureUrl;

    public static OAuth2UserInfo fromGoogleAttributes(Map<String, Object> attributes) {
        OAuth2UserInfo userInfo = new OAuth2UserInfo();
        userInfo.setEmail((String) attributes.get("email"));
        userInfo.setName((String) attributes.get("name"));
        userInfo.setPictureUrl((String) attributes.get("picture"));
        return userInfo;
    }
}