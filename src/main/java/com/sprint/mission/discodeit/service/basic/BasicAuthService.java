package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.DiscodeitUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class BasicAuthService implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDto getCurrentUserInfo(DiscodeitUserDetails userDetails) {

        log.debug("[AuthService] 사용자 정보 조회 요청");

        String userName = userDetails.getUsername();

        User user = userRepository.findByUsername(userName)
            .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userName));

        log.debug("[AuthService] 조회된 사용자 정보: {}", user);

        return userMapper.toDto(user);
    }
}
