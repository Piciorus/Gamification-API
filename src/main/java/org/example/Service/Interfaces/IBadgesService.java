package org.example.Service.Interfaces;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;

import java.util.UUID;

public interface IBadgesService {
    Badge createBadge(CreateBadgeRequest createBadgeRequest);

    void deleteBadge(UUID idBadge);

    GetBadgeByIdResponse findBadgeById(UUID idBadge);

    Iterable<GetAllBadgesResponse> findAllBadges();

    void rewardBadge(UUID idBadge, UUID idUser);

    Iterable<GetBadgeByIdResponse> findBadgesByUserId(UUID idUser);
}
