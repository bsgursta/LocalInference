package com.proto.localinference.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SpringSecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity httpSecurity,
      JwtDecoder jwtDecoder,
      JwtAuthenticationConverter jwtAuthenticationConverter) {

    httpSecurity
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/", "/auth/**") /* prod - required */
                    .permitAll()
                    // .requestMatchers(
                    // "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**") /* testing api */
                    // .permitAll()
                    .requestMatchers("/mcu/**") /* testing only */
                    .permitAll()
                    // .requestMatchers("/index.html", "/app.js") /* testing frontend */
                    // .permitAll()
                    .anyRequest() /* enable once auth is set */
                    .authenticated())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .oauth2ResourceServer(
            oauth2 ->
                oauth2
                    .jwt(
                        jwt ->
                            jwt.decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter))
                    .bearerTokenResolver(
                        request -> {
                          // String path = request.getRequestURI();
                          // if (path.equals("/")
                          //     || path.equals("/index.html")
                          //     || path.equals("/app.js")) {
                          //   return null;
                          // }

                          String token = null;

                          String bearer = new DefaultBearerTokenResolver().resolve(request);

                          if (bearer != null) {
                            return bearer;
                          } else if (request.getCookies() != null) {
                            for (var cookie : request.getCookies()) {
                              if ("accessToken".equals(cookie.getName())) {
                                token = cookie.getValue();
                                break;
                              }
                            }
                          }
                          if (token == null) return null;

                          try {
                            Jwt jwt = jwtDecoder.decode(token);
                            if (!"access".equals(jwt.getClaim("type"))) return null;
                          } catch (JwtException e) {
                            return null;
                          }

                          return token;
                        }));

    return httpSecurity.build();
  }

  @Bean
  public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration)
      throws Exception {
    return configuration.getAuthenticationManager();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
  }
}
