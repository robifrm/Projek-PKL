package com.vnet.vnet_backend.config;

import com.vnet.vnet_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsernameIgnoreCase(username)
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.getUsername())
                        .password(user.getPassword())
                        .authorities(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan"));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/registrations/public").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/packages/public", "/api/packages/*/public").permitAll()
                .requestMatchers("/api/registrations/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT", "TECHNICIAN")
                .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT")
                .requestMatchers(HttpMethod.GET, "/api/customers/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT")
                .requestMatchers(HttpMethod.POST, "/api/customers/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT")
                .requestMatchers(HttpMethod.PUT, "/api/customers/*/status").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT")
                .requestMatchers(HttpMethod.DELETE, "/api/customers/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/packages/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT", "TECHNICIAN")
                .requestMatchers("/api/packages/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/import/stats").hasAnyRole("SUPER_ADMIN", "STAFF")
                .requestMatchers("/api/import/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/agents/performance").hasAnyRole("SUPER_ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET, "/api/agents/*/customers").hasAnyRole("SUPER_ADMIN", "STAFF")
                .requestMatchers("/api/agents/**").hasRole("SUPER_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/addresses/insights").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT")
                .requestMatchers(HttpMethod.GET, "/api/system-config").hasAnyRole("SUPER_ADMIN", "STAFF")
                .requestMatchers(HttpMethod.PUT, "/api/system-config").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/users/profile", "/api/users/change-password", "/api/users/sessions", "/api/users/sessions/**").hasAnyRole("SUPER_ADMIN", "STAFF", "AGENT", "TECHNICIAN")
                .requestMatchers("/api/users/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/**").hasRole("SUPER_ADMIN")
                .anyRequest().permitAll()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
