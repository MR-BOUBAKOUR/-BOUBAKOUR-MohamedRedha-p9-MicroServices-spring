package com.MedilaboSolutions.gateway.security;

import com.MedilaboSolutions.gateway.dto.OAuth2UserInfo;
import com.MedilaboSolutions.gateway.service.UserService;
import com.MedilaboSolutions.gateway.utils.AuthUtil;
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

    @Value("${cors.frontend-error-url}")
    private String frontendErrorUrl;

    private final AuthUtil authUtil;
    private final UserService userService;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuth2UserInfo userInfo = OAuth2UserInfo.fromGoogleAttributes(attributes);
        String username = userInfo.getName();
        String pictureUrl = userInfo.getPictureUrl();

        ServerWebExchange exchange = webFilterExchange.getExchange();

        return userService.findByUsername(username)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(user -> userService.updateUserPicture(user, pictureUrl).thenReturn(user))
                .flatMap(user -> {
                    String role = "ROLE_" + user.getRole();
                    String accessToken = authUtil.generateAccessToken(username, role, user.getUrlPicture());
                    String refreshToken = authUtil.generateRefreshToken(username);

                    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                            .httpOnly(true)
                            .secure(true)
                            .sameSite("Strict")
                            .maxAge(Duration.ofMillis(refreshTokenExpirationMs))
                            .path("/")
                            .build();

                    exchange.getResponse().addCookie(refreshCookie);

                    String redirectUrl = String.format("%s?token=%s&expires=%d",
                            frontendSuccessUrl, accessToken, accessTokenExpirationMs);

                    log.info("OAuth2 authentication success for user: {}", username);

                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(ex -> {
                    log.warn("Unauthorized OAuth2 login attempt for unknown user: {}", username);
                    String redirectUrl = frontendErrorUrl + "?error=oauth2_unknown_user";
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                    exchange.getResponse().getHeaders().setLocation(URI.create(redirectUrl));
                    return exchange.getResponse().setComplete();
                });
    }
}