package org.example.Controller;

import org.example.Domain.Entities.User;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.example.Service.Interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Iterable<GetAllUsersResponse>> getAllUsers(@RequestParam(defaultValue = "asc") String sort) {
        if (sort.equals("asc")) {
            return ResponseEntity.ok(userService.getUsersSortedByTokensAscending());
        } else if (sort.equals("desc")) {
            return ResponseEntity.ok(userService.getUsersSortedByTokensDescending());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/getUserById/{id}")
    public ResponseEntity<GetUserByIdResponse> getUserById(@PathVariable("id") Integer userId) {
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
    public ResponseEntity<Iterable<GetAllUsersResponse>> getUsersSortedByTokensAscending() {
        return ResponseEntity.ok(userService.getUsersSortedByTokensAscending());
    }

    @GetMapping(path = "/getUsersSortedByTokensDescending")
    public ResponseEntity<Iterable<GetAllUsersResponse>> getUsersSortedByTokensDescending() {
        return ResponseEntity.ok(userService.getUsersSortedByTokensDescending());
    }

    @PutMapping(path = "/updateThreshold/{id}")
    public void updateThreshold(@PathVariable("id") Integer userId, @RequestBody final Map<String, Integer> requestBody) {
        int threshold = requestBody.get("threshold");
        userService.updateThreshold(userId, threshold);
    }


}
