package Bright.BeSafeProject.controller.api;

import Bright.BeSafeProject.component.JwtComponent;
import Bright.BeSafeProject.dto.*;
import Bright.BeSafeProject.dto.apiRequest.TmapRouteRequestDTO;
import Bright.BeSafeProject.service.*;
import Bright.BeSafeProject.vo.AccountRoleEnum;
import Bright.BeSafeProject.vo.PlatformEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RequestMapping("/besafe/api")
@RestController
@RequiredArgsConstructor
public class ApiController {
    private final JwtComponent jwtComponent;
    private final AccountService accountService;
    private final DataService dataService;
    private final ExternalApiService externalApiService;

    @PostMapping("/join")
    public Mono<ResponseEntity<String>> join(@RequestBody LocalSignInDTO signInDTO){
        return accountService.join(
                    PlatformEnum.LOCAL.name(),
                    signInDTO.name(),
                    signInDTO.emailAndPassword().insertedEmail(),
                    signInDTO.emailAndPassword().insertedPassword(),
                    AccountRoleEnum.ROLE_USER.name()
                )
                .thenReturn(ResponseEntity.ok("가입 성공"))
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity
                                .badRequest()
                                .body(e.getMessage()))
                );
    }

    @PostMapping("/logIn")
    public Mono<ResponseEntity<String>> localLogIn(ServerHttpResponse response,
                                 @RequestBody LocalLogInDTO localLogInDTO){
        return accountService.logInLocal(localLogInDTO)
                .flatMap(authentication -> {
                    JwtDTO jwtDTO = jwtComponent.generateToken(authentication);
                    return Mono.fromSupplier(() -> ResponseEntity.ok()
                            .header(HttpHeaders.SET_COOKIE, jwtDTO.accessTokenCookie().toString())
                            .header(HttpHeaders.SET_COOKIE, jwtDTO.refreshTokenCookie().toString())
                            .body("로그인 성공")
                    );
                })
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity
                                .badRequest()
                                .body(e.getMessage()))
                );
    }

    @PostMapping("/logOut")
    public Mono<ResponseEntity<String>> logOut(ServerHttpRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = ((UserDetails) authentication.getPrincipal()).getUsername();
                    JwtDTO jwtDTO = jwtComponent.extractAccessTokenCookie(request);

                    return jwtComponent.discardToken(
                            jwtDTO.accessTokenString(),
                            jwtDTO.refreshTokenString(),
                            email);
                })
                .map(jwtDTO -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, jwtDTO.accessTokenCookie().toString())
                        .header(HttpHeaders.SET_COOKIE, jwtDTO.refreshTokenCookie().toString())
                        .body("로그아웃 완료")
                );
    }

    @PostMapping("/deleteAccount")
    public Mono<ResponseEntity<String>> deleteAccount(ServerHttpRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = ((UserDetails) authentication.getPrincipal()).getUsername();
                    JwtDTO jwtDTO = jwtComponent.extractAccessTokenCookie(request);

                    accountService.deleteAccount(email);
                    return jwtComponent.discardToken(
                            jwtDTO.accessTokenString(),
                            jwtDTO.refreshTokenString(),
                            email);
                })
                .map(jwtDTO -> ResponseEntity.ok()
                        .header(HttpHeaders.SET_COOKIE, jwtDTO.accessTokenCookie().toString())
                        .header(HttpHeaders.SET_COOKIE, jwtDTO.refreshTokenCookie().toString())
                        .body("회원 탈퇴 완료")
                );
    }

    @GetMapping("/getUserInfo")
    public Mono<ProfileDTO> getUserInfo(ServerHttpRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = ((UserDetails) authentication.getPrincipal()).getUsername();

                    return accountService.findAccountForUser(email);
                });
    }

    @GetMapping("/getCurrentUsage")
    public Mono<List<HistoryDTO>> getCurrentUsage(ServerHttpRequest request,
                                                  @RequestParam int page,
                                                  @RequestParam int size){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = ((UserDetails) authentication.getPrincipal()).getUsername();

                    return accountService.loadAccount(email)
                            .flatMap(accountDTO ->
                                    dataService.getUsageList(
                                            accountDTO.id(),
                                            page,
                                            size
                                    ));
                });
    }

    @PostMapping("/getSafeRoute")
    public Mono<SafeRouteDTO> getSafeRoute(ServerHttpRequest request,
                                           @RequestBody TmapRouteRequestDTO requestDTO){
        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .flatMap(authentication -> {
                    String email = ((UserDetails) authentication.getPrincipal()).getUsername();

                    return externalApiService.callTmapRoute(requestDTO)
                            .flatMap(routeNodes ->
                                accountService.loadAccount(email)
                                        .flatMap(accountDTO ->
                                                dataService.saveServiceUsageHistory(
                                                        accountDTO.id(),
                                                        requestDTO.startX(),
                                                        requestDTO.startY(),
                                                        requestDTO.endX(),
                                                        requestDTO.endY()
                                                ).thenReturn(accountDTO)
                                        )
                                        .flatMap(accountDTO ->
                                                dataService.findLightNodeNearPath(routeNodes)
                                                        .map(lightNodes -> new SafeRouteDTO(routeNodes,lightNodes))
                                        )
                            );
                });
    }
}
