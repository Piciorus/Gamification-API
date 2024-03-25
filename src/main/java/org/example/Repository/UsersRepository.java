package org.example.Repository;

import org.example.Domain.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsersRepository extends JpaRepository<User, UUID> {
    User save(User user);

    void deleteById(final UUID id);

    User getById(final UUID id);

    List<User> findAll();

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
