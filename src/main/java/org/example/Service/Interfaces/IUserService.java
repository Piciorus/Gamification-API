package org.example.Service.Interfaces;

import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;

import java.util.UUID;

public interface IUserService {
    void deleteUserById(final UUID idUser);

    GetUserByIdResponse getUserById(final UUID idUser);

    Iterable<GetAllUsersResponse> getAllUsers();

    Iterable<GetAllUsersResponse> getUsersSortedByTokensAscending();

    Iterable<GetAllUsersResponse> getUsersSortedByTokensDescending();

}
