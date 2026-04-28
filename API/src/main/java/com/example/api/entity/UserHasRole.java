package com.example.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_has_role",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_uhr_user_role",
                columnNames = {"user_id", "role_id"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UserHasRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)  // role hầu như luôn cần khi load UserHasRole
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "granted_at", nullable = false, updatable = false)
    private LocalDateTime grantedAt;

    @PrePersist
    protected void onCreate() {
        if (this.grantedAt == null) {
            this.grantedAt = LocalDateTime.now();
        }
    }
}