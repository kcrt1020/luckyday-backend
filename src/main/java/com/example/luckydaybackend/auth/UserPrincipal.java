package com.example.luckydaybackend.auth;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class UserPrincipal implements UserDetails {
    // ✅ getUserId 대신 getEmail 사용
    @Getter
    private final String email; // ✅ userId 제거
    private final String password;

    public UserPrincipal(String email, String password) { // ✅ userId 제거
        this.email = email;
        this.password = password;
    }

    @Override
    public String getUsername() { // ✅ getUsername은 email 반환
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }
}
