package com.deniz.bloomlishbackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "\"user\"", schema = "uygulama")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userID;
    private String username;
    @Column
    private String email;
    @Column
    private String password;
    private String role;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Results> results = new ArrayList<>();
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BlogPost> blogPosts = new ArrayList<>();

    @ManyToMany(mappedBy = "likedUsers")
    private Set<BlogPost> likedPosts = new HashSet<>();

    @Enumerated(EnumType.STRING)
    private Level currentLevel;
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name="account_status",nullable = false)
    private AccountStatus accountStatus = AccountStatus.ACTIVE;

    @Column(name="premium",nullable = false)
    private boolean premium = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String r = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(r));
    }

    @Override
    public String getUsername() {
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
        return accountStatus != AccountStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return accountStatus == AccountStatus.ACTIVE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return userID != null && userID.equals(user.userID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID);
    }
    public String getDisplayName() {
        return this.username;
    }

    public User(Long userID){
        this.userID=userID;
    }

    @Column(nullable = false)
    private int totalXp = 0;

    @Column(nullable = false)
    private int weeklyXp = 0;
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}



