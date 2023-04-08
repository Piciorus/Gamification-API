package org.example.Service.Implementation;

import org.example.Domain.Entities.Leaderboard;
import org.example.Domain.Entities.User;
import org.example.Repository.LeaderboardRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.ILeaderboardService;
import org.springframework.stereotype.Service;

@Service
public class LeaderboardService implements ILeaderboardService {

    private final UsersRepository userRepository;
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardService(UsersRepository userRepository, LeaderboardRepository leaderboardRepository) {
        this.userRepository = userRepository;
        this.leaderboardRepository = leaderboardRepository;
    }

    @Override
    public Leaderboard createLeaderboard(Leaderboard leaderboards,int idUser) {
        User user = userRepository.getById(idUser);
        leaderboards.setUser(user);
        return leaderboardRepository.save(leaderboards);
    }

    @Override
    public Leaderboard updateLeaderboard(Leaderboard leaderboards, Integer id) {
        Leaderboard leaderboardFromDb = leaderboardRepository.getById(id);
        leaderboardFromDb.setPoints(leaderboards.getPoints());
        leaderboardFromDb.setPoints(leaderboards.getPosition());
        return leaderboardRepository.save(leaderboardFromDb);
    }

    @Override
    public void deleteLeaderboard(Integer id) {
        leaderboardRepository.deleteById(id);
    }

    @Override
    public Leaderboard findLeaderboardById(Integer id) {
        return leaderboardRepository.getById(id);
    }

    @Override
    public Iterable<Leaderboard> findAllLeaderboards() {
        return leaderboardRepository.findAll();
    }
}
