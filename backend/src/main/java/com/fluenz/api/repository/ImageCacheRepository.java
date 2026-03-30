package com.fluenz.api.repository;

import com.fluenz.api.entity.ImageCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageCacheRepository extends JpaRepository<ImageCache, UUID> {

    @Query("SELECT ic FROM ImageCache ic WHERE ic.keyword = :keyword AND ic.expiresAt > :now")
    Optional<ImageCache> findByKeywordAndNotExpired(@Param("keyword") String keyword, @Param("now") LocalDateTime now);

    default Optional<ImageCache> findValidByKeyword(String keyword) {
        return findByKeywordAndNotExpired(keyword, LocalDateTime.now());
    }

    @Modifying
    @Query(value = "INSERT INTO image_cache (id, keyword, image_url, provider, created_at, expires_at) " +
            "VALUES (:id, :keyword, :imageUrl, :provider, :createdAt, :expiresAt) " +
            "ON CONFLICT (keyword) DO NOTHING", nativeQuery = true)
    void upsert(@Param("id") UUID id, @Param("keyword") String keyword,
                @Param("imageUrl") String imageUrl, @Param("provider") String provider,
                @Param("createdAt") LocalDateTime createdAt, @Param("expiresAt") LocalDateTime expiresAt);
}
