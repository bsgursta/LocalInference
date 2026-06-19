package com.proto.localinference.services;

import com.proto.localinference.dto.requestarguments.AuthRecord;
import com.proto.localinference.exceptions.UsernameIsTakenException;
import com.proto.localinference.model.User;
import com.proto.localinference.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userRepository
        .findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));
  }

  public User createUser(AuthRecord request) throws Exception {
    if (userRepository.existsByUsername(request.username()))
      throw new UsernameIsTakenException(request.username());

    User user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    return userRepository.save(user);
  }
}
