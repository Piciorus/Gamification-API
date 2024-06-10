package org.example.Controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.example.Config.JwtUtils;
import org.example.Config.ResponseEntity1;
import org.example.Domain.Entities.Role;
import org.example.Domain.Entities.Security.UserDetailsImplementation;
import org.example.Domain.Entities.User;
import org.example.Domain.Models.RefreshToken.Response.RefreshTokenResponseDTO;
import org.example.Domain.Models.User.Request.LoginUserRequest;
import org.example.Domain.Models.User.Request.RegisterUserRequest;
import org.example.Domain.Models.User.Response.LoginUserResponse;
import org.example.Exception.BusinessException;
import org.example.Exception.BusinessExceptionCode;
import org.example.Repository.RoleRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Implementation.security.UserDetailsServiceImplementation;
import org.example.Service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String REFRESH_TOKEN_COOKIE_NAME = "RefreshTokenCookie";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenService refreshTokenService;
    @Value("${security.decipherKey}")
    private String key;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    UserDetailsServiceImplementation userDetailsService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginUserRequest loginUserRequest) throws BusinessException {
        try {
            String decryptedPassword = decrypt(loginUserRequest.getPassword(), key);

            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginUserRequest.getUsername(), decryptedPassword));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetailsImplementation userDetails = (UserDetailsImplementation) authentication.getPrincipal();

            String jwt = jwtUtils.generateJwtToken(userDetails);

            String refreshToken = UUID.randomUUID().toString();

            refreshTokenService.deleteRefreshTokenForUser(userDetails.getId());
            refreshTokenService.createRefreshToken(refreshToken, userDetails.getId());

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.SET_COOKIE, createCookie(refreshToken).toString());
            LoginUserResponse response = new LoginUserResponse(jwt,userDetails.getId(),userDetails.getUsername(),userDetails.getEmail(),userDetails.getTokens(),userDetails.getThreshold(),userDetails.isFirstLogin());

            return new ResponseEntity<>(response, headers, HttpStatus.OK);
        } catch (AuthenticationException e) {
            throw new BusinessException(BusinessExceptionCode.INVALID_CREDENTIALS);
        }
    }
    private ResponseCookie createCookie(String token) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .maxAge(Duration.ofDays(1))
                .sameSite("Lax")
                .path("/auth/refreshToken")
                .build();
    }
    @PostMapping("/register")
    public ResponseEntity1<?> registerUser(@RequestBody RegisterUserRequest registerRequest) throws BusinessException {
        this.userDetailsService.registerUser(registerRequest);
        return new ResponseEntity1<>("", 200, "Registered successfully!");
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<RefreshTokenResponseDTO> checkCookie(HttpServletRequest request) throws BusinessException {
        Optional<Cookie> cookie = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals(REFRESH_TOKEN_COOKIE_NAME)).findFirst();
        if (cookie.isPresent()) {
            return ResponseEntity.ok(new RefreshTokenResponseDTO(refreshTokenService.exchangeRefreshToken(cookie.get().getValue())));
        }
        throw new BusinessException(BusinessExceptionCode.MISSING_COOKIE);
    }

    public static String decrypt(String toDecrypt, String key) {
        try {
            IvParameterSpec iv = new IvParameterSpec(key.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
            byte[] cipherText = cipher.doFinal(Base64.getDecoder().decode(toDecrypt));
            return new String(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}