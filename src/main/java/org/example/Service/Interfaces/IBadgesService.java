package org.example.Service.Interfaces;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;

public interface IBadgesService {
    Badge createBadge(CreateBadgeRequest createBadgeRequest);

    Badge updateBadge(Badge badges, Integer id);

    void deleteBadge(Integer id);

    GetBadgeByIdResponse findBadgeById(Integer id);

    Iterable<GetAllBadgesResponse> findAllBadges();

    void rewardBadge(int idBadge,int idUser);

    Iterable<Badge> findBadgesByUserId(int idUser);
}
