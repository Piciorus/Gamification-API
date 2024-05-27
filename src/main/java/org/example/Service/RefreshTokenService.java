package org.example.Service;

import org.example.Config.JwtUtils;
import org.example.Domain.Entities.RefreshToken;
import org.example.Domain.Entities.User;
import org.example.Exception.BusinessException;
import org.example.Exception.BusinessExceptionCode;
import org.example.Repository.RefreshTokenRepository;
import org.example.Repository.UsersRepository;
import org.example.Service.Implementation.security.UserDetailsServiceImplementation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UsersRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImplementation userDetailsService;

    public void deleteRefreshTokenForUser(UUID userId) {
        refreshTokenRepository.deleteRefreshTokenFromUser(userId);
    }


    public void createRefreshToken(String uuid, UUID userId) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(uuid);
        rt.setExpiryDate(Instant.now().plusSeconds(84000));

        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            rt.setUser(user.get());
            refreshTokenRepository.save(rt);
        }
    }


    public String exchangeRefreshToken(String refreshToken) throws BusinessException {
        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findById(refreshToken);
        if(refreshTokenOptional.isEmpty()) {
            throw new BusinessException(BusinessExceptionCode.INVALID_REFRESH_TOKEN);
        }
        RefreshToken rt = refreshTokenOptional.get();
        if(rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(rt);
            throw new BusinessException(BusinessExceptionCode.EXPIRED_REFRESH_TOKEN);
        }
        return jwtUtils.generateJwtToken(userDetailsService.loadUserByUsername(rt.getUser().getUsername()));
    }

}