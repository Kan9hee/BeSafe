package Bright.BeSafeProject.service;

import Bright.BeSafeProject.dto.*;
import Bright.BeSafeProject.entity.Account;
import Bright.BeSafeProject.mapper.AccountMapper;
import Bright.BeSafeProject.repository.AccountRepository;
import Bright.BeSafeProject.vo.PlatformEnum;
import lombok.RequiredArgsConstructor;
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
            if(password.isEmpty())
                return Mono.error(new IllegalArgumentException("비밀번호가 없습니다."));
            return isExistsAccount(email)
                    .flatMap(exists -> {
                        if(exists)
                            return Mono.error(new IllegalArgumentException("이미 있는 사용자입니다."));
                        Account account = Account.builder()
                                .platform(Objects.requireNonNull(platform))
                                .name(Objects.requireNonNull(name))
                                .email(Objects.requireNonNull(email))
                                .password(passwordEncoder().encode(Objects.requireNonNull(password)))
                                .authority(Objects.requireNonNull(authority))
                                .createdAt(LocalDateTime.now())
                                .build();
                        return accountRepository.save(account).then();
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
            return accountRepository.save(account).then();
        }
    }

    public Mono<Authentication> logInLocal(LocalLogInDTO logInInfo){
        if(logInInfo.insertedPassword().isBlank())
            return Mono.error(new IllegalArgumentException("비밀번호 없음"));

        return loadAccount(logInInfo.insertedEmail())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("일치하는 데이터 없음")))
                .flatMap(simpleAccountDTO -> {
                    if(!passwordEncoder().matches(logInInfo.insertedPassword(), simpleAccountDTO.password()))
                        return Mono.error(new IllegalArgumentException("비밀번호 틀림"));

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
                .map(AccountMapper.INSTANCE::toDto);
    }

    public Mono<Boolean> isExistsAccount(String email){
        return accountRepository.existsByEmail(email);
    }

    public void deleteAccount(String email){
        accountRepository.deleteByEmail(email)
                .flatMap(result -> {
                    if (!(result > 0))
                        throw new RuntimeException("사용자 삭제 실패");
                    return null;
                });
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
