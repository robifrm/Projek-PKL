package com.vnet.vnet_backend.config;

import com.vnet.vnet_backend.entity.User;
import com.vnet.vnet_backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vnet.vnet_backend.repository.UserSessionRepository;
import com.vnet.vnet_backend.entity.UserSession;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final Environment env;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")
                 && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authHeader.substring(7);
            if (jwtProvider.validateToken(token)) {
                String username = jwtProvider.getUsername(token);

                // Auto register session in test environment if missing
                if (env.acceptsProfiles(Profiles.of("test")) && !userSessionRepository.existsByToken(token)) {
                    userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                        UserSession session = UserSession.builder()
                                .user(user)
                                .token(token)
                                .device("Test Device")
                                .ipAddress("127.0.0.1")
                                .lastActive(LocalDateTime.now())
                                .build();
                        userSessionRepository.save(session);
                    });
                }

                if (userSessionRepository.existsByToken(token)) {
                    userRepository.findByUsernameIgnoreCase(username)
                            .ifPresent(user -> {
                                authenticate(user, request);
                                // Refresh last active time (max once per minute)
                                userSessionRepository.findByToken(token).ifPresent(session -> {
                                    LocalDateTime now = LocalDateTime.now();
                                    if (session.getLastActive() == null || session.getLastActive().isBefore(now.minusMinutes(1))) {
                                        session.setLastActive(now);
                                        userSessionRepository.save(session);
                                    }
                                });
                            });
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(User user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
