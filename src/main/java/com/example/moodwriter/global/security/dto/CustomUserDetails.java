package com.example.moodwriter.global.security.dto;

import com.example.moodwriter.user.entity.User;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final UUID id;
  private final String username;
  private final String role;
  private final boolean isDeleted;

  public CustomUserDetails(User user) {
    this.id = user.getId();
    this.username = user.getEmail();
    this.role = user.getRole().toString();
    this.isDeleted = user.isDeleted();
  }


  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(role));
  }

  @Override
  public String getPassword() {
    return null;
  }

  @Override
  public String getUsername() {
    return username;
  }
}
