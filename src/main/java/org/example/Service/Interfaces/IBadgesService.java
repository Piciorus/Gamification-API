package org.example.Service.Interfaces;

import org.example.Domain.Entities.Badge;
import org.example.Domain.Models.Badge.Request.CreateBadgeRequest;
import org.example.Domain.Models.Badge.Response.GetAllBadgesResponse;
import org.example.Domain.Models.Badge.Response.GetBadgeByIdResponse;

import java.util.UUID;

public interface IBadgesService {

    /**
     * Creates a new badge.
     *
     * @param createBadgeRequest The request containing information to create the badge.
     * @return The created badge.
     */
    Badge createBadge(CreateBadgeRequest createBadgeRequest);

    /**
     * Deletes a badge by its ID.
     *
     * @param idBadge The ID of the badge to delete.
     */
    void deleteBadge(UUID idBadge);

    /**
     * Finds a badge by its ID.
     *
     * @param idBadge The ID of the badge to find.
     * @return The found badge response.
     */
    GetBadgeByIdResponse findBadgeById(UUID idBadge);

    /**
     * Retrieves all badges.
     *
     * @return An iterable of all badges.
     */
    Iterable<GetAllBadgesResponse> findAllBadges();

    /**
     * Rewards a badge to a user.
     *
     * @param idBadge The ID of the badge to reward.
     * @param idUser  The ID of the user to reward the badge.
     */
    void rewardBadge(UUID idBadge, UUID idUser);

    /**
     * Finds badges by user ID.
     *
     * @param idUser The ID of the user.
     * @return An iterable of badges owned by the user.
     */
    Iterable<GetBadgeByIdResponse> findBadgesByUserId(UUID idUser);
}
