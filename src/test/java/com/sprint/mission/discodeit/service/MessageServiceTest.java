package com.sprint.mission.discodeit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.Message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.Message.MessageDto;
import com.sprint.mission.discodeit.dto.Message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.User.UserDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.Channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.Message.MessageNotFoundException;
import com.sprint.mission.discodeit.exception.User.UserNotFoundException;
import com.sprint.mission.discodeit.mapper.MessageMapper;
import com.sprint.mission.discodeit.mapper.PageResponseMapper;
import com.sprint.mission.discodeit.repository.BinaryContentRepository;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.basic.BasicMessageService;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 단위 테스트")
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private BinaryContentRepository binaryContentRepository;
    @Mock
    private ChannelRepository channelRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BinaryContentStorage binaryContentStorage;
    @Mock
    private MessageMapper messageMapper;
    @Mock
    private PageResponseMapper pageResponseMapper;

    @InjectMocks
    private BasicMessageService messageService;

    private Message message;
    private Message message1;
    private Message message2;
    private Channel channel;
    private User user;
    private BinaryContent attachment;

    @BeforeEach
    void setUp() {
        user = new User("테스트유저", "test@codeit.com", "test1234", null, null);
        channel = new Channel(ChannelType.PUBLIC, "테스트 채널", "테스트 채널입니다.");
        message = new Message("테스트 메시지입니다.", channel, user, null);
        message1 = new Message("첫번째 메시지", channel, user, null);
        message2 = new Message("두번째 메시지", channel, user, null);
        attachment = new BinaryContent("test.png", 1024L, "image/png");

        ReflectionTestUtils.setField(message, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(message1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(message2, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(channel, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(attachment, "id", UUID.randomUUID());
    }

    @Test
    @DisplayName("유효한 생성 요청으로 첨부파일이 포함된 메시지를 생성할 수 있다.")
    void 메시지_생성요청_첨부파일포함_성공() {

        // given
        UUID userId = user.getId();
        UUID channelId = channel.getId();
        UUID attachmentId = UUID.randomUUID();
        byte[] testBytes = "테스트 이미지".getBytes(StandardCharsets.UTF_8);
        String content = "메시지 생성 테스트입니다.";

        MessageCreateRequest createRequest = new MessageCreateRequest(content, userId, channelId);
        BinaryContentCreateRequest binaryRequest = new BinaryContentCreateRequest(
                "test.png", "image/png", testBytes);
        List<BinaryContentCreateRequest> binaryRequests = List.of(binaryRequest);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));
        given(binaryContentRepository.saveAll(anyList())).willAnswer(invocation -> {
            List<BinaryContent> savedList = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedList.get(0), "id", attachmentId);
            return savedList;
        });
        given(binaryContentStorage.put(eq(attachmentId), eq(testBytes))).willReturn(attachmentId);

        // when
        messageService.create(createRequest, binaryRequests);

        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        then(messageRepository).should().save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo(content);
        assertThat(savedMessage.getChannel()).isEqualTo(channel);
        assertThat(savedMessage.getAuthor()).isEqualTo(user);
        assertThat(savedMessage.getAttachments()).isNotNull();
        assertThat(savedMessage.getAttachments()).hasSize(1);
        assertThat(savedMessage.getAttachments().get(0).getId()).isEqualTo(attachmentId);
    }

    @Test
    @DisplayName("유효한 생성 요청으로 첨부파일이 포함되지 않은 메시지를 생성할 수 있다.")
    void 메시지_생성요청_첨부파일미포함_성공() {

        // given
        UUID userId = user.getId();
        UUID channelId = channel.getId();
        String content = "메시지 생성 테스트입니다.";
        MessageCreateRequest createRequest = new MessageCreateRequest(content, userId, channelId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.of(channel));

        // when
        messageService.create(createRequest, new ArrayList<>());

        // then
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        then(messageRepository).should().save(messageCaptor.capture());

        Message savedMessage = messageCaptor.getValue();

        assertThat(savedMessage).isNotNull();
        assertThat(savedMessage.getContent()).isEqualTo(content);
        assertThat(savedMessage.getChannel()).isEqualTo(channel);
        assertThat(savedMessage.getAuthor()).isEqualTo(user);
        assertThat(savedMessage.getAttachments()).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 유저로 메시지 전송 시 예외가 발생한다.")
    void 메시지_생성요청_존재하지않는유저_예외발생() {

        // given
        UUID userId = UUID.randomUUID();
        UUID channelId = channel.getId();
        String content = "메시지 생성 테스트입니다.";
        MessageCreateRequest createRequest = new MessageCreateRequest(content, userId, channelId);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            messageService.create(createRequest, new ArrayList<>());
        }).isInstanceOf(UserNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(channelRepository).shouldHaveNoInteractions();
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(messageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("존재하지 않는 채널에서 메시지 전송 시 예외가 발생한다.")
    void 메시지_생성요청_존재하지않는채널_예외발생() {

        // given
        UUID userId = user.getId();
        UUID channelId = UUID.randomUUID();
        String content = "메시지 생성 테스트입니다.";
        MessageCreateRequest createRequest = new MessageCreateRequest(content, userId, channelId);

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(channelRepository.findById(channelId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            messageService.create(createRequest, new ArrayList<>());
        }).isInstanceOf(ChannelNotFoundException.class);

        then(userRepository).should().findById(userId);
        then(channelRepository).should().findById(channelId);
        then(binaryContentStorage).shouldHaveNoInteractions();
        then(binaryContentRepository).shouldHaveNoInteractions();
        then(messageRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("유효한 수정 요청으로 메시지 내용을 수정할 수 있다.")
    void 메시지_수정요청_성공() {

        // given
        UUID messageId = message.getId();
        String newContent = "메시지 수정 테스트입니다.";
        MessageUpdateRequest updateRequest = new MessageUpdateRequest(newContent);

        given(messageRepository.findById(messageId)).willReturn(Optional.of(message));

        // when
        messageService.update(messageId, updateRequest);

        // then
        assertThat(message).isNotNull();
        assertThat(message.getContent()).isEqualTo(newContent);
        then(messageRepository).should().findById(messageId);
    }

    @Test
    @DisplayName("존재하지 않는 메시지 수정 요청시 예외가 발생한다.")
    void 메시지_수정요청_존재하지않는메시지_예외발생() {

        // given
        UUID messageId = UUID.randomUUID();
        String newContent = "존재하지 않는 메시지 수정 테스트입니다.";
        MessageUpdateRequest updateRequest = new MessageUpdateRequest(newContent);

        given(messageRepository.findById(messageId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> {
            messageService.update(messageId, updateRequest);
        }).isInstanceOf(MessageNotFoundException.class);
        then(messageRepository).should().findById(messageId);
    }

    @Test
    @DisplayName("유효한 삭제 요청으로 메시지를 삭제할 수 있다.")
    void 메시지_삭제요청_성공() {

        // given
        UUID messageId = message.getId();

        given(messageRepository.existsById(messageId)).willReturn(true);
        willDoNothing().given(messageRepository).deleteById(messageId);

        // when
        messageService.delete(messageId);

        // then
        then(messageRepository).should().existsById(messageId);
        then(messageRepository).should().deleteById(messageId);
    }

    @Test
    @DisplayName("존재하지 않는 메시지를 삭제 요청시 예외가 발생한다.")
    void 메시지_삭제요청_존재하지않는메시지_예외발생() {

        // given
        UUID messageId = UUID.randomUUID();

        given(messageRepository.existsById(messageId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> {
            messageService.delete(messageId);
        }).isInstanceOf(MessageNotFoundException.class);

        then(messageRepository).should().existsById(messageId);
        then(messageRepository).should(never()).deleteById(messageId);
    }

    @Test
    @DisplayName("커서가 있는 경우, 해당 시각 이전의 메시지들만 조회된다.")
    void 채널ID로_메시지조회시_메시지존재_커서있음() {

        // given
        UUID channelId = channel.getId();
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(0, 10);
        UUID message1Id = message1.getId();
        UUID message2Id = message2.getId();
        Instant message1Time = now.minusSeconds(30);
        Instant message2Time = now.minusSeconds(10);
        Instant targetTime = now.minusSeconds(20);

        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), null,
                true);

        List<Message> messages = List.of(message1);
        List<MessageDto> messageDtos = List.of(
                new MessageDto(message1Id, message1Time, message1Time, "첫번째 메시지", channel.getId(),
                        userDto, new ArrayList<>())
        );

        Slice<Message> messageSlice = new SliceImpl<>(messages, pageable, true);

        given(channelRepository.existsById(channelId)).willReturn(true);
        given(messageRepository.findAllByChannelIdWithAuthor(eq(channelId), any(),
                eq(pageable))).willReturn(messageSlice);
        given(messageMapper.toDto(eq(message1))).willReturn(messageDtos.get(0));
        given(pageResponseMapper.fromSlice(
                ArgumentMatchers.<Slice<MessageDto>>any(),
                any()
        )).willReturn(new PageResponse<>(
                messageDtos,
                messageDtos.get(0).createdAt(),
                messageDtos.size(),
                true,
                null
        ));

        // when
        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, targetTime,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().get(0).content()).isEqualTo("첫번째 메시지");
        assertThat(result.nextCursor()).isEqualTo(messageDtos.get(0).createdAt());

        then(messageRepository).should().findAllByChannelIdWithAuthor(eq(channelId), eq(targetTime),
                eq(pageable));
        then(messageMapper).should().toDto(any());
        then(pageResponseMapper).should().fromSlice(any(), any());
    }

    @Test
    @DisplayName("커서가 없는 경우, 현재 시각 기준으로 메시지들을 조회한다.")
    void 채널ID로_메시지조회시_메시지존재_커서없음() {

        // given
        UUID channelId = channel.getId();
        Instant now = Instant.now();
        Pageable pageable = PageRequest.of(0, 10);
        UUID message1Id = message1.getId();
        UUID message2Id = message2.getId();
        Instant message1Time = now.minusSeconds(30);
        Instant message2Time = now.minusSeconds(10);

        UserDto userDto = new UserDto(user.getId(), user.getUsername(), user.getEmail(), null,
                true);

        List<Message> messages = List.of(message1, message2);
        List<MessageDto> messageDtos = List.of(
                new MessageDto(message1Id, message1Time, message1Time, "첫번째 메시지", channel.getId(),
                        userDto, new ArrayList<>()),
                new MessageDto(message2Id, message2Time, message2Time, "두번째 메시지", channel.getId(),
                        userDto, new ArrayList<>())
        );

        Slice<Message> messageSlice = new SliceImpl<>(messages, pageable, true);

        given(channelRepository.existsById(channelId)).willReturn(true);
        given(messageRepository.findAllByChannelIdWithAuthor(eq(channelId), any(),
                eq(pageable))).willReturn(messageSlice);
        given(messageMapper.toDto(eq(message1))).willReturn(messageDtos.get(0));
        given(messageMapper.toDto(eq(message2))).willReturn(messageDtos.get(1));
        given(pageResponseMapper.fromSlice(
                ArgumentMatchers.<Slice<MessageDto>>any(),
                any()
        )).willReturn(new PageResponse<>(
                messageDtos,
                messageDtos.get(1).createdAt(),
                messageDtos.size(),
                true,
                null
        ));

        // when
        PageResponse<MessageDto> result = messageService.findAllByChannelId(channelId, null,
                pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).content()).isEqualTo("첫번째 메시지");
        assertThat(result.nextCursor()).isEqualTo(messageDtos.get(1).createdAt());

        then(messageRepository).should()
                .findAllByChannelIdWithAuthor(eq(channelId), any(), eq(pageable));
        then(messageMapper).should(times(2)).toDto(any());
        then(pageResponseMapper).should().fromSlice(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 채널로 조회 요청시 예외가 발생한다.")
    void 채널ID로_메시지조회_존재하지않는채널_예외발생() {

        // given
        UUID channelId = UUID.randomUUID();

        given(channelRepository.existsById(channelId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> {
            messageService.findAllByChannelId(channelId, null, PageRequest.of(0, 10));
        }).isInstanceOf(ChannelNotFoundException.class);

        then(channelRepository).should().existsById(channelId);
        then(messageRepository).shouldHaveNoInteractions();
    }
}