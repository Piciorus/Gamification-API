package org.example.Repository;

import org.example.Domain.Entities.Badge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BadgesRepository extends CrudRepository<Badge, UUID> {
    Badge save(Badge badges);

    void deleteById(final UUID id);

    Badge getById(final UUID id);

    Iterable<Badge> findAll();
}
