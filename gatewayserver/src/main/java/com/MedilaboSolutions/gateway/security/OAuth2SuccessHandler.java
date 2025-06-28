package com.MedilaboSolutions.gateway.security;

import com.MedilaboSolutions.gateway.dto.OAuth2UserInfo;
import com.MedilaboSolutions.gateway.service.UserService;
import com.MedilaboSolutions.gateway.utils.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements ServerAuthenticationSuccessHandler {

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    @Value("${cors.frontend-success-url}")
    private String frontendSuccessUrl;

    private final AuthUtil authUtil;
    private final UserService userService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        return Mono
                .fromCallable(() -> {
                    OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                    Map<String, Object> attributes = oauth2User.getAttributes();

                    log.info("OAuth2 authentication success for user: {}", attributes.get("name"));

                    // Extract user info from Google attributes
                    OAuth2UserInfo userInfo = OAuth2UserInfo.fromGoogleAttributes(attributes);

                    String email = userInfo.getEmail();
                    String username = userInfo.getName();
                    String pictureUrl = userInfo.getPictureUrl();
                    String role = "ROLE_MEDECIN";

                    userService.findByUsername(username).ifPresent(user ->
                            userService.updateUserPicture(user, pictureUrl));

                    // Generate JWT tokens
                    String accessToken = authUtil.generateAccessToken(username, role, pictureUrl);
                    String refreshToken = authUtil.generateRefreshToken(username);

                    // Create the refresh token cookie
                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .secure(true)              // ⚠️ Should be true in production (HTTPS)
                            .sameSite("None")
                            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                            .path("/")
                            .build();

                    ServerWebExchange exchange = webFilterExchange.getExchange();
                    exchange.getResponse().addCookie(refreshCookie);

                    // Redirect to frontend with access token and expiration
                    String redirectUrl = String.format("%s?token=%s&expires=%d",
                            frontendSuccessUrl, accessToken, accessTokenExpirationMs);

                    exchange.getResponse()
                            .setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders()
                            .setLocation(URI.create(redirectUrl));

                    return null;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then(webFilterExchange.getExchange().getResponse().setComplete())
                .doOnError(error -> log.error("Error in OAuth2 success handler", error));
    }
}