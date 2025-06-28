package Bright.BeSafeProject.service;

import Bright.BeSafeProject.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements ReactiveUserDetailsService {
    private final AccountService accountService;

    @Override
    public Mono<UserDetails> findByUsername(String username) throws UsernameNotFoundException {
        return accountService.loadAccount(username)
                .map(accountDTO -> new CustomUserDetails(
                        accountDTO.email(),
                        accountDTO.password(),
                        List.of(new SimpleGrantedAuthority(accountDTO.authority()))
                ));
    }
}
