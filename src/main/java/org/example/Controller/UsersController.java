package org.example.Controller;

import org.example.Domain.Entities.User;
import org.example.Service.Interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController()
@Component
public class UsersController {
    private final IUserService userService;

    @Autowired
    public UsersController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/getAllUsers")
    public ResponseEntity<Iterable<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping(path = "/getUserById/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Integer userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @DeleteMapping(path = "/deleteUserById/{id}")
    public ResponseEntity deleteUserById(@PathVariable("id") Integer userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping(path = "/updateTokens/{id}")
    public User updateTokens(@PathVariable("id") Integer userId, @RequestBody final Map<String, Integer> requestBody) {
        int tokens = requestBody.get("tokens");
        return userService.updateTokens(userId, tokens);
    }

    @GetMapping(path = "/getUsersSortedByTokensAscending")
    public ResponseEntity<Iterable<User>> getUsersSortedByTokensAscending() {
        return ResponseEntity.ok(userService.getUsersSortedByTokensAscending());
    }

    @GetMapping(path = "/getUsersSortedByTokensDescending")
    public ResponseEntity<Iterable<User>> getUsersSortedByTokensDescending() {
        return ResponseEntity.ok(userService.getUsersSortedByTokensDescending());
    }


}
