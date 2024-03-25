package org.example.Service.Interfaces;

import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;

import java.util.UUID;

public interface IUserService {

    /**
     * Deletes a user by ID.
     *
     * @param idUser The ID of the user to delete.
     */
    void deleteUserById(final UUID idUser);

    /**
     * Retrieves a user by ID.
     *
     * @param idUser The ID of the user to retrieve.
     * @return The user response.
     */
    GetUserByIdResponse getUserById(final UUID idUser);

    /**
     * Retrieves all users.
     *
     * @return An iterable of all users.
     */
    Iterable<GetAllUsersResponse> getAllUsers();

    /**
     * Retrieves all users sorted by tokens in ascending order.
     *
     * @return An iterable of users sorted by tokens in ascending order.
     */
    Iterable<GetAllUsersResponse> getUsersSortedByTokensAscending();

    /**
     * Retrieves all users sorted by tokens in descending order.
     *
     * @return An iterable of users sorted by tokens in descending order.
     */
    Iterable<GetAllUsersResponse> getUsersSortedByTokensDescending();
}
