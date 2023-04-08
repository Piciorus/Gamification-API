package org.example.Service.Interfaces;

import org.example.Domain.Entities.Leaderboard;

public interface ILeaderboardService {

    Leaderboard createLeaderboard(Leaderboard leaderboards,int idUser);

    Leaderboard updateLeaderboard(Leaderboard leaderboards, Integer id);

    void deleteLeaderboard(Integer id);

    Leaderboard findLeaderboardById(Integer id);

    Iterable<Leaderboard> findAllLeaderboards();
}
