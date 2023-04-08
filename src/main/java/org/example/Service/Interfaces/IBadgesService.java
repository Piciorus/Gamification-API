package org.example.Service.Interfaces;

import org.example.Domain.Entities.Badge;

public interface IBadgesService {
    Badge createBadge(Badge badges);

    Badge updateBadge(Badge badges, Integer id);

    void deleteBadge(Integer id);

    Badge findBadgeById(Integer id);

    Iterable<Badge> findAllBadges();

    void rewardBadge(int idBadge,int idUser);

    Iterable<Badge> findBadgesByUserId(int idUser);
}
