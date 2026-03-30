package com.fluenz.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "image_cache")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String keyword;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    @Column(length = 50)
    private String provider;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.expiresAt == null) {
            this.expiresAt = this.createdAt.plusDays(30);
        }
    }
}
