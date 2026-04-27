package com.Kee.V2C.security;

import com.Kee.V2C.entity.Credential;
import com.Kee.V2C.enums.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


/*a wrapper class around the UserDetails to minimize db queries*/
public class UserDetailsImpl implements UserDetails {
        private final Long id; // This is the magic "slot" for your ID
        private final String username;
        private final String password;
        private final UserStatus status;
        private final Collection<? extends GrantedAuthority> authorities;

        public UserDetailsImpl(Credential credential) {
            this.id = credential.getId();
            this.username = credential.getUserName();
            this.password = credential.getPassword();
            this.status=credential.getUserStatus();
            this.authorities = Collections.singletonList(
                    new SimpleGrantedAuthority(credential.getRole().getRole().name())
            );
        }

        public Long getId() {
            return id;
        }
        public UserStatus getStatus(){return  status;}
        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
        @Override
        public String getPassword() { return password; }
        @Override
        public String getUsername() { return username; }

        // Set these to true for your portfolio project
        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isAccountNonLocked() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
        @Override public boolean isEnabled() { return status!=UserStatus.SUSPENDED; }
    }
