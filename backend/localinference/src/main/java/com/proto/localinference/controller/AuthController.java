package com.proto.localinference.controller;

import com.proto.localinference.dto.AuthRecord;
import com.proto.localinference.services.AuthService;
import com.proto.localinference.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

  @Value(value = "${jwt.access-token.expiration}")
  private long accessTokenExpiration;

  @Value(value = "${jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  private final AuthenticationManager authenticationManager;
  private final AuthService authService;
  private final JwtService jwtService;
  private final JwtDecoder jwtDecoder;

  public AuthController(
      AuthenticationManager authenticationManager,
      AuthService authService,
      JwtService jwtService,
      JwtDecoder jwtDecoder) {
    this.authenticationManager = authenticationManager;
    this.authService = authService;
    this.jwtService = jwtService;
    this.jwtDecoder = jwtDecoder;
  }

  // @Deprecated
  // @PostMapping("/register")
  // public ResponseEntity<String> createUser(@RequestBody AuthRecord request) throws Exception {
  //   var res = authService.createUser(request);

  //   return ResponseEntity.ok(res.getUsername());
  // }

  @PostMapping("/login")
  public ResponseEntity<String> login(
      @RequestBody @Valid AuthRecord request, HttpServletResponse response) {
    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.username(), request.password()));
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    var userDetails = authService.loadUserByUsername(request.username());
    String accessToken = jwtService.generateAccessToken(userDetails);
    String refreshToken = jwtService.generateRefreshToken(userDetails);

    response.addCookie(jwtService.createAccessCookie(accessToken));
    response.addCookie(jwtService.createRefreshCookie(refreshToken));
    return ResponseEntity.ok(accessToken);
  }

  /**
   * Used by the frontend to collect a new accessToken to use for auth. Might use the
   * SecurityFilterChain to renew access tokens manually
   *
   * @param request
   * @param response
   * @return refreshToken
   */
  @PostMapping("/refresh")
  public ResponseEntity<String> refresh(HttpServletRequest request, HttpServletResponse response) {
    Cookie[] cookies = request.getCookies();

    if (cookies == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    String refreshToken = null;
    for (Cookie c : cookies) {
      if (c.getName().equals("refreshToken")) {
        refreshToken = c.getValue();
        Jwt jwt = jwtDecoder.decode(refreshToken);
        if (!"refresh".equals(jwt.getClaim("type")))
          return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        break;
      }
    }

    if (refreshToken == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    UserDetails userDetails =
        authService.loadUserByUsername(jwtService.extractUsername(refreshToken));

    response.addCookie(jwtService.createAccessCookie(jwtService.generateAccessToken(userDetails)));

    return ResponseEntity.ok(jwtService.generateAccessToken(userDetails));
  }

  @PostMapping("/logout")
  public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
    response.addCookie(jwtService.createBlankRefreshCookie());
    return ResponseEntity.ok(Map.of("message", "logged out successfully"));
  }
}
