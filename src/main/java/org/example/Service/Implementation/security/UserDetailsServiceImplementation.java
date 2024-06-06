package org.example.Service.Implementation.security;

import org.example.Controller.AuthController;
import org.example.Domain.Entities.Badge;
import org.example.Domain.Entities.ERole;
import org.example.Domain.Entities.Role;
import org.example.Domain.Entities.Security.UserDetailsImplementation;
import org.example.Domain.Entities.User;
import org.example.Domain.Models.User.Request.RegisterUserRequest;
import org.example.Exception.BusinessException;
import org.example.Exception.BusinessExceptionCode;
import org.example.Repository.RoleRepository;
import org.example.Repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.example.Domain.Entities.ERole.ROLE_USER;

@Service
public class UserDetailsServiceImplementation implements UserDetailsService {

    @Value("${security.decipherKey}")
    private String key;

    @Autowired
    private UsersRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final RoleRepository roleRepository;
    private static final String[] COLORS = {
            "#2196F3", "#4CAF50", "#FF9800", "#9C27B0", "#673AB7",
            "#FF5722", "#795548", "#607D8B"
    };

    @Autowired
    public UserDetailsServiceImplementation(UsersRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with username: " + username));

        return UserDetailsImplementation.build(user);
    }

    public void registerUser(RegisterUserRequest registerRequest) throws BusinessException {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new BusinessException(BusinessExceptionCode.USERNAME_ALREADY_REGISTERED);
        }

        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new BusinessException(BusinessExceptionCode.EMAIL_ALREADY_REGISTERED);
        }
        String decryptedPassword = AuthController.decrypt(registerRequest.getPassword(), key);

        Role role = roleRepository.findByName(registerRequest.getRole()).orElseThrow(() -> new BusinessException(BusinessExceptionCode.INVALID_USER_FORMAT));
        Set<Badge> badges=new HashSet<>();
        String avatarColor = getRandomColor();
        User userToSave = new User(registerRequest.getEmail(),registerRequest.getUsername(),passwordEncoder.encode(decryptedPassword),0,0, avatarColor,true,badges,Set.of(role));

        userRepository.save(userToSave);
    }

    private String getRandomColor() {
        SecureRandom random = new SecureRandom();
        int index = random.nextInt(COLORS.length);
        return COLORS[index];
    }
}
