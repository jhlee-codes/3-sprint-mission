package com.sprint.mission.discodeit.auth.jwt.store;

import com.sprint.mission.discodeit.dto.JwtInformation;
import java.util.UUID;

public class InMemoryJwtRegistry implements JwtRegistry {

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {

    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return false;
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return false;
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return false;
    }

    @Override
    public JwtInformation rotateJwtInformation(String refreshToken,
        JwtInformation newJwtInformation) {
        return null;
    }
}
