package org.example.Service.Interfaces;

import org.example.Domain.Entities.Quest;
import org.example.Domain.Models.Quest.Request.CreateQuestRequest;
import org.example.Domain.Models.Quest.Request.UpdateQuestRequest;
import org.example.Domain.Models.Quest.Response.GetAllQuestsResponse;
import org.example.Domain.Models.Quest.Response.GetQuestResponse;

import java.util.UUID;

public interface IQuestService {

    /**
     * Creates a new quest.
     *
     * @param createQuestRequest The request containing information to create the quest.
     * @param idUser             The ID of the user creating the quest.
     * @return The created quest.
     */
    Quest createQuest(CreateQuestRequest createQuestRequest, UUID idUser);

    /**
     * Updates an existing quest.
     *
     * @param updateQuestRequest The request containing updated information for the quest.
     * @param idQuest            The ID of the quest to update.
     * @return The updated quest.
     */
    Quest updateQuest(UpdateQuestRequest updateQuestRequest, UUID idQuest);

    /**
     * Deletes a quest by its ID.
     *
     * @param idQuest The ID of the quest to delete.
     */
    void deleteQuest(UUID idQuest);

    /**
     * Finds a quest by its ID.
     *
     * @param idQuest The ID of the quest to find.
     * @return The found quest response.
     */
    GetQuestResponse findQuestById(UUID idQuest);

    /**
     * Retrieves all quests.
     *
     * @return An iterable of all quests.
     */
    Iterable<GetAllQuestsResponse> findAllQuests();

    /**
     * Retrieves all resolved quests by user ID.
     *
     * @param idUser The ID of the user.
     * @return An iterable of resolved quests.
     */
    Iterable<GetAllQuestsResponse> findAllResolvedQuestsByUserId(UUID idUser);

    /**
     * Resolves a quest by user ID.
     *
     * @param idQuest The ID of the quest to resolve.
     * @param idUser  The ID of the user resolving the quest.
     * @return The resolved quest response.
     */
    GetQuestResponse resolveQuest(UUID idQuest, UUID idUser);

    /**
     * Checks the answer to a quest.
     *
     * @param userId  The ID of the user answering the quest.
     * @param answer  The answer provided by the user.
     * @param questId The ID of the quest.
     * @return True if the answer is correct, false otherwise.
     */
    boolean checkAnswer(UUID userId, String answer, UUID questId);

}
