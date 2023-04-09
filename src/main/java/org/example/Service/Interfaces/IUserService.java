package org.example.Service.Interfaces;

import org.example.Domain.Entities.User;
import org.example.Domain.Models.User.Request.LoginUserRequest;
import org.example.Domain.Models.User.Request.RegisterUserRequest;

import java.util.List;

public interface IUserService {
    void deleteUserById(final Integer id);

    User getUserById(final Integer id);

    Iterable<User> getAllUsers();

    User updateTokens(int id,int tokens);

    List<User> getUsersSortedByTokensAscending();

    List<User> getUsersSortedByTokensDescending();

}
