package Bright.BeSafeProject.service;

import Bright.BeSafeProject.dto.*;
import Bright.BeSafeProject.entity.Account;
import Bright.BeSafeProject.exception.CustomException;
import Bright.BeSafeProject.exception.ErrorCode;
import Bright.BeSafeProject.mapper.AccountMapper;
import Bright.BeSafeProject.repository.AccountRepository;
import Bright.BeSafeProject.vo.PlatformEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Mono<Void> join(String platform,
                             String name,
                             String email,
                             String password,
                             String authority){
        if(PlatformEnum.LOCAL.name().equals(platform)){
            if(password.isEmpty()) {
                log.error(ErrorCode.BLANK_PASSWORD.getErrorMessage());
                return Mono.error(new CustomException(ErrorCode.BLANK_PASSWORD));
            }
            return isExistsAccount(email)
                    .flatMap(exists -> {
                        if(exists) {
                            log.error(ErrorCode.ACCOUNT_ALREADY_EXISTS.getErrorMessage());
                            return Mono.error(new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS));
                        }
                        Account account = Account.builder()
                                .platform(Objects.requireNonNull(platform))
                                .name(Objects.requireNonNull(name))
                                .email(Objects.requireNonNull(email))
                                .password(passwordEncoder().encode(Objects.requireNonNull(password)))
                                .authority(Objects.requireNonNull(authority))
                                .createdAt(LocalDateTime.now())
                                .build();

                        return accountRepository.save(account)
                                .onErrorResume(e -> {
                                    log.error(e.getMessage());
                                    return Mono.error(new CustomException(ErrorCode.ACCOUNT_SAVE_FAILED));
                                })
                                .then();
                    });
        } else {
            Account account = Account.builder()
                    .platform(Objects.requireNonNull(platform))
                    .name(Objects.requireNonNull(name))
                    .email(Objects.requireNonNull(email))
                    .password(null)
                    .authority(Objects.requireNonNull(authority))
                    .createdAt(LocalDateTime.now())
                    .build();

            return accountRepository.save(account)
                    .onErrorResume(e -> {
                        log.error(e.getMessage());
                        return Mono.error(new CustomException(ErrorCode.ACCOUNT_SAVE_FAILED));
                    })
                    .then();
        }
    }

    public Mono<Authentication> logInLocal(LocalLogInDTO logInInfo){
        if(logInInfo.insertedPassword().isBlank()) {
            log.error(ErrorCode.BLANK_PASSWORD.getErrorMessage());
            return Mono.error(new CustomException(ErrorCode.BLANK_PASSWORD));
        }

        return loadAccount(logInInfo.insertedEmail())
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(ErrorCode.ACCOUNT_NOT_FOUND.getErrorMessage());
                    return Mono.error(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
                }))
                .flatMap(simpleAccountDTO -> {
                    if(!passwordEncoder().matches(logInInfo.insertedPassword(), simpleAccountDTO.password())){
                        log.error(ErrorCode.WRONG_PASSWORD.getErrorMessage());
                        return Mono.error(new CustomException(ErrorCode.WRONG_PASSWORD));
                    }

                    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(simpleAccountDTO.authority()));
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            simpleAccountDTO.email(),
                            simpleAccountDTO.password(),
                            authorities
                    );

                    return Mono.just(authentication);
                });
    }

    public Mono<ProfileDTO> findAccountForUser(String email){
        return loadAccount(email)
                .map(accountDTO -> new ProfileDTO(
                        accountDTO.platform(),
                        accountDTO.name(),
                        accountDTO.email()
                ));
    }

    public Mono<AccountDTO> loadAccount(String email){
        return accountRepository.findByEmail(email)
                .switchIfEmpty(Mono.defer(() -> {
                    log.error(ErrorCode.ACCOUNT_NOT_FOUND.getErrorMessage());
                    return Mono.error(new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
                }))
                .map(AccountMapper.INSTANCE::toDto);
    }

    public Mono<Boolean> isExistsAccount(String email){
        return accountRepository.existsByEmail(email);
    }

    public Mono<Void> deleteAccount(String email){
        return accountRepository.deleteByEmail(email)
                .handle((result, sink) -> {
                    if (result <= 0) {
                        log.error(ErrorCode.ACCOUNT_DELETE_FAILED.getErrorMessage());
                        sink.error(new CustomException(ErrorCode.ACCOUNT_DELETE_FAILED));
                    }
                });
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
