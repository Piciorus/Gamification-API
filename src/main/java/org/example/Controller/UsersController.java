package org.example.Controller;

import org.example.Domain.Models.User.Response.GetAllUsersResponse;
import org.example.Domain.Models.User.Response.GetUserByIdResponse;
import org.example.Service.Interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @DeleteMapping(path = "/deleteUserById/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity deleteUserById(@PathVariable("id") @NotBlank UUID userId) {
        userService.deleteUserById(userId);
        return ResponseEntity.ok().body("User deleted!");
    }


}
