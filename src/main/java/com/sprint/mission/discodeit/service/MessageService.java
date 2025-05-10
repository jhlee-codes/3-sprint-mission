package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.BinaryContent.BinaryContentCreateRequestDTO;
import com.sprint.mission.discodeit.dto.Message.MessageCreateRequestDTO;
import com.sprint.mission.discodeit.dto.Message.MessageUpdateRequestDTO;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    // 생성
    Message create(MessageCreateRequestDTO createRequestDTO, List<BinaryContentCreateRequestDTO> binaryContentCreateRequestsDTO);
    // 조회 (채널)
    List<Message> findAllByChannelId(UUID channelId);
    // 조회 (ID)
    Message find(UUID messageId);
    // 수정
    Message update(UUID messageId, MessageUpdateRequestDTO updateRequestDTO);
    // 삭제
    void delete(UUID messageId);
}
