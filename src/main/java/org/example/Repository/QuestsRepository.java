package org.example.Repository;

import org.example.Domain.Entities.Quest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestsRepository extends JpaRepository<Quest, UUID> {
    Quest save(Quest quests);

    void deleteById(final UUID id);

    Quest getById(final UUID id);

    List<Quest> findAll();

    @Query("SELECT q FROM Quest q INNER JOIN q.users1 qu WHERE qu.id=:userId")
    List<Quest> findAllQuestsByUserId(@Param("userId") UUID userId);


}
