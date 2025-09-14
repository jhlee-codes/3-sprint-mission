package com.sprint.mission.discodeit.exception.Auth;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class InvalidTokenException extends AuthException {
    
    public InvalidTokenException(String token) {
        super(
            ErrorCode.TOKEN_INVALID,
            Map.of("token", token)
        );
    }
}
