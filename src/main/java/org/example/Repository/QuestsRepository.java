package org.example.Repository;

import org.example.Domain.Entities.Quest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestsRepository extends CrudRepository<Quest,Integer> {
    Quest save(Quest quests);

    void deleteById(final Integer id);

    Quest getById(final Integer id);

    Iterable<Quest> findAll();
}
