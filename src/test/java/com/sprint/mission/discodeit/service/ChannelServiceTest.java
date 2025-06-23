package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;

import com.sprint.mission.discodeit.dto.Channel.ChannelDto;
import com.sprint.mission.discodeit.dto.Channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.Channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.Channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicChannelService;
import java.time.Instant;
import java.util.List;
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
@DisplayName("ChannelService 단위 테스트")
public class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private ReadStatusRepository readStatusRepository;
    @Mock
    private ChannelMapper channelMapper;

    @InjectMocks
    private BasicChannelService channelService;

    private Channel publicChannel;
    private Channel privateChannel;
    private User user;
    private Instant fixedTime;

    @BeforeEach
    void setUp() {
        user = new User("테스트유저", "test@codeit.com", "test1234", null, null);
        publicChannel = new Channel(ChannelType.PUBLIC, "테스트 채널", "테스트 채널입니다.");
        privateChannel = new Channel(ChannelType.PRIVATE, null, null);

        ReflectionTestUtils.setField(publicChannel, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(privateChannel, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());

        fixedTime = Instant.parse("2025-01-01T00:00:00Z");
    }

    @Test
    @DisplayName("유효한 생성 요청으로 공개 채널을 생성할 수 있다.")
    void 공개채널_생성요청_성공() {

        // given
        PublicChannelCreateRequest createRequest = new PublicChannelCreateRequest("공개 채널 생성 테스트",
                "공개 채널 생성 테스트입니다.");

        given(channelRepository.save(any(Channel.class))).willReturn(publicChannel);

        // when
        channelService.create(createRequest);

        // then
        ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
        then(channelRepository).should().save(channelCaptor.capture());

        Channel savedChannel = channelCaptor.getValue();

        assertThat(savedChannel).isNotNull();
        assertThat(savedChannel.getName()).isEqualTo("공개 채널 생성 테스트");
        assertThat(savedChannel.getDescription()).isEqualTo("공개 채널 생성 테스트입니다.");
        assertThat(savedChannel.getType()).isEqualTo(ChannelType.PUBLIC);
    }

    @Test
    @DisplayName("유효한 생성 요청으로 개인 채널을 생성할 수 있다.")
    void 개인채널_생성요청_성공() {

        // given
        PrivateChannelCreateRequest createRequest = new PrivateChannelCreateRequest(
                List.of(user.getId()));

        given(channelRepository.save(any(Channel.class))).willReturn(privateChannel);

        // when
        channelService.create(createRequest);

        // then
        ArgumentCaptor<Channel> channelCaptor = ArgumentCaptor.forClass(Channel.class);
        then(channelRepository).should().save(channelCaptor.capture());

        Channel savedChannel = channelCaptor.getValue();

        assertThat(savedChannel).isNotNull();
        assertThat(savedChannel.getName()).isNull();
        assertThat(savedChannel.getDescription()).isNull();
        assertThat(savedChannel.getType()).isEqualTo(ChannelType.PRIVATE);
    }

    @Test
    @DisplayName("유효한 수정 요청으로 공개 채널을 수정할 수 있다.")
    void 공개채널_수정요청_성공() {

        // given
        PublicChannelUpdateRequest updateRequest = new PublicChannelUpdateRequest("2) 테스트 채널",
                "2) 테스트 채널입니다.");

        given(channelRepository.findById(publicChannel.getId())).willReturn(
                Optional.of(publicChannel));
        given(channelRepository.save(any(Channel.class))).willReturn(publicChannel);

        // when
        channelService.update(publicChannel.getId(), updateRequest);

        // then
        assertThat(publicChannel.getName()).isEqualTo("2) 테스트 채널");
        assertThat(publicChannel.getDescription()).isEqualTo("2) 테스트 채널입니다.");

        then(channelRepository).should().save(publicChannel);
    }

    @Test
    @DisplayName("개인 채널 수정 요청 시 예외가 발생한다.")
    void 개인채널_수정요청_예외발생() {

        // given
        UUID channelId = privateChannel.getId();
        PublicChannelUpdateRequest updateRequest = new PublicChannelUpdateRequest("2) 테스트 채널",
                "2) 테스트 채널입니다.");

        given(channelRepository.findById(channelId)).willReturn(Optional.of(privateChannel));

        // when & then
        assertThatThrownBy(() -> {
            channelService.update(channelId, updateRequest);
        }).isInstanceOf(PrivateChannelUpdateException.class);

        then(channelRepository).should().findById(channelId);
    }

    @Test
    @DisplayName("유효한 삭제 요청으로 채널을 삭제할 수 있다.")
    void 채널_삭제요청_성공() {

        // given
        UUID channelId = publicChannel.getId();

        given(channelRepository.existsById(channelId)).willReturn(true);
        willDoNothing().given(messageRepository).deleteAllByChannelId(channelId);
        willDoNothing().given(readStatusRepository).deleteAllByChannelId(channelId);
        willDoNothing().given(channelRepository).deleteById(channelId);

        // when
        channelService.delete(channelId);

        // then
        then(channelRepository).should().existsById(channelId);
        then(messageRepository).should().deleteAllByChannelId(channelId);
        then(readStatusRepository).should().deleteAllByChannelId(channelId);
        then(channelRepository).should().deleteById(channelId);
    }

    @Test
    @DisplayName("존재하지 않는 채널 삭제 시도 시 예외가 발생한다.")
    void 채널_삭제요청_예외발생() {

        // given
        UUID channelId = UUID.randomUUID();

        given(channelRepository.existsById(channelId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> {
            channelService.delete(channelId);
        }).isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should().existsById(channelId);
        then(messageRepository).shouldHaveNoInteractions();
        then(readStatusRepository).shouldHaveNoInteractions();
        then(channelRepository).should(never()).deleteById(any());
    }

    @Test
    @DisplayName("유효한 유저ID로 해당 유저가 조회 가능한 모든 채널을 조회할 수 있다.")
    void 유저ID로_채널조회시_채널존재() {

        // given
        UUID userId = user.getId();
        ChannelDto publicChannelDto = new ChannelDto(publicChannel.getId(), publicChannel.getName(),
                publicChannel.getDescription(), publicChannel.getType(), fixedTime, List.of());
        ChannelDto privateChannelDto = new ChannelDto(privateChannel.getId(),
                privateChannel.getName(), privateChannel.getDescription(), privateChannel.getType(),
                fixedTime, List.of());

        given(channelRepository.findAllPublicOrUserChannels(userId)).willReturn(
                List.of(publicChannel, privateChannel));
        given(channelMapper.toDto(publicChannel)).willReturn(publicChannelDto);
        given(channelMapper.toDto(privateChannel)).willReturn(privateChannelDto);

        // when
        List<ChannelDto> channels = channelService.findAllByUserId(userId);

        // then
        assertThat(channels).hasSize(2);
        assertThat(channels).containsExactly(publicChannelDto, privateChannelDto);

        then(channelRepository).should().findAllPublicOrUserChannels(userId);
    }

    @Test
    @DisplayName("유저ID로 조회 가능한 채널이 없는 경우 빈 리스트를 반환한다.")
    void 유저ID로_채널조회시_채널없음() {

        // given
        UUID userId = UUID.randomUUID();

        given(channelRepository.findAllPublicOrUserChannels(userId)).willReturn(List.of());

        // when
        List<ChannelDto> result = channelService.findAllByUserId(userId);

        // then
        assertThat(result).isEmpty();
        then(channelRepository).should().findAllPublicOrUserChannels(userId);
        then(channelMapper).should(never()).toDto(any());
    }
}
