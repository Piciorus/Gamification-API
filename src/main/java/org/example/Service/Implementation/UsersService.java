package org.example.Service.Implementation;

import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class UsersService implements IUserService {
    private final UsersRepository usersRepository;
    private final Mapper mapper;

    public UsersService(UsersRepository usersRepository, Mapper mapper) {
        this.usersRepository = usersRepository;
        this.mapper = mapper;
    }

    @Override
    public void deleteUserById(Integer id) {
        usersRepository.deleteById(id);
    }

    @Override
    public GetUserByIdResponse getUserById(Integer id) {
        User user = usersRepository.getById(id);
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
    public User updateTokens(int id, int tokens) {
        User user = usersRepository.getById(id);
        user.setTokens(user.getTokens() - tokens);
        return usersRepository.save(user);
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

    @Override
    public void updateThreshold(int id, int threshold) {
        User user = usersRepository.getById(id);
        user.setThreshold(threshold);
        usersRepository.save(user);
    }

}
