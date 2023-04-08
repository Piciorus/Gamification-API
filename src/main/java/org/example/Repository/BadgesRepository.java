package org.example.Repository;

import org.example.Domain.Entities.Badge;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadgesRepository extends CrudRepository<Badge,Integer> {
    Badge save(Badge badges);

    void deleteById(final Integer id);

    Badge getById(final Integer id);

    Iterable<Badge> findAll();
}
