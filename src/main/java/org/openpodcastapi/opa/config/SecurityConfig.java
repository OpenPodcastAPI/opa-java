package org.openpodcastapi.opa.config;

import lombok.RequiredArgsConstructor;
import org.openpodcastapi.opa.auth.ApiBearerTokenAuthenticationConverter;
import org.openpodcastapi.opa.auth.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_PAGES = {
            "/",
            "/login",
            "/logout-confirm",
            "/register",
            "/docs",
            "/docs/**",
            "/css/**",
            "/js/**",
            "/images/**",
            "/favicon.ico",
    };

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurity(
            HttpSecurity http,
            JwtAuthenticationProvider jwtAuthenticationProvider,
            AuthenticationEntryPoint entryPoint,
            AccessDeniedHandler deniedHandler,
            ApiBearerTokenAuthenticationConverter converter
    ) {

        AuthenticationManager jwtManager = new ProviderManager(jwtAuthenticationProvider);

        BearerTokenAuthenticationFilter bearerFilter =
                new BearerTokenAuthenticationFilter(jwtManager, converter);

        bearerFilter.setAuthenticationFailureHandler(
                entryPoint::commence
        );

        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler)
                )
                .addFilterBefore(bearerFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain webSecurity(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/docs", "/docs/**")
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PAGES).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .failureUrl("/login?error=true"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"))
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                               BCryptPasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean(name = "jwtAuthManager")
    public AuthenticationManager jwtAuthenticationManager(JwtAuthenticationProvider provider) {
        return new ProviderManager(provider);
    }

    @Bean(name = "apiLoginManager", defaultCandidate = false)
    public AuthenticationManager apiLoginAuthenticationManager(
            DaoAuthenticationProvider daoProvider) {
        return new ProviderManager(daoProvider);
    }
}
