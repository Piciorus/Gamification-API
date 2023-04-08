package org.example.Service.Implementation;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.User;
import org.example.Repository.BadgesRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IBadgesService;
import org.springframework.stereotype.Service;

@Service
public class BadgeService implements IBadgesService {

    private final BadgesRepository badgesRepository;

    private final UsersRepository usersRepository;

    public BadgeService(BadgesRepository badgesRepository, UsersRepository usersRepository) {
        this.badgesRepository = badgesRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public Badge createBadge(Badge badges) {
        return badgesRepository.save(badges);
    }

    @Override
    public Badge updateBadge(Badge badges, Integer id) {
        Badge badgeFromDb = badgesRepository.getById(id);
        badgeFromDb.setName(badges.getName());
        return badgesRepository.save(badgeFromDb);
    }

    @Override
    public void deleteBadge(Integer id) {
        badgesRepository.deleteById(id);
    }

    @Override
    public Badge findBadgeById(Integer id) {
        return badgesRepository.getById(id);
    }

    @Override
    public Iterable<Badge> findAllBadges() {
        return badgesRepository.findAll();
    }

    @Override
    public void rewardBadge(int idBadge, int idUser) {
        Badge badge = badgesRepository.getById(idBadge);
        User user = usersRepository.getById(idUser);
        user.getBadgesList().add(badge);
        badge.getUsers().add(user);
        badgesRepository.save(badge);
        usersRepository.save(user);
    }

    @Override
    public Iterable<Badge> findBadgesByUserId(int idUser) {
        User user=usersRepository.getById(idUser);
        return user.getBadgesList();
    }
}
