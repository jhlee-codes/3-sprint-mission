package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserCreateRequest;
import com.sprint.mission.discodeit.dto.User.UserUpdateRequest;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.User.UserAlreadyExistsException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.UserMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicUserService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BinaryContentRepository binaryContentRepository;
    @Mock
    private BinaryContentStorage binaryContentStorage;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private BasicUserService userService;

    private User withProfileUser;
    private BinaryContent profile;

    @BeforeEach
    void setUp() {
        profile = new BinaryContent("profile.png", 1024L, "image/png");
        withProfileUser = new User("이코드", "lee@codeit.com", "lee1234", profile, null);

        ReflectionTestUtils.setField(withProfileUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("유효한 생성 요청으로 프로필이 포함된 유저를 생성할 수 있다.")
    void 유저_생성요청_프로필포함_성공() {

        // given
        String name = "테스트유저";
        String email = "test@codeit.com";
        String password = "test1234";
        byte[] testBytes = "테스트 이미지".getBytes(StandardCharsets.UTF_8);

        UserCreateRequest userCreateRequest = new UserCreateRequest(name, email, password);
        BinaryContentCreateRequest profileCreateRequest = new BinaryContentCreateRequest(
                "profile.png", "image/png", testBytes);

        UUID profileId = UUID.randomUUID();

        given(userRepository.existsByUsername(name)).willReturn(false);
        given(userRepository.existsByEmail(email)).willReturn(false);
        given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
            BinaryContent saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", profileId);
            return saved;
        });
        given(binaryContentStorage.put(eq(profileId), eq(testBytes))).willReturn(profileId);

        // when
        userService.create(userCreateRequest, profileCreateRequest);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());
        then(binaryContentRepository).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should().put(eq(profileId), eq(testBytes));

        User savedUser = userCaptor.getValue();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(name);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(password);
        assertThat(savedUser.getProfile()).isNotNull();
        assertThat(savedUser.getProfile().getId()).isEqualTo(profileId);
        assertThat(savedUser.getStatus()).isNotNull();
    }

    @Test
    @DisplayName("유효한 생성 요청으로 프로필이 없는 유저를 생성할 수 있다.")
    void 유저_생성요청_프로필없음_성공() {

        // given
        String name = "테스트유저";
        String email = "test@codeit.com";
        String password = "test1234";
        UserCreateRequest userCreateRequest = new UserCreateRequest(name, email, password);

        given(userRepository.existsByUsername(name)).willReturn(false);
        given(userRepository.existsByEmail(email)).willReturn(false);

        // when
        userService.create(userCreateRequest, null);

        // then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        then(userRepository).should().save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo(name);
        assertThat(savedUser.getEmail()).isEqualTo(email);
        assertThat(savedUser.getPassword()).isEqualTo(password);
        assertThat(savedUser.getProfile()).isNull();
        assertThat(savedUser.getStatus()).isNotNull();

        then(binaryContentRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 존재하는 유저명으로 유저 생성 요청시 예외가 발생한다.")
    void 유저_생성요청_중복유저명예외발생() {

        // given
        String name = "테스트유저";
        String email = "test@codeit.com";
        String password = "test1234";
        UserCreateRequest userCreateRequest = new UserCreateRequest(name, email, password);

        given(userRepository.existsByUsername(name)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            userService.create(userCreateRequest, null);
        }).isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should().existsByUsername(name);
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 유저 생성 요청시 예외가 발생한다.")
    void 유저_생성요청_중복이메일예외발생() {

        // given
        String name = "테스트유저";
        String email = "test@codeit.com";
        String password = "test1234";
        UserCreateRequest userCreateRequest = new UserCreateRequest(name, email, password);

        given(userRepository.existsByEmail(email)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            userService.create(userCreateRequest, null);
        }).isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should().existsByEmail(email);
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("유효한 수정 요청으로 유저 정보를 수정할 수 있다.")
    void 유저_수정요청_성공() {

        // given
        UUID userId = withProfileUser.getId();
        String newName = "(*수정)김코드";
        String newEmail = "kimkim@codeit.com";
        String newPassword = "kimkim1234";
        byte[] newProfileImage = "테스트 이미지".getBytes();
        UUID profileId = UUID.randomUUID();

        UserUpdateRequest updateRequest = new UserUpdateRequest(newName, newEmail, newPassword);
        BinaryContentCreateRequest profileCreateRequest = new BinaryContentCreateRequest(
                profile.getFileName(), profile.getContentType(), newProfileImage);

        given(userRepository.findById(userId)).willReturn(Optional.of(withProfileUser));
        given(userRepository.existsByUsername(newName)).willReturn(false);
        given(userRepository.existsByEmail(newEmail)).willReturn(false);
        given(binaryContentRepository.save(any(BinaryContent.class))).willAnswer(invocation -> {
            BinaryContent saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", profileId);
            return saved;
        });
        given(binaryContentStorage.put(eq(profileId), eq(newProfileImage))).willReturn(
                profileId);

        // when
        userService.update(userId, updateRequest, profileCreateRequest);

        // then
        then(userRepository).should().findById(userId);
        then(userRepository).should().existsByUsername(newName);
        then(userRepository).should().existsByEmail(newEmail);
        then(binaryContentRepository).should().save(any(BinaryContent.class));
        then(binaryContentStorage).should().put(eq(profileId), eq(newProfileImage));

        assertThat(withProfileUser).isNotNull();
        assertThat(withProfileUser.getUsername()).isEqualTo(newName);
        assertThat(withProfileUser.getEmail()).isEqualTo(newEmail);
        assertThat(withProfileUser.getPassword()).isEqualTo(newPassword);
        assertThat(withProfileUser.getProfile()).isNotNull();
    }

    @Test
    @DisplayName("이미 존재하는 유저명으로 유저 수정 요청시 예외가 발생한다.")
    void 유저_수정요청_중복유저명예외발생() {

        // given
        UUID userId = withProfileUser.getId();
        String newName = "(*수정)김코드";
        String newEmail = "kimkim@codeit.com";
        String newPassword = "kimkim1234";
        UserUpdateRequest updateRequest = new UserUpdateRequest(newName, newEmail, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(withProfileUser));
        given(userRepository.existsByUsername(newName)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            userService.update(userId, updateRequest, null);
        }).isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should().existsByUsername(newName);
        then(userRepository).should(never()).existsByEmail(newEmail);
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 유저 수정 요청시 예외가 발생한다.")
    void 유저_수정요청_중복이메일예외발생() {

        // given
        UUID userId = withProfileUser.getId();
        String newName = "(*수정)김코드";
        String newEmail = "kimkim@codeit.com";
        String newPassword = "kimkim1234";
        UserUpdateRequest updateRequest = new UserUpdateRequest(newName, newEmail, newPassword);

        given(userRepository.findById(userId)).willReturn(Optional.of(withProfileUser));
        given(userRepository.existsByUsername(newName)).willReturn(false);
        given(userRepository.existsByEmail(newEmail)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> {
            userService.update(userId, updateRequest, null);
        }).isInstanceOf(UserAlreadyExistsException.class);

        then(userRepository).should().existsByEmail(newEmail);
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("유효한 삭제 요청으로 유저를 삭제할 수 있다.")
    void 유저_삭제요청_성공() {

        // given
        UUID userId = withProfileUser.getId();

        given(userRepository.existsById(userId)).willReturn(true);
        willDoNothing().given(userRepository).deleteById(userId);

        // when
        userService.delete(userId);

        // then
        then(userRepository).should().existsById(userId);
        then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("존재하지 않는 유저 삭제 요청시 예외가 발생한다.")
    void 유저_삭제요청_존재하지않는유저예외발생() {

        // given
        UUID userId = UUID.randomUUID();

        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> {
            userService.delete(userId);
        }).isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().existsById(userId);
        then(userRepository).should(never()).deleteById(userId);
    }
}