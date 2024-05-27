package org.example.Domain.Models.RefreshToken.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RefreshTokenResponseDTO {
    private String renewedAccessToken;
}