package com.tallerwebi.bookhub.entity;

import com.tallerwebi.bookhub.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private Integer age;

    private Role role;

    @Builder.Default
    private Boolean active = false;
    private String recoveryToken;
    private Integer readingGoal;
    private String imageUrl;
    private String bio;

    @Builder.Default
    private Boolean achievementsAssigned = false;
    private LocalDateTime planStartDate;
    private LocalDateTime planEndDate;

    // Relationships - Commented out until related entities are migrated
    
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "plan_id")
    // @ToString.Exclude
    // private Plan plan;

    // @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    // @ToString.Exclude
    // private Set<UserNotification> notifications;

    // Audit fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
