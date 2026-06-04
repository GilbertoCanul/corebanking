package dev.gcanul.corebanking.entities;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // Este será el email o nombre de usuario para loguearse

    @Column(nullable = false)
    private String password;

    // Métodos de UserDetails (Spring Security los usará)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(); // Por ahora vacío, luego veremos Roles (ADMIN, USER)
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    // Estas banderas son para reglas de negocio (ej. bloquear usuarios)
    @Override
    public boolean isAccountNonExpired() { return true; }
    @Override
    public boolean isAccountNonLocked() { return true; }
    @Override
    public boolean isCredentialsNonExpired() { return true; }
    @Override
    public boolean isEnabled() { return true; }
}