package org.example.Repository;

import jakarta.transaction.Transactional;
import org.example.Domain.Entities.RefreshToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

@Transactional
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    @Query("delete from RefreshToken where user.id = :id")
    @Modifying
    void deleteRefreshTokenFromUser(UUID id);
}
