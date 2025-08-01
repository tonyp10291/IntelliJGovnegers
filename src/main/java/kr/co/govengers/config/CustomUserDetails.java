package kr.co.govengers.config;

import kr.co.govengers.entity.User;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


@ToString
public class CustomUserDetails implements UserDetails {
    private final User user;

    // Constructor
    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        System.out.println("CustomUserDetails_getAuthorities() 실행");
        return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
    }

    public String getEmail() {
        return user.getUmail();
    }

    public String getUnm(){
        return user.getUnm();
    }

    public String getUtel(){
        return user.getUtel();
    }

    public String getUbt(){
        return user.getUbt();
    }

    public boolean isEmailVerified(){
        return user.isEmailVerified();
    }

    public boolean isSmsVerified(){
        return user.isSmsVerified();
    }

    public int getPoint(){
        return user.getPoint();
    }

    @Override
    public String getUsername() {
        return user.getUid();
    }

    @Override
    public String getPassword() {
        return user.getUpw();
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

}