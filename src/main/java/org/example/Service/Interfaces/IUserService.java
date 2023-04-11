package org.example.Service.Interfaces;

import org.example.Domain.Entities.User;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;

public interface IUserService {
    void deleteUserById(final Integer id);

    GetUserByIdResponse getUserById(final Integer id);

    Iterable<GetAllUsersResponse> getAllUsers();

    User updateTokens(int id,int tokens);

    Iterable<GetAllUsersResponse> getUsersSortedByTokensAscending();

    Iterable<GetAllUsersResponse> getUsersSortedByTokensDescending();
    void updateThreshold(int id, int threshold);

}
