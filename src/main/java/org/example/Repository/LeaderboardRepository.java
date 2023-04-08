package org.example.Repository;

import org.example.Domain.Entities.Leaderboard;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRepository extends CrudRepository<Leaderboard,Integer> {
    Leaderboard save(Leaderboard leaderboard);

    void deleteById(final Integer id);

    Leaderboard getById(final Integer id);

    Iterable<Leaderboard> findAll();
}
