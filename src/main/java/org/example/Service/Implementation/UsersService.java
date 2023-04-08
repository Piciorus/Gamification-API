package org.example.Service.Implementation;

import org.example.Domain.Entities.User;
import org.example.Domain.Mapper.Mapper;
import org.example.Domain.Models.LoginUserRequest;
import org.example.Domain.Models.RegisterUserRequest;
import org.example.Repository.UsersRepository;
import org.example.Service.Interfaces.IUserService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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
    public User register(RegisterUserRequest registerUserRequest) {
        return usersRepository.save(mapper.RegisterUserRequestToUser(registerUserRequest));
    }
    @Override
    public void deleteUserById(Integer id) {
        usersRepository.deleteById(id);
    }

    @Override
    public User getUserById(Integer id) {
        return usersRepository.getById(id);
    }

    @Override
    public Iterable<User> getAllUsers() {
        return usersRepository.findAll();
    }

    @Override
    public User login(LoginUserRequest loginUserRequest) throws Exception {
        if(Objects.isNull(loginUserRequest.getUsername()) || Objects.isNull(loginUserRequest.getPassword())){
            throw new Exception("Username or password is null");
        }

        User userFromDb = usersRepository.findByUsername(loginUserRequest.getUsername());

        if(Objects.isNull(userFromDb)){
            throw new Exception("User not found");
        }

        if(!userFromDb.getPassword().equals(loginUserRequest.getPassword())){
            throw new Exception("Password is incorrect");
        }

        return userFromDb;
    }

    @Override
    public User updateTokens(int id,int tokens) {
        User user = usersRepository.getById(id);
        user.setTokens(user.getTokens()+tokens);
        return usersRepository.save(user);
    }

    @Override
    public List<User> getUsersSortedByTokensAscending() {
        Iterable<User> iterable = usersRepository.findAll();
        Stream<User> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.sorted(Comparator.comparingInt(User::getTokens)).collect(Collectors.toList());
    }

    @Override
    public List<User> getUsersSortedByTokensDescending() {
        Iterable<User> iterable = usersRepository.findAll();
        Stream<User> stream = StreamSupport.stream(iterable.spliterator(), false);
        return stream.sorted((o1, o2) -> o2.getTokens()-o1.getTokens()).collect(Collectors.toList());
    }

}
