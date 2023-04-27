package org.example.Repository;

import org.example.Domain.Entities.Quest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuestsRepository extends CrudRepository<Quest, UUID> {
    Quest save(Quest quests);

    void deleteById(final UUID id);

    Quest getById(final UUID id);

    Iterable<Quest> findAll();
}
