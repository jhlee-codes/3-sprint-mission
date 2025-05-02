package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.Channel.ChannelDTO;
import com.sprint.mission.discodeit.dto.Channel.PrivateChannelCreateRequestDTO;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelCreateRequestDTO;
import com.sprint.mission.discodeit.dto.Channel.PublicChannelUpdateRequestDTO;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BasicChannelService implements ChannelService {
    private final ChannelRepository channelRepository;
    private final ReadStatusRepository readStatusRepository;
    private final MessageRepository messageRepository;

    public BasicChannelService(ChannelRepository channelRepository, ReadStatusRepository readStatusRepository, MessageRepository messageRepository) {
        this.channelRepository = channelRepository;
        this.readStatusRepository = readStatusRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * 주어진 요청 DTO를 기반으로 Public 채널 생성 및 저장
     *
     * @param createRequestDTO 생성 요청 DTO
     * @return 생성된 채널
     */
    @Override
    public Channel create(PublicChannelCreateRequestDTO createRequestDTO) {
        // 채널 생성
        Channel ch = new Channel(
                createRequestDTO.name(),
                createRequestDTO.description(),
                false
        );
        channelRepository.save(ch);
        return ch;
    }

    /**
     * 주어진 요청 DTO를 기반으로 Private 채널 생성
     *
     * @param createRequestDTO 생성 요청 DTO
     * @return 생성된 채널
     */
    @Override
    public Channel create(PrivateChannelCreateRequestDTO createRequestDTO) {
        // 채널 생성
        Channel ch = new Channel(
                true
        );
        // User별 ReadStatus 생성
        ReadStatus readStatus = new ReadStatus(createRequestDTO.userId(), ch.getId());
        // 데이터 저장
        channelRepository.save(ch);
        readStatusRepository.save(readStatus);
        return ch;
    }

    /**
     * 주어진 채널을 기반으로 ChannelResponseDTO 생성
     *
     * @param ch 채널엔티티
     * @return ChannelResponseDTO
     */
    private ChannelDTO buildChannelDTO(Channel ch) {
        // 가장 최근 메시지
        Message msg = messageRepository.findByChannelId(ch.getId()).stream()
                .reduce((a, b) -> b)
                .orElse(null);
        // PRIVATE 채널인 경우 참여한 User의 id 정보 포함
        if (ch.isPrivate()) {
            List<UUID> userIdList = readStatusRepository.findUserIdByChannelId(ch.getId());
            return ChannelDTO.from(ch,
                    msg != null ? msg.getCreatedAt() : null,
                    userIdList);
        } else {
            return ChannelDTO.from(ch,
                    msg != null ? msg.getCreatedAt() : null);
        }
    }

    /**
     * 주어진 유저ID가 조회 가능한 채널 전체 조회
     *
     * @param userId 조회할 채널의 유저ID
     * @return 조회된 채널DTO 리스트
     */
    @Override
    public List<ChannelDTO> findAllByUserId(UUID userId) {
        List<Channel> channelList = channelRepository.findAll();

        return channelList.stream()
                .filter(ch -> {
                    // PRIVATE 채널인 경우 유저가 포함된 채널만 반환
                    if (ch.isPrivate()) {
                        List<UUID> userIdList = readStatusRepository.findUserIdByChannelId(ch.getId());
                        return userIdList.contains(userId);
                    } else {
                        return true;
                    }
                })
                .map(this::buildChannelDTO)
                .collect(Collectors.toList());
    }

    /**
     * 주어진 id에 해당하는 채널 조회
     *
     * @param channelId 조회할 채널의 ID
     * @return 조회된 채널
     * @throws NoSuchElementException 해당 ID의 채널이 존재하지 않는 경우
     */
    @Override
    public ChannelDTO find(UUID channelId) {
        Channel ch = channelRepository.findById(channelId)
                .orElseThrow(()->new NoSuchElementException("해당 ID의 채널이 존재하지 않습니다."));
        return buildChannelDTO(ch);
    }

    /**
     * 주어진 채널ID에 해당하는 공개 채널을 수정 요청DTO의 값으로 수정
     *
     * @param channelId 수정 대상 채널ID
     * @param updateRequestDTO 수정 요청 DTO
     * @return 수정된 채널
     * @throws NoSuchElementException 해당 ID의 채널이 존재하지 않는 경우
     * @throws IllegalArgumentException 해당 채널이 PRIVATE 채널인 경우
     */
    @Override
    public Channel update(UUID channelId, PublicChannelUpdateRequestDTO updateRequestDTO) {
        Channel ch = channelRepository.findById(channelId)
                .orElseThrow(() -> new NoSuchElementException("해당 채널이 존재하지 않습니다."));
        // PRIVATE 채널 예외처리
        if (ch.isPrivate()) {
          throw new IllegalArgumentException("PRIVATE 채널은 수정할 수 없습니다.");
        }
        // 채널 수정
        ch.update(
                updateRequestDTO.newName(),
                updateRequestDTO.newDescription()
        );
        channelRepository.save(ch);
        return ch;
    }

    /**
     * 주어진 id에 해당하는 채널 삭제
     *
     * @param channelId 삭제할 대상 채널 id
     */
    @Override
    public void delete(UUID channelId) {
        // 채널 삭제
        channelRepository.deleteById(channelId);

        // 관련 도메인 삭제
        messageRepository.findByChannelId(channelId).stream()
                .map(Message::getId)
                .forEach(messageRepository::deleteById);
        readStatusRepository.findByChannelId(channelId).stream()
                .map(ReadStatus::getId)
                .forEach(readStatusRepository::deleteById);
    }
}
