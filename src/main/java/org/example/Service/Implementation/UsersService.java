package org.example.Service.Implementation;

import jakarta.persistence.EntityNotFoundException;
import org.example.Domain.Entities.Role;
import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.User.Request.UpdateUserRequest;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.example.Repository.RefreshTokenRepository;
import org.example.Repository.RoleRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class UsersService implements IUserService {
    private final UsersRepository usersRepository;
    private final Mapper mapper;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UsersService(UsersRepository usersRepository, Mapper mapper, RoleRepository roleRepository, RefreshTokenRepository refreshTokenRepository) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void deleteUserById(UUID idUser) {
        usersRepository.deleteById(idUser);
    }

    @Override
    public GetUserByIdResponse getUserById(UUID idUser) {
        User user = usersRepository.getById(idUser);
        return mapper.UserToGetUserByIdResponse(user);
    }

    @Override
    public Iterable<GetAllUsersResponse> getAllUsers() {
        List<GetAllUsersResponse> list = new ArrayList<>();
        usersRepository.findAll().forEach(user -> {
            list.add(mapper.UserToGetAllUsersResponse(user));
        });
        return list;
    }

    @Override
    public User updateUser(UpdateUserRequest updateUserRequest, UUID idUser) {
        Role role = roleRepository.findByName(updateUserRequest.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));
        User userFromDb = usersRepository.getById(idUser);
        mapper.UpdateUserRequestToUser(updateUserRequest, userFromDb);
        userFromDb.getRoles().add(role);
        return usersRepository.save(userFromDb);
    }

    @Override
    public void deleteUser(UUID idUser) {
        refreshTokenRepository.deleteRefreshTokenFromUser(idUser);
        usersRepository.deleteById(idUser);
    }

    @Override
    public Iterable<GetAllUsersResponse> getUsersSortedByTokensAscending() {
        Iterable<User> iterable = usersRepository.findAll();
        Stream<User> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.sorted(Comparator.comparingInt(User::getTokens)).map(user -> mapper.UserToGetAllUsersResponse(user)).collect(Collectors.toList());
    }

    @Override
    public Iterable<GetAllUsersResponse> getUsersSortedByTokensDescending() {
        Iterable<User> iterable = usersRepository.findAll();
        Stream<User> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.sorted(Comparator.comparingInt(User::getTokens).reversed()).map(user -> mapper.UserToGetAllUsersResponse(user)).collect(Collectors.toList());
    }
}
