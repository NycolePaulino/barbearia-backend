package br.com.barbearia.api.security;

import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // busca o user no banco c/ o e-mail dele
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        // convertendo o user p/ user do spring security
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                "",
                new ArrayList<>()
        );
    }
}