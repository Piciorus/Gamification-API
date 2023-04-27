package org.example.Repository;

import org.example.Domain.Entities.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UsersRepository extends CrudRepository<User, UUID> {
    User save(User user);

    void deleteById(final UUID id);

    User getById(final UUID id);

    Iterable<User> findAll();

    User findByUsername(String username);

    boolean existsByUsername(String username);
}
