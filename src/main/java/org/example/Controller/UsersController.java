package org.example.Controller;

import org.example.Domain.Entities.User;
import org.example.Domain.Models.User.Request.UpdateUserRequest;
import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.example.Service.Interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/users")
public class UsersController {
    private final IUserService userService;

    @Autowired
    public UsersController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping(path = "/getAllUsers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Iterable<GetAllUsersResponse>> getAllUsers(@RequestParam(defaultValue = "asc") String sortType) {
        if (sortType.equals("asc")) {
            return ResponseEntity.ok(userService.getUsersSortedByTokensAscending());
        } else if (sortType.equals("desc")) {
            return ResponseEntity.ok(userService.getUsersSortedByTokensDescending());
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping(path = "/getUserById/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<GetUserByIdResponse> getUserById(@PathVariable("id") @NotBlank UUID userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping(path = "/updateUser/{id}")
    @PreAuthorize("hasRole('ADMIN') or #userId == principal.id")
    public ResponseEntity<User> updateUser(
            @PathVariable("id") @NotBlank UUID userId,
            @RequestBody UpdateUserRequest updateUserRequest
    ) {
        User updatedUser = userService.updateUser(updateUserRequest, userId);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping(path = "/deleteUserById/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteUserById(@PathVariable("id") @NotBlank UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().body("User deleted!");
    }

    @DeleteMapping(path = "/deleteUser/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable("id") @NotBlank UUID idUser) {
        userService.deleteUser(idUser);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
