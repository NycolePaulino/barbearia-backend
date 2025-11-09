package br.com.barbearia.api.security;

import br.com.barbearia.api.domain.User;
import br.com.barbearia.api.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // pega os dados do usuário (com o google)
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String imageUrl = oauth2User.getAttribute("picture");

        // procura o usuário no banco de dados
        Optional<User> userOpt = userRepository.findByEmail(email);

        User user;
        if (userOpt.isPresent()) {
            // se o user já existe -> atualiza os dados
            user = userOpt.get();
            user.setName(name);
            user.setImage(imageUrl);
            user.setUpdatedAt(Instant.now());
        } else {
            // se ele é novo -> cria um
            user = new User(
                    null,
                    name,
                    email,
                    true,
                    imageUrl,
                    Instant.now(),
                    Instant.now()
            );
        }

        // salva ou atualiza o usuário
        userRepository.save(user);

        // gera o Token JWT para esse usuário
        String token = jwtTokenProvider.generateToken(authentication);

        String targetUrl = "http://localhost:3000/?token=" + token;

        response.sendRedirect(targetUrl);
    }
}