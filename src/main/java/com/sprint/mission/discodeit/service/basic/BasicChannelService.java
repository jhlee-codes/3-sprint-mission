package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.annotation.Logging;
import com.sprint.mission.discodeit.dto.Channel.ChannelDto;
import com.sprint.mission.discodeit.dto.Channel.PrivateChannelCreateRequest;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelCreateRequest;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelUpdateRequest;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.exception.Channel.ChannelNotFoundException;
import com.sprint.mission.discodeit.exception.Channel.PrivateChannelUpdateException;
import com.sprint.mission.discodeit.mapper.ChannelMapper;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@Logging
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelMapper channelMapper;

    /**
     * 주어진 요청 DTO를 기반으로 Public 채널 생성
     *
     * @param createRequest 생성 요청 DTO
     * @return 생성된 채널
     */
    @Override
    @Transactional
    public ChannelDto create(PublicChannelCreateRequest createRequest) {
        log.info("{} 채널 생성 요청: 채널명 = {}, 채널 설명 = {}", ChannelType.PUBLIC, createRequest.name(),
                createRequest.description());

        Channel publicChannel = Channel.builder()
                .name(createRequest.name())
                .description(createRequest.description())
                .type(ChannelType.PUBLIC)
                .build();

        channelRepository.save(publicChannel);
        return channelMapper.toDto(publicChannel);
    }

    /**
     * 주어진 요청 DTO를 기반으로 Private 채널 생성
     *
     * @param createRequest 생성 요청 DTO
     * @return 생성된 채널
     */
    @Override
    @Transactional
    public ChannelDto create(PrivateChannelCreateRequest createRequest) {
        log.info("{} 채널 생성 요청: 참여 인원 = {}", ChannelType.PRIVATE, createRequest.participantIds());

        Channel privateChannel = Channel.builder()
                .type(ChannelType.PRIVATE)
                .build();

        channelRepository.save(privateChannel);

        // private 채널 입장 유저의 ReadStatus 생성
        List<ReadStatus> readStatuses = userRepository.findAllById(createRequest.participantIds())
                .stream()
                .map(user -> ReadStatus.builder()
                        .user(user)
                        .channel(privateChannel)
                        .lastReadAt(privateChannel.getCreatedAt())
                        .build())
                .toList();

        readStatusRepository.saveAll(readStatuses);
        return channelMapper.toDto(privateChannel);
    }

    /**
     * 주어진 유저ID가 조회 가능한 채널 전체 조회
     *
     * @param userId 조회할 채널의 유저ID
     * @return 조회된 채널DTO 리스트
     */
    @Override
    @Transactional(readOnly = true)
    public List<ChannelDto> findAllByUserId(UUID userId) {

        List<Channel> channels = channelRepository.findAllPublicOrUserChannels(userId);

        return channels.stream()
                .map(channelMapper::toDto)
                .toList();
    }

    /**
     * 주어진 ID에 해당하는 채널 조회
     *
     * @param channelId 조회할 채널의 ID
     * @return 조회된 채널
     * @throws ChannelNotFoundException 존재하는 채널이 없는 경우
     */
    @Override
    @Transactional(readOnly = true)
    public ChannelDto find(UUID channelId) {

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

        return channelMapper.toDto(channel);
    }

    /**
     * 주어진 채널ID에 해당하는 공개 채널 수정
     *
     * @param channelId     수정할 채널의 ID
     * @param updateRequest 수정 요청 DTO
     * @return 수정된 채널
     * @throws ChannelNotFoundException      존재하는 채널이 없는 경우
     * @throws PrivateChannelUpdateException PRIVATE 채널 수정을 시도한 경우
     */
    @Override
    @Transactional
    public ChannelDto update(UUID channelId, PublicChannelUpdateRequest updateRequest) {
        log.info("{} 채널 수정 요청: 채널명 = {}, 채널 설명 = {}", ChannelType.PUBLIC, updateRequest.newName(),
                updateRequest.newDescription());

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ChannelNotFoundException(channelId));

        if (ChannelType.PRIVATE.equals(channel.getType())) {
            log.warn("채널 수정 실패: PRIVATE 채널 수정 불가");
            throw new PrivateChannelUpdateException(channelId);
        }

        channel.update(updateRequest.newName(), updateRequest.newDescription());

        channelRepository.save(channel);
        return channelMapper.toDto(channel);
    }

    /**
     * 주어진 ID에 해당하는 채널 삭제
     *
     * @param channelId 삭제할 대상 채널 ID
     * @throws ChannelNotFoundException 존재하는 채널이 없는 경우
     */
    @Override
    @Transactional
    public void delete(UUID channelId) {
        log.info("채널 삭제 요청: ID = {}", channelId);

        if (!channelRepository.existsById(channelId)) {
            log.warn("채널 삭제 실패: 존재하지 않는 채널: ID = {}", channelId);
            throw new ChannelNotFoundException(channelId);
        }

        messageRepository.deleteAllByChannelId(channelId);
        readStatusRepository.deleteAllByChannelId(channelId);
        channelRepository.deleteById(channelId);

        log.info("채널 삭제 완료: ID = {}", channelId);
    }
}
