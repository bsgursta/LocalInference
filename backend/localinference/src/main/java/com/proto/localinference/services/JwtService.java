package com.proto.localinference.services;

import jakarta.servlet.http.Cookie;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  @Value(value = "${jwt.issuer")
  private String issuer;

  @Value(value = "${jwt.access-token.expiration}")
  private long accessTokenExpiration;

  @Value(value = "${jwt.refresh-token.expiration}")
  private long refreshTokenExpiration;

  private JwtEncoder encoder;
  private JwtDecoder decoder;

  public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
    this.encoder = encoder;
    this.decoder = decoder;
  }

  //   private SecretKey getSigningKey() {
  //     return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
  //   }

  public String generateAccessToken(UserDetails userDetails) {
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();

    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            // .issuer(issuer)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(accessTokenExpiration))
            .subject(userDetails.getUsername())
            .claim("type", "access")
            .build();

    String token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    return token;

    // return Jwts.builder()
    //     .subject(userDetails.getUsername())
    //     .claim(
    //         "roles",
    //         userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
    //     .claim("type", "access")
    //     .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
    //     .signWith(getSigningKey())
    //     .compact();
  }

  public String generateRefreshToken(UserDetails userDetails) {
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    JwtClaimsSet claimsSet =
        JwtClaimsSet.builder()
            // .issuer(issuer)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
            .subject(userDetails.getUsername())
            .claim("type", "refresh")
            .build();

    String token = encoder.encode(JwtEncoderParameters.from(jwsHeader, claimsSet)).getTokenValue();
    return token;

    // return Jwts.builder()
    //     .subject(userDetails.getUsername())
    //     .claim("type", "refresh")
    //     .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
    //     .signWith(getSigningKey())
    //     .compact();
  }

  public String extractUsername(String token) {
    String username = decoder.decode(token).getSubject();
    return username;

    // return Jwts.parser()
    //     .verifyWith(getSigningKey())
    //     .build()
    //     .parseSignedClaims(token)
    //     .getPayload()
    //     .getSubject();
  }

  public Cookie createRefreshCookie(String token) {
    Cookie cookie = new Cookie("refreshToken", token);
    cookie.setHttpOnly(true);
    cookie.setSecure(true);
    cookie.setPath("/");
    cookie.setMaxAge((int) (refreshTokenExpiration / 1000));
    return cookie;
  }

  /* creates empty refresh cookie. for logout */
  public Cookie createBlankRefreshCookie() {
    Cookie refreshCookie = new Cookie("refreshToken", "");
    refreshCookie.setHttpOnly(true);
    refreshCookie.setSecure(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(0);
    return refreshCookie;
  }

  public Cookie createAccessCookie(String token) {
    Cookie cookie = new Cookie("accessToken", token);
    cookie.setHttpOnly(true);
    cookie.setMaxAge((int) (accessTokenExpiration / 1000));
    cookie.setSecure(true);
    cookie.setPath("/");
    return cookie;
  }
}
