package com.example.travellingapp.entity;

import com.example.travellingapp.enums.UserSettingEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User implements Serializable, UserDetails {
    @Serial
    private static final long serialVersionUID = 12L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private long userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password")
    private String password;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "dob")

    private LocalDate dob;

    @Column(name = "created_date")
    private Instant createdDate;

    @Column(name = "referred_code")
    private String referredCode;

    @Column(name = "phone_num")
    private String phoneNumber;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "is_OAuth2", nullable = false)
    private boolean isOAuth2;

    @Column(name = "display_name")
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_theme")
    private UserSettingEnum preferredTheme;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "profile_image_public_id", length = 500)
    private String profileImagePublicId;

    @Column(name = "modified_date")
    private Instant modifiedDate;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.displayName = username;
        this.preferredTheme = UserSettingEnum.SYSTEM;
    }

    public User(String username, String password, String phoneNumber, LocalDate dob, Instant createdDate, String email, boolean isActive) {
        this.username = username;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.dob = dob;
        this.createdDate = createdDate;
        this.email = email;
        this.isActive = isActive;
        this.displayName = username;
        this.preferredTheme = UserSettingEnum.SYSTEM;
    }

    public User(String username, String password, LocalDate dob, Instant createdDate, String email, boolean isActive, boolean isOAuth2) {
        this.username = username;
        this.password = password;
        this.dob = dob;
        this.createdDate = createdDate;
        this.email = email;
        this.isActive = isActive;
        this.isOAuth2 = isOAuth2;
        this.displayName = username;
        this.preferredTheme = UserSettingEnum.SYSTEM;
    }

    public User() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}