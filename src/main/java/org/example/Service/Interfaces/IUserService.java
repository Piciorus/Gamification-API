package org.example.Service.Interfaces;

import org.example.Domain.Entities.User;
import org.example.Domain.Models.LoginUserRequest;
import org.example.Domain.Models.RegisterUserRequest;

import java.util.List;

public interface IUserService {
    User register(RegisterUserRequest registerUserRequest);

    void deleteUserById(final Integer id);

    User getUserById(final Integer id);

    Iterable<User> getAllUsers();

    User login(LoginUserRequest user) throws Exception;

    User updateTokens(int id,int tokens);

    List<User> getUsersSortedByTokensAscending();

    List<User> getUsersSortedByTokensDescending();

}
