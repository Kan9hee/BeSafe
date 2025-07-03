package Bright.BeSafeProject.component;

import Bright.BeSafeProject.config.JwtConfig;
import Bright.BeSafeProject.dto.JwtDTO;
import Bright.BeSafeProject.exception.CustomException;
import Bright.BeSafeProject.exception.ErrorCode;
import Bright.BeSafeProject.service.CustomUserDetailsService;
import Bright.BeSafeProject.service.TokenManageService;
import Bright.BeSafeProject.vo.SecuredPathEnum;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.RequestPath;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.pattern.PathPatternParser;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtComponent implements WebFilter {
    private final JwtConfig jwtConfig;
    private final TokenManageService tokenManageService;
    private final CustomUserDetailsService customUserDetailsService;
    private final PathPatternParser pathParser;

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange,
                             @NotNull WebFilterChain chain) {
        RequestPath requestURI = exchange.getRequest().getPath();
        boolean isSecured = SecuredPathEnum.allPaths()
                .stream()
                .anyMatch(path -> pathParser.parse(path)
                        .matches(requestURI)
                );
        if(!isSecured)
            return chain.filter(exchange);

        String accessToken = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(jwtConfig.getAccessTokenName()))
                .map(HttpCookie::getValue)
                .orElse(null);
        String refreshToken = Optional.ofNullable(exchange.getRequest().getCookies().getFirst(jwtConfig.getRefreshTokenName()))
                .map(HttpCookie::getValue)
                .orElse(null);

        if(accessToken == null || refreshToken == null) {
            log.error(ErrorCode.TOKEN_NOT_FOUND.getErrorMessage());
            return Mono.error(new CustomException(ErrorCode.TOKEN_NOT_FOUND));
        }

        String email;
        try {
            email = getEmailFromAccessToken(accessToken);
        } catch (Exception e) {
            log.error(ErrorCode.EMAIL_INFO_MISSING_TOKEN.getErrorMessage());
            return Mono.error(new CustomException(ErrorCode.EMAIL_INFO_MISSING_TOKEN));
        }

        Mono<String> accessTokenMono = isExpired(accessToken)
                ? reissueAccessToken(accessToken, refreshToken, email).map(JwtDTO::accessTokenString)
                : Mono.just(accessToken);

        return accessTokenMono
                .flatMap(token -> tokenManageService.checkTokenBlacklisted(token)
                        .flatMap(isBlacklisted -> {
                            if(isBlacklisted) {
                                log.error(ErrorCode.EXPIRED_TOKEN.getErrorMessage());
                                return Mono.error(new CustomException(ErrorCode.EXPIRED_TOKEN));
                            }
                            return customUserDetailsService.findByUsername(email)
                                    .switchIfEmpty(Mono.defer(() -> {
                                        log.error(ErrorCode.ACCOUNT_NOT_FOUND.getErrorMessage());
                                        return Mono.error(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
                                    }))
                                    .flatMap(userDetails -> {
                                        Authentication authentication = new UsernamePasswordAuthenticationToken(
                                                userDetails,null,userDetails.getAuthorities());
                                        return chain.filter(exchange)
                                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                                    });
                        }));
    }

    @NotNull
    public JwtDTO generateToken(Authentication authentication){
        Date now = new Date();

        String accessToken = Jwts.builder()
                .subject(authentication.getName())
                .issuedAt(now)
                .expiration(new Date(now.getTime()+jwtConfig.getAccessTokenExpiration()))
                .signWith(jwtConfig.getSecret())
                .compact();

        String refreshToken = Jwts.builder()
                .expiration(new Date(now.getTime()+jwtConfig.getRefreshTokenExpiration()))
                .signWith(jwtConfig.getSecret())
                .compact();

        ResponseCookie accessTokenCookie = ResponseCookie.from(jwtConfig.getAccessTokenName(), accessToken)
                .httpOnly(true)
                .secure(false)
                .path(jwtConfig.getPath())
                .sameSite(jwtConfig.getCookieSameSite())
                .maxAge(Duration.ofMinutes(jwtConfig.getAccessTokenCookieExpirationMinutes()))
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from(jwtConfig.getRefreshTokenName(), refreshToken)
                .httpOnly(true)
                .secure(false)
                .path(jwtConfig.getPath())
                .sameSite(jwtConfig.getCookieSameSite())
                .maxAge(Duration.ofDays(jwtConfig.getRefreshTokenCookieExpirationDays()))
                .build();

        return new JwtDTO(accessToken, refreshToken, accessTokenCookie, refreshTokenCookie);
    }

    public Mono<JwtDTO> reissueAccessToken(String deprecatedAccessToken,
                                           String refreshToken,
                                           String userEmail) {
        return tokenManageService.checkTokenBlacklisted(refreshToken)
                .flatMap(isBlacklisted -> {
                    if(isBlacklisted || isExpired(refreshToken)) {
                        log.error(ErrorCode.EXPIRED_TOKEN.getErrorMessage());
                        return Mono.error(new CustomException(ErrorCode.EXPIRED_TOKEN));
                    }
                    return null;
                })
                .then( customUserDetailsService.findByUsername(userEmail)
                                .flatMap(userDetails -> {
                                    Authentication authentication =
                                            new UsernamePasswordAuthenticationToken(userDetails,userDetails.getPassword());

                                    JwtDTO newToken = generateToken(authentication);
                                    return discardToken(deprecatedAccessToken, refreshToken,userEmail)
                                            .then(tokenManageService.saveRefreshToken(
                                                    newToken.refreshTokenString(),
                                                    userDetails.getUsername()
                                            ))
                                            .thenReturn(newToken);
                                })
                );
    }

    public Mono<JwtDTO> discardToken(String accessToken,
                                   String refreshToken,
                                   String accountEmail) {
        return tokenManageService.saveBlacklistToken(accessToken)
                .then(Mono.defer(() -> {
                    if(refreshToken == null || refreshToken.isBlank())
                        return Mono.empty();
                    return tokenManageService.saveBlacklistToken(refreshToken)
                            .then(tokenManageService.removeRefreshToken(accountEmail));
                }))
                .then(Mono.defer(() -> {
                    ResponseCookie deleteAccessTokenCookie = ResponseCookie.from(jwtConfig.getAccessTokenName(), accessToken)
                            .httpOnly(true)
                            .secure(false)
                            .path(jwtConfig.getPath())
                            .sameSite(jwtConfig.getCookieSameSite())
                            .maxAge(Duration.ofSeconds(jwtConfig.getDiscardTime()))
                            .build();

                    ResponseCookie deleteRefreshTokenCookie = ResponseCookie.from(jwtConfig.getRefreshTokenName(), refreshToken)
                            .httpOnly(true)
                            .secure(false)
                            .path(jwtConfig.getPath())
                            .sameSite(jwtConfig.getCookieSameSite())
                            .maxAge(Duration.ofSeconds(jwtConfig.getDiscardTime()))
                            .build();

                    return Mono.just(new JwtDTO(
                            null,
                            null,
                            deleteAccessTokenCookie,
                            deleteRefreshTokenCookie));
                }));
    }

    public boolean isExpired(String tokenString){
        Claims claims = Jwts.parser()
                .verifyWith(jwtConfig.getSecret())
                .build()
                .parseSignedClaims(tokenString)
                .getPayload();

        Date expirationDate = claims.getExpiration();
        return expirationDate.before(new Date());
    }

    public JwtDTO extractAccessTokenCookie(ServerHttpRequest request) {
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        String accessTokenString = Objects.requireNonNull(cookies.getFirst(jwtConfig.getAccessTokenName())).getValue();
        String refreshTokenString = Objects.requireNonNull(cookies.getFirst(jwtConfig.getRefreshTokenName())).getValue();

        return new JwtDTO(accessTokenString,refreshTokenString,null,null);
    }

    public String getEmailFromAccessToken(String accessToken){
        return Jwts.parser()
                .verifyWith(jwtConfig.getSecret())
                .build()
                .parseSignedClaims(accessToken)
                .getPayload()
                .getSubject();
    }
}
